package com.eraf.gateway.ratelimit.advanced.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Redis 기반 분산 Rate Limit Repository
 * Redis Lua 스크립트를 사용하여 원자적 연산 보장
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimitRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "rate-limit:";

    /**
     * Token Bucket 알고리즘용 Lua 스크립트
     * KEYS[1] = rate-limit key
     * ARGV[1] = capacity
     * ARGV[2] = refill rate
     * ARGV[3] = current timestamp
     * Returns: 0 if allowed, 1 if denied
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refill_rate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])

            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1])
            local last_refill = tonumber(bucket[2])

            if tokens == nil then
                tokens = capacity
                last_refill = now
            else
                local time_passed = math.max(0, now - last_refill) / 1000.0
                local tokens_to_add = time_passed * refill_rate
                tokens = math.min(capacity, tokens + tokens_to_add)
                last_refill = now
            end

            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
                redis.call('EXPIRE', key, 3600)
                return 0
            else
                return 1
            end
            """;

    /**
     * Fixed Window 알고리즘용 Lua 스크립트
     * KEYS[1] = rate-limit key
     * ARGV[1] = max requests
     * ARGV[2] = window seconds
     * Returns: 0 if allowed, 1 if denied
     */
    private static final String FIXED_WINDOW_SCRIPT = """
            local key = KEYS[1]
            local max_requests = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])

            local current = redis.call('INCR', key)

            if current == 1 then
                redis.call('EXPIRE', key, window)
            end

            if current <= max_requests then
                return 0
            else
                return 1
            end
            """;

    /**
     * Sliding Window 알고리즘용 Lua 스크립트
     * KEYS[1] = rate-limit key
     * ARGV[1] = max requests
     * ARGV[2] = window milliseconds
     * ARGV[3] = current timestamp
     * Returns: 0 if allowed, 1 if denied
     */
    private static final String SLIDING_WINDOW_SCRIPT = """
            local key = KEYS[1]
            local max_requests = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local cutoff = now - window

            redis.call('ZREMRANGEBYSCORE', key, 0, cutoff)

            local count = redis.call('ZCARD', key)

            if count < max_requests then
                redis.call('ZADD', key, now, now)
                redis.call('EXPIRE', key, math.ceil(window / 1000))
                return 0
            else
                return 1
            end
            """;

    /**
     * Token Bucket 요청 허용 여부 확인
     */
    public boolean allowTokenBucket(String key, int capacity, double refillRate) {
        try {
            String fullKey = KEY_PREFIX + "token:" + key;
            Long result = redisTemplate.execute(
                    RedisScript.of(TOKEN_BUCKET_SCRIPT, Long.class),
                    Collections.singletonList(fullKey),
                    String.valueOf(capacity),
                    String.valueOf(refillRate),
                    String.valueOf(System.currentTimeMillis())
            );
            return result != null && result == 0;
        } catch (Exception e) {
            log.error("Redis error during token bucket check for key: {}", key, e);
            // Fail open - 레디스 오류 시 요청 허용
            return true;
        }
    }

    /**
     * Fixed Window 요청 허용 여부 확인
     */
    public boolean allowFixedWindow(String key, int maxRequests, int windowSeconds) {
        try {
            String fullKey = KEY_PREFIX + "fixed:" + key;
            Long result = redisTemplate.execute(
                    RedisScript.of(FIXED_WINDOW_SCRIPT, Long.class),
                    Collections.singletonList(fullKey),
                    String.valueOf(maxRequests),
                    String.valueOf(windowSeconds)
            );
            return result != null && result == 0;
        } catch (Exception e) {
            log.error("Redis error during fixed window check for key: {}", key, e);
            return true;
        }
    }

    /**
     * Sliding Window 요청 허용 여부 확인
     */
    public boolean allowSlidingWindow(String key, int maxRequests, int windowSeconds) {
        try {
            String fullKey = KEY_PREFIX + "sliding:" + key;
            long windowMillis = windowSeconds * 1000L;
            Long result = redisTemplate.execute(
                    RedisScript.of(SLIDING_WINDOW_SCRIPT, Long.class),
                    Collections.singletonList(fullKey),
                    String.valueOf(maxRequests),
                    String.valueOf(windowMillis),
                    String.valueOf(System.currentTimeMillis())
            );
            return result != null && result == 0;
        } catch (Exception e) {
            log.error("Redis error during sliding window check for key: {}", key, e);
            return true;
        }
    }

    /**
     * 남은 요청 수 조회 (Fixed Window)
     */
    public long getRemainingRequests(String key, int maxRequests) {
        try {
            String fullKey = KEY_PREFIX + "fixed:" + key;
            String value = redisTemplate.opsForValue().get(fullKey);
            if (value == null) {
                return maxRequests;
            }
            long current = Long.parseLong(value);
            return Math.max(0, maxRequests - current);
        } catch (Exception e) {
            log.error("Redis error getting remaining requests for key: {}", key, e);
            return maxRequests;
        }
    }

    /**
     * TTL 조회 (리셋까지 남은 시간)
     */
    public long getTTL(String key, String algorithm) {
        try {
            String fullKey = KEY_PREFIX + algorithm + ":" + key;
            Long ttl = redisTemplate.getExpire(fullKey);
            return ttl != null ? Math.max(0, ttl) : 0;
        } catch (Exception e) {
            log.error("Redis error getting TTL for key: {}", key, e);
            return 0;
        }
    }

    /**
     * 특정 키 리셋
     */
    public void reset(String key) {
        try {
            redisTemplate.delete(KEY_PREFIX + "*:" + key);
            log.debug("Reset rate limit for key: {}", key);
        } catch (Exception e) {
            log.error("Redis error resetting key: {}", key, e);
        }
    }

    /**
     * 모든 Rate Limit 데이터 삭제
     */
    public void resetAll() {
        try {
            var keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Reset all rate limits, deleted {} keys", keys.size());
            }
        } catch (Exception e) {
            log.error("Redis error resetting all keys", e);
        }
    }

    /**
     * 헬스 체크
     */
    public boolean isHealthy() {
        try {
            redisTemplate.opsForValue().get("health-check");
            return true;
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return false;
        }
    }
}
