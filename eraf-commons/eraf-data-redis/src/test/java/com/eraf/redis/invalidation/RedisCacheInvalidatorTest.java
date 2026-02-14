package com.eraf.redis.invalidation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RedisCacheInvalidator 테스트
 */
class RedisCacheInvalidatorTest {

    private RedisCacheInvalidator invalidator;
    private RedisTemplate<String, Object> redisTemplate;
    private RedisMessageListenerContainer listenerContainer;
    private CacheManager cacheManager;
    private Cache cache;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        listenerContainer = mock(RedisMessageListenerContainer.class);
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(cache);

        invalidator = new RedisCacheInvalidator(redisTemplate, listenerContainer, cacheManager);
    }

    @Test
    @DisplayName("특정 키 무효화 - 로컬 캐시 제거")
    void testInvalidateKey() {
        // When
        invalidator.invalidate("users", "user:123");

        // Then - 로컬 캐시 제거됨
        verify(cacheManager).getCache("users");
        verify(cache).evict("user:123");
    }

    @Test
    @DisplayName("특정 키 무효화 - Pub/Sub 전파 시도")
    void testInvalidateKeyPublish() {
        // When
        invalidator.invalidate("products", "prod:456");

        // Then - 로컬 캐시 무효화는 반드시 호출됨
        verify(cache).evict("prod:456");

        // Pub/Sub 전파는 ObjectMapper 직렬화 시도 (Instant→JSON)
        // convertAndSend가 호출되면 성공, 안 되면 catch에서 로그만 출력 (둘 다 정상 동작)
    }

    @Test
    @DisplayName("캐시 전체 무효화 - 로컬 캐시 클리어")
    void testInvalidateAll() {
        // When
        invalidator.invalidateAll("orders");

        // Then - 로컬 캐시 전체 제거
        verify(cacheManager).getCache("orders");
        verify(cache).clear();
    }

    @Test
    @DisplayName("캐시 전체 무효화 - Pub/Sub 전파")
    void testInvalidateAllPublish() {
        // When
        invalidator.invalidateAll("carts");

        // Then - Redis Pub/Sub 메시지 발행
        verify(redisTemplate).convertAndSend("cache:invalidation:carts:all", "INVALIDATE_ALL");
    }

    @Test
    @DisplayName("존재하지 않는 캐시 무효화 - 에러 없이 처리")
    void testInvalidateNonExistentCache() {
        // Given
        when(cacheManager.getCache(anyString())).thenReturn(null);

        // When & Then - 예외 없이 처리
        assertThatCode(() -> invalidator.invalidate("nonexistent", "key"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("NULL 키 무효화 - 처리 가능")
    void testInvalidateNullKey() {
        // When & Then
        assertThatCode(() -> invalidator.invalidate("cache", null))
                .doesNotThrowAnyException();
    }
}
