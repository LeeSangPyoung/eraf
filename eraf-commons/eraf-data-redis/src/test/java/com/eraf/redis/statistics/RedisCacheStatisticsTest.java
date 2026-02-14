package com.eraf.redis.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RedisCacheStatistics 테스트
 */
class RedisCacheStatisticsTest {

    private RedisCacheStatistics statistics;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        statistics = new RedisCacheStatistics(redisTemplate);
    }

    @Test
    @DisplayName("캐시 히트 기록")
    void testRecordHit() {
        // When
        statistics.recordHit("users");
        statistics.recordHit("users");

        // Then
        Map<String, Object> stats = statistics.getCacheStatistics("users");
        assertThat(stats.get("hits")).isEqualTo(2L);
        assertThat(stats.get("misses")).isEqualTo(0L);
    }

    @Test
    @DisplayName("캐시 미스 기록")
    void testRecordMiss() {
        // When
        statistics.recordMiss("products");

        // Then
        Map<String, Object> stats = statistics.getCacheStatistics("products");
        assertThat(stats.get("hits")).isEqualTo(0L);
        assertThat(stats.get("misses")).isEqualTo(1L);
    }

    @Test
    @DisplayName("히트율 계산")
    void testHitRate() {
        // Given
        statistics.recordHit("orders");
        statistics.recordHit("orders");
        statistics.recordHit("orders");
        statistics.recordMiss("orders");

        // When
        Map<String, Object> stats = statistics.getCacheStatistics("orders");

        // Then
        assertThat(stats.get("totalRequests")).isEqualTo(4L);
        assertThat(stats.get("hitRate")).isEqualTo(75.0);
        assertThat(stats.get("missRate")).isEqualTo(25.0);
    }

    @Test
    @DisplayName("전역 통계 조회")
    void testGlobalStatistics() {
        // Given
        statistics.recordHit("cache1");
        statistics.recordHit("cache2");
        statistics.recordMiss("cache1");

        // When
        Map<String, Object> global = statistics.getGlobalStatistics();

        // Then
        assertThat(global.get("totalHits")).isEqualTo(2L);
        assertThat(global.get("totalMisses")).isEqualTo(1L);
        assertThat(global.get("totalRequests")).isEqualTo(3L);
        assertThat(global.get("cacheCount")).isEqualTo(2);
    }

    @Test
    @DisplayName("캐시 저장 및 크기 추적")
    void testRecordPut() {
        // When
        statistics.recordPut("images", 1024L);
        statistics.recordPut("images", 2048L);

        // Then
        Map<String, Object> stats = statistics.getCacheStatistics("images");
        assertThat(stats.get("puts")).isEqualTo(2L);
        assertThat(stats.get("totalSizeBytes")).isEqualTo(3072L);
    }

    @Test
    @DisplayName("응답 시간 추적")
    void testRecordLatency() {
        // When
        statistics.recordLatency("api", 100L);
        statistics.recordLatency("api", 200L);
        statistics.recordLatency("api", 300L);

        // Then
        Map<String, Object> stats = statistics.getCacheStatistics("api");
        assertThat(stats.get("averageLatencyMs")).isEqualTo(200.0);
    }

    @Test
    @DisplayName("통계 초기화")
    void testReset() {
        // Given
        statistics.recordHit("test");
        statistics.recordMiss("test");

        // When
        statistics.reset();

        // Then
        Map<String, Object> global = statistics.getGlobalStatistics();
        assertThat(global.get("totalHits")).isEqualTo(0L);
        assertThat(global.get("totalMisses")).isEqualTo(0L);
        assertThat(global.get("cacheCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 캐시 통계 초기화")
    void testResetCache() {
        // Given
        statistics.recordHit("cache1");
        statistics.recordHit("cache2");

        // When
        statistics.resetCache("cache1");

        // Then
        Map<String, Object> cache1Stats = statistics.getCacheStatistics("cache1");
        Map<String, Object> cache2Stats = statistics.getCacheStatistics("cache2");

        assertThat(cache1Stats.get("exists")).isEqualTo(false);
        assertThat(cache2Stats.get("hits")).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 캐시 조회")
    void testGetNonExistentCache() {
        // When
        Map<String, Object> stats = statistics.getCacheStatistics("nonexistent");

        // Then
        assertThat(stats.get("exists")).isEqualTo(false);
    }

    @Test
    @DisplayName("모든 캐시 통계 조회")
    void testGetAllCacheStatistics() {
        // Given
        statistics.recordHit("cache1");
        statistics.recordMiss("cache2");
        statistics.recordHit("cache3");

        // When
        Map<String, Map<String, Object>> allStats = statistics.getAllCacheStatistics();

        // Then
        assertThat(allStats).hasSize(3);
        assertThat(allStats).containsKeys("cache1", "cache2", "cache3");
    }
}
