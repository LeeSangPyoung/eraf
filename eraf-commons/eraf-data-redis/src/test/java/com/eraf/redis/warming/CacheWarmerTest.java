package com.eraf.redis.warming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheWarmer 테스트
 */
class CacheWarmerTest {

    private CacheWarmer cacheWarmer;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        cacheWarmer = new CacheWarmer(redisTemplate);
    }

    @Test
    @DisplayName("워밍 작업 등록")
    void testRegister() {
        // When
        cacheWarmer.register("users", () -> List.of("user1", "user2", "user3"));

        // Then - 등록만 하고 아직 실행 안 됨
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("TTL 지정 워밍 작업 등록")
    void testRegisterWithTTL() {
        // When
        cacheWarmer.register("products", () -> List.of("prod1", "prod2"), Duration.ofMinutes(30));

        // Then - 등록만 하고 아직 실행 안 됨
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("수동 워밍 - 모든 캐시")
    void testWarmAll() {
        // Given
        cacheWarmer.register("cache1", () -> List.of(
                new TestEntity(1L, "item1"),
                new TestEntity(2L, "item2")
        ));

        // When
        CacheWarmer.WarmupResult result = cacheWarmer.warmAll();

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getSuccessCaches()).contains("cache1");
    }

    @Test
    @DisplayName("수동 워밍 - 특정 캐시")
    void testWarmCache() {
        // Given
        cacheWarmer.register("targetCache", () -> List.of(
                new TestEntity(1L, "data1")
        ));
        cacheWarmer.register("otherCache", () -> List.of(
                new TestEntity(2L, "data2")
        ));

        // When
        cacheWarmer.warmCache("targetCache");

        // Then - targetCache만 워밍됨
        verify(redisTemplate, atLeastOnce()).opsForValue();
    }

    @Test
    @DisplayName("워밍 실패 처리")
    void testWarmWithException() {
        // Given
        cacheWarmer.register("failCache", () -> {
            throw new RuntimeException("Data load failed");
        });

        // When
        CacheWarmer.WarmupResult result = cacheWarmer.warmAll();

        // Then
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailedCaches()).contains("failCache");
    }

    @Test
    @DisplayName("빈 데이터 워밍")
    void testWarmWithEmptyData() {
        // Given
        cacheWarmer.register("emptyCache", List::of);

        // When
        CacheWarmer.WarmupResult result = cacheWarmer.warmAll();

        // Then - 빈 데이터는 skip
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("NULL 데이터 워밍")
    void testWarmWithNullData() {
        // Given
        cacheWarmer.register("nullCache", () -> null);

        // When
        CacheWarmer.WarmupResult result = cacheWarmer.warmAll();

        // Then - NULL 데이터는 skip
        verify(redisTemplate, never()).opsForValue();
    }

    /**
     * 테스트용 엔티티
     */
    static class TestEntity {
        private Long id;
        private String data;

        public TestEntity(Long id, String data) {
            this.id = id;
            this.data = data;
        }

        public Long getId() {
            return id;
        }

        public String getData() {
            return data;
        }
    }
}
