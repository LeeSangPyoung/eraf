package com.eraf.starter.redis;

import com.eraf.core.idempotent.IdempotencyStore;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 멱등성 저장소
 * 분산 환경에서 중복 요청 방지
 */
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "eraf:idempotent:";
    private static final String PROCESSING = "__PROCESSING__";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration defaultTtl;

    public RedisIdempotencyStore(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, Duration.ofHours(24));
    }

    public RedisIdempotencyStore(RedisTemplate<String, Object> redisTemplate, Duration defaultTtl) {
        this.redisTemplate = redisTemplate;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public boolean setIfAbsent(String key, Duration timeout) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                KEY_PREFIX + key,
                PROCESSING,
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + key));
    }

    @Override
    public void saveResult(String key, Object result, Duration timeout) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + key,
                result != null ? result : "null",
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public Optional<Object> getResult(String key) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + key);
        if (value == null || PROCESSING.equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(KEY_PREFIX + key);
    }

    /**
     * 처리 중 상태인지 확인
     */
    public boolean isProcessing(String key) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + key);
        return PROCESSING.equals(value);
    }

    /**
     * 처리 완료 상태로 전환
     */
    public void markAsCompleted(String key, Object result, Duration ttl) {
        saveResult(key, result, ttl);
    }

    /**
     * 처리 실패 시 마킹 제거 (재시도 허용)
     */
    public void clearProcessing(String key) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + key);
        if (PROCESSING.equals(value)) {
            redisTemplate.delete(KEY_PREFIX + key);
        }
    }
}
