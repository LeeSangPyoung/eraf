package com.eraf.gateway.ratelimit.advanced.service;

import com.eraf.gateway.ratelimit.advanced.algorithm.*;
import com.eraf.gateway.ratelimit.advanced.domain.AdvancedRateLimitRule;
import com.eraf.gateway.ratelimit.advanced.domain.RateLimitAlgorithm;
import com.eraf.gateway.ratelimit.advanced.repository.RedisRateLimitRepository;
import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import com.eraf.gateway.ratelimit.exception.RateLimitExceededException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 고급 Rate Limit 서비스
 * - 다양한 알고리즘 지원 (Token Bucket, Leaky Bucket, Sliding Window, Fixed Window)
 * - Redis 기반 분산 제한
 * - Consumer별 제한
 * - 헤더 기반 제한
 */
@Slf4j
public class AdvancedRateLimitService {

    private final List<AdvancedRateLimitRule> rules;
    private final RedisRateLimitRepository redisRepository;
    private final boolean distributedMode;
    private final Map<String, RateLimiter> localLimiters = new ConcurrentHashMap<>();

    public AdvancedRateLimitService(
            List<AdvancedRateLimitRule> rules,
            RedisRateLimitRepository redisRepository,
            boolean distributedMode) {
        this.rules = rules;
        this.redisRepository = redisRepository;
        this.distributedMode = distributedMode && redisRepository != null;
    }

    /**
     * Rate Limit 체크
     *
     * @param path       요청 경로
     * @param identifier 식별자 (IP, API Key, User ID 등)
     * @param type       Rate Limit 타입
     * @param headers    헤더 정보 (헤더 기반 제한용)
     * @throws RateLimitExceededException 제한 초과 시
     */
    public void checkRateLimit(String path, String identifier, RateLimitRule.RateLimitType type, Map<String, String> headers) {
        AdvancedRateLimitRule rule = findMatchingRule(path, type);
        if (rule == null || !rule.isValid()) {
            return;
        }

        // Consumer별 제한 확인
        String consumerId = extractConsumerId(headers);
        if (consumerId != null) {
            AdvancedRateLimitRule.ConsumerLimit consumerLimit = rule.getConsumerLimit(consumerId);
            if (consumerLimit != null) {
                checkConsumerLimit(rule, consumerLimit, identifier, consumerId);
                return;
            }
        }

        // 헤더 기반 제한 확인
        Integer headerLimit = getHeaderBasedLimit(rule, headers);
        if (headerLimit != null) {
            checkWithCustomLimit(rule, identifier, headerLimit);
            return;
        }

        // 기본 제한 확인
        checkDefaultLimit(rule, identifier);
    }

    /**
     * 기본 Rate Limit 체크 (헤더 없이)
     */
    public void checkRateLimit(String path, String identifier, RateLimitRule.RateLimitType type) {
        checkRateLimit(path, identifier, type, Map.of());
    }

    /**
     * Rate Limit 정보 조회
     */
    public RateLimitInfo getRateLimitInfo(String path, String identifier, RateLimitRule.RateLimitType type) {
        AdvancedRateLimitRule rule = findMatchingRule(path, type);
        if (rule == null || !rule.isValid()) {
            return null;
        }

        String key = buildKey(rule, identifier);
        long remaining = getRemainingRequests(rule, key);
        long reset = getResetTime(rule, key);

        return RateLimitInfo.builder()
                .limit(rule.getMaxRequests())
                .remaining(remaining)
                .resetTimeSeconds(reset)
                .algorithm(rule.getAlgorithm().name())
                .build();
    }

    /**
     * 기본 제한 체크
     */
    private void checkDefaultLimit(AdvancedRateLimitRule rule, String identifier) {
        String key = buildKey(rule, identifier);
        boolean allowed;

        if (distributedMode) {
            allowed = checkDistributed(rule, key);
        } else {
            allowed = checkLocal(rule, key);
        }

        if (!allowed) {
            int retryAfterSeconds = (int) ((getResetTime(rule, key) - System.currentTimeMillis()) / 1000);
            throw new RateLimitExceededException(
                    retryAfterSeconds,
                    rule.getMaxRequests()
            );
        }
    }

    /**
     * Consumer별 제한 체크
     */
    private void checkConsumerLimit(AdvancedRateLimitRule rule, AdvancedRateLimitRule.ConsumerLimit consumerLimit,
                                     String identifier, String consumerId) {
        String key = buildKey(rule, identifier + ":" + consumerId);
        boolean allowed;

        if (distributedMode) {
            allowed = checkDistributedWithConsumer(rule, key, consumerLimit);
        } else {
            allowed = checkLocalWithConsumer(rule, key, consumerLimit);
        }

        if (!allowed) {
            int retryAfterSeconds = (int) ((getResetTime(rule, key) - System.currentTimeMillis()) / 1000);
            int limit = consumerLimit.getMaxRequests();
            throw new RateLimitExceededException(retryAfterSeconds, limit);
        }
    }

    /**
     * 커스텀 제한으로 체크
     */
    private void checkWithCustomLimit(AdvancedRateLimitRule rule, String identifier, int customLimit) {
        String key = buildKey(rule, identifier);

        // 간단하게 Fixed Window로 처리
        boolean allowed;
        if (distributedMode) {
            allowed = redisRepository.allowFixedWindow(key, customLimit, rule.getWindowSeconds());
        } else {
            RateLimiter limiter = getOrCreateLimiter(rule, customLimit);
            allowed = limiter.allowRequest(key);
        }

        if (!allowed) {
            int retryAfterSeconds = (int) ((getResetTime(rule, key) - System.currentTimeMillis()) / 1000);
            throw new RateLimitExceededException(
                    retryAfterSeconds,
                    customLimit
            );
        }
    }

    /**
     * 분산 모드 체크
     */
    private boolean checkDistributed(AdvancedRateLimitRule rule, String key) {
        return switch (rule.getAlgorithm()) {
            case TOKEN_BUCKET -> redisRepository.allowTokenBucket(key, rule.getBurstSize(), rule.getRefillRate());
            case LEAKY_BUCKET -> redisRepository.allowTokenBucket(key, rule.getMaxRequests(), rule.getRefillRate());
            case SLIDING_WINDOW -> redisRepository.allowSlidingWindow(key, rule.getMaxRequests(), rule.getWindowSeconds());
            case FIXED_WINDOW -> redisRepository.allowFixedWindow(key, rule.getMaxRequests(), rule.getWindowSeconds());
        };
    }

    /**
     * Consumer별 분산 모드 체크
     */
    private boolean checkDistributedWithConsumer(AdvancedRateLimitRule rule, String key,
                                                  AdvancedRateLimitRule.ConsumerLimit consumerLimit) {
        return switch (rule.getAlgorithm()) {
            case TOKEN_BUCKET -> redisRepository.allowTokenBucket(key, consumerLimit.getBurstSize(), consumerLimit.getRefillRate());
            case LEAKY_BUCKET -> redisRepository.allowTokenBucket(key, consumerLimit.getMaxRequests(), consumerLimit.getRefillRate());
            case SLIDING_WINDOW -> redisRepository.allowSlidingWindow(key, consumerLimit.getMaxRequests(), rule.getWindowSeconds());
            case FIXED_WINDOW -> redisRepository.allowFixedWindow(key, consumerLimit.getMaxRequests(), rule.getWindowSeconds());
        };
    }

    /**
     * 로컬 모드 체크
     */
    private boolean checkLocal(AdvancedRateLimitRule rule, String key) {
        RateLimiter limiter = getOrCreateLimiter(rule);
        return limiter.allowRequest(key);
    }

    /**
     * Consumer별 로컬 모드 체크
     */
    private boolean checkLocalWithConsumer(AdvancedRateLimitRule rule, String key,
                                            AdvancedRateLimitRule.ConsumerLimit consumerLimit) {
        // Consumer별로 별도의 limiter 생성
        String limiterKey = rule.getId() + ":consumer:" + consumerLimit.getConsumerId();
        RateLimiter limiter = localLimiters.computeIfAbsent(limiterKey, k -> createLimiterForConsumer(rule, consumerLimit));
        return limiter.allowRequest(key);
    }

    /**
     * Limiter 생성 또는 조회
     */
    private RateLimiter getOrCreateLimiter(AdvancedRateLimitRule rule) {
        return localLimiters.computeIfAbsent(rule.getId(), k -> createLimiter(rule));
    }

    /**
     * 커스텀 제한으로 Limiter 생성 또는 조회
     */
    private RateLimiter getOrCreateLimiter(AdvancedRateLimitRule rule, int customLimit) {
        String key = rule.getId() + ":custom:" + customLimit;
        return localLimiters.computeIfAbsent(key, k ->
            new FixedWindowRateLimiter(customLimit, rule.getWindowSeconds())
        );
    }

    /**
     * Limiter 생성
     */
    private RateLimiter createLimiter(AdvancedRateLimitRule rule) {
        return switch (rule.getAlgorithm()) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(rule.getBurstSize(), rule.getRefillRate(), rule.getWindowSeconds());
            case LEAKY_BUCKET -> new LeakyBucketRateLimiter(rule.getMaxRequests(), rule.getRefillRate(), rule.getWindowSeconds());
            case SLIDING_WINDOW -> new SlidingWindowRateLimiter(rule.getMaxRequests(), rule.getWindowSeconds());
            case FIXED_WINDOW -> new FixedWindowRateLimiter(rule.getMaxRequests(), rule.getWindowSeconds());
        };
    }

    /**
     * Consumer용 Limiter 생성
     */
    private RateLimiter createLimiterForConsumer(AdvancedRateLimitRule rule, AdvancedRateLimitRule.ConsumerLimit consumerLimit) {
        return switch (rule.getAlgorithm()) {
            case TOKEN_BUCKET -> new TokenBucketRateLimiter(consumerLimit.getBurstSize(), consumerLimit.getRefillRate(), rule.getWindowSeconds());
            case LEAKY_BUCKET -> new LeakyBucketRateLimiter(consumerLimit.getMaxRequests(), consumerLimit.getRefillRate(), rule.getWindowSeconds());
            case SLIDING_WINDOW -> new SlidingWindowRateLimiter(consumerLimit.getMaxRequests(), rule.getWindowSeconds());
            case FIXED_WINDOW -> new FixedWindowRateLimiter(consumerLimit.getMaxRequests(), rule.getWindowSeconds());
        };
    }

    /**
     * 남은 요청 수 조회
     */
    private long getRemainingRequests(AdvancedRateLimitRule rule, String key) {
        if (distributedMode) {
            return redisRepository.getRemainingRequests(key, rule.getMaxRequests());
        } else {
            RateLimiter limiter = getOrCreateLimiter(rule);
            return limiter.getRemainingRequests(key);
        }
    }

    /**
     * 리셋 시간 조회
     */
    private long getResetTime(AdvancedRateLimitRule rule, String key) {
        if (distributedMode) {
            return redisRepository.getTTL(key, rule.getAlgorithm().name().toLowerCase());
        } else {
            RateLimiter limiter = getOrCreateLimiter(rule);
            return limiter.getResetTimeSeconds(key);
        }
    }

    /**
     * 매칭되는 규칙 찾기
     */
    private AdvancedRateLimitRule findMatchingRule(String path, RateLimitRule.RateLimitType type) {
        return rules.stream()
                .filter(rule -> rule.isValid() && rule.getType() == type)
                .filter(rule -> matchesPath(rule, path))
                .min((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()))
                .orElse(null);
    }

    /**
     * 경로 매칭
     */
    private boolean matchesPath(AdvancedRateLimitRule rule, String path) {
        return rule.toBasicRule().matchesPath(path);
    }

    /**
     * Consumer ID 추출
     */
    private String extractConsumerId(Map<String, String> headers) {
        // API Key, User ID 등에서 Consumer ID 추출
        String apiKey = headers.get("X-API-Key");
        if (apiKey != null) {
            return apiKey;
        }

        String userId = headers.get("X-User-ID");
        if (userId != null) {
            return userId;
        }

        return null;
    }

    /**
     * 헤더 기반 제한 조회
     */
    private Integer getHeaderBasedLimit(AdvancedRateLimitRule rule, Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            Integer limit = rule.getHeaderBasedLimit(header.getValue());
            if (limit != null) {
                return limit;
            }
        }
        return null;
    }

    /**
     * Rate Limit 키 생성
     */
    private String buildKey(AdvancedRateLimitRule rule, String identifier) {
        return String.format("%s:%s:%s", rule.getId(), rule.getType(), identifier);
    }

    /**
     * Rate Limit 정보
     */
    @Getter
    @Builder
    public static class RateLimitInfo {
        private final int limit;
        private final long remaining;
        private final long resetTimeSeconds;
        private final String algorithm;
    }
}
