package com.eraf.gateway.ratelimit.service;

import com.eraf.gateway.ratelimit.domain.RateLimitRecord;
import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import com.eraf.gateway.ratelimit.exception.RateLimitExceededException;
import com.eraf.gateway.ratelimit.repository.RateLimitRecordRepository;
import com.eraf.gateway.ratelimit.repository.RateLimitRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Rate Limiting 서비스
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitRuleRepository ruleRepository;
    private final RateLimitRecordRepository recordRepository;

    /**
     * 요청에 대한 Rate Limit 체크
     * @param path 요청 경로
     * @param identifier 식별자 (IP, API Key, User ID 등)
     * @param type Rate Limit 타입
     * @throws RateLimitExceededException Rate Limit 초과 시
     */
    public void checkRateLimit(String path, String identifier, RateLimitRule.RateLimitType type) {
        List<RateLimitRule> rules = ruleRepository.findEnabledByType(type);

        for (RateLimitRule rule : rules) {
            if (rule.matchesPath(path)) {
                String key = RateLimitRecord.generateKey(rule.getId(), identifier);

                boolean allowed = recordRepository.incrementAndCheck(
                        key,
                        rule.getWindowSeconds(),
                        rule.getMaxRequests()
                );

                if (!allowed) {
                    long retryAfter = recordRepository.getResetTimeSeconds(key, rule.getWindowSeconds());
                    log.warn("Rate limit exceeded for key: {}, rule: {}", key, rule.getName());
                    throw new RateLimitExceededException((int) retryAfter, rule.getMaxRequests());
                }
            }
        }
    }

    /**
     * 특정 식별자의 Rate Limit 정보 조회
     */
    public RateLimitInfo getRateLimitInfo(String path, String identifier, RateLimitRule.RateLimitType type) {
        List<RateLimitRule> rules = ruleRepository.findEnabledByType(type);

        for (RateLimitRule rule : rules) {
            if (rule.matchesPath(path)) {
                String key = RateLimitRecord.generateKey(rule.getId(), identifier);
                int remaining = recordRepository.getRemainingRequests(key, rule.getMaxRequests());
                long resetTime = recordRepository.getResetTimeSeconds(key, rule.getWindowSeconds());

                return RateLimitInfo.builder()
                        .limit(rule.getMaxRequests())
                        .remaining(remaining)
                        .resetTimeSeconds(resetTime)
                        .windowSeconds(rule.getWindowSeconds())
                        .build();
            }
        }

        return null;
    }

    /**
     * Rate Limit 규칙 생성
     */
    public RateLimitRule createRule(RateLimitRule rule) {
        return ruleRepository.save(rule);
    }

    /**
     * Rate Limit 규칙 수정
     */
    public RateLimitRule updateRule(RateLimitRule rule) {
        return ruleRepository.save(rule);
    }

    /**
     * Rate Limit 규칙 삭제
     */
    public void deleteRule(String ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    /**
     * 모든 규칙 조회
     */
    public List<RateLimitRule> getAllRules() {
        return ruleRepository.findAll();
    }

    /**
     * 규칙 ID로 조회
     */
    public Optional<RateLimitRule> getRule(String ruleId) {
        return ruleRepository.findById(ruleId);
    }

    /**
     * Rate Limit 정보 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class RateLimitInfo {
        private final int limit;
        private final int remaining;
        private final long resetTimeSeconds;
        private final int windowSeconds;
    }
}
