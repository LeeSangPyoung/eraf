package com.eraf.featureflag;

import com.eraf.featureflag.feature.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagCacheManagerTest {

    @Mock
    private RedisTemplate<String, Boolean> redisTemplate;

    @Mock
    private ValueOperations<String, Boolean> valueOperations;

    @Test
    @DisplayName("Redis 없는 환경에서 getEnabled은 null 반환")
    void testWithoutRedis() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(null, properties);

        assertNull(cacheManager.getEnabled("test"));
        assertFalse(cacheManager.isRedisAvailable());
    }

    @Test
    @DisplayName("Redis 비활성 시 null 반환")
    void testRedisDisabled() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.getCache().setRedisEnabled(false);

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);

        assertNull(cacheManager.getEnabled("test"));
        assertFalse(cacheManager.isRedisAvailable());
    }

    @Test
    @DisplayName("Redis 캐시 히트")
    void testGetEnabledCacheHit() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("eraf:feature-flag:test:enabled")).thenReturn(true);

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);

        Boolean result = cacheManager.getEnabled("test");
        assertTrue(result);
    }

    @Test
    @DisplayName("Redis 캐시 미스")
    void testGetEnabledCacheMiss() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("eraf:feature-flag:unknown:enabled")).thenReturn(null);

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);

        assertNull(cacheManager.getEnabled("unknown"));
    }

    @Test
    @DisplayName("캐시 저장")
    void testPutEnabled() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        properties.getCache().setL2Ttl(Duration.ofSeconds(60));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);
        cacheManager.putEnabled("test", true);

        verify(valueOperations).set("eraf:feature-flag:test:enabled", true, 60000, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("캐시 삭제")
    void testEvict() {
        FeatureFlagProperties properties = new FeatureFlagProperties();

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);
        cacheManager.evict("test");

        verify(redisTemplate).delete("eraf:feature-flag:test:enabled");
    }

    @Test
    @DisplayName("전체 캐시 삭제")
    void testEvictAll() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        when(redisTemplate.keys("eraf:feature-flag:*")).thenReturn(Set.of("key1", "key2"));

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);
        cacheManager.evictAll();

        verify(redisTemplate).delete(Set.of("key1", "key2"));
    }

    @Test
    @DisplayName("Redis 예외 시 graceful fallback")
    void testRedisExceptionGraceful() {
        FeatureFlagProperties properties = new FeatureFlagProperties();
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        FeatureFlagCacheManager cacheManager = new FeatureFlagCacheManager(redisTemplate, properties);

        // Should not throw, just return null
        assertNull(cacheManager.getEnabled("test"));
    }
}
