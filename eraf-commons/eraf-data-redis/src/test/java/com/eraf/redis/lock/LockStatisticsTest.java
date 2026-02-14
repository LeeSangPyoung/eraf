package com.eraf.redis.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * LockStatistics 테스트
 */
class LockStatisticsTest {

    private LockStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new LockStatistics();
    }

    @Test
    @DisplayName("락 획득 기록")
    void testRecordAcquire() {
        // When
        statistics.recordAcquire("order:123", "owner-1", 50L);

        // Then
        Map<String, Object> stats = statistics.getGlobalStatistics();
        assertThat(stats.get("totalAcquired")).isEqualTo(1L);
        assertThat(stats.get("activeLockCount")).isEqualTo(1);
    }

    @Test
    @DisplayName("락 해제 기록")
    void testRecordRelease() {
        // Given
        statistics.recordAcquire("order:123", "owner-1", 10L);

        // When
        statistics.recordRelease("order:123");

        // Then
        Map<String, Object> stats = statistics.getGlobalStatistics();
        assertThat(stats.get("totalReleased")).isEqualTo(1L);
        assertThat(stats.get("activeLockCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("락 타임아웃 기록")
    void testRecordTimeout() {
        // When
        statistics.recordTimeout("order:456");

        // Then
        Map<String, Object> stats = statistics.getGlobalStatistics();
        assertThat(stats.get("totalTimeouts")).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공율 계산")
    void testSuccessRate() {
        // Given
        statistics.recordAcquire("key1", "owner-1", 10L);
        statistics.recordAcquire("key2", "owner-1", 20L);
        statistics.recordAcquire("key3", "owner-1", 30L);
        statistics.recordTimeout("key4");

        // When
        Map<String, Object> stats = statistics.getGlobalStatistics();

        // Then
        assertThat(stats.get("totalAttempts")).isEqualTo(4L);
        assertThat(stats.get("successRate")).isEqualTo(75.0);
    }

    @Test
    @DisplayName("평균 대기 시간 계산")
    void testAverageWaitTime() {
        // Given
        statistics.recordAcquire("key1", "owner-1", 100L);
        statistics.recordAcquire("key2", "owner-1", 200L);

        // When
        Map<String, Object> stats = statistics.getGlobalStatistics();

        // Then
        assertThat(stats.get("averageWaitTimeMs")).isEqualTo(150.0);
    }

    @Test
    @DisplayName("활성 락 목록 조회")
    void testGetActiveLocks() {
        // Given
        statistics.recordAcquire("order:1", "owner-a", 10L);
        statistics.recordAcquire("order:2", "owner-b", 20L);

        // When
        List<Map<String, Object>> activeLocks = statistics.getActiveLocks();

        // Then
        assertThat(activeLocks).hasSize(2);
        assertThat(activeLocks.stream().map(m -> m.get("key")))
                .containsExactlyInAnyOrder("order:1", "order:2");
    }

    @Test
    @DisplayName("활성 락 - 해제 후 제거")
    void testActiveLocksAfterRelease() {
        // Given
        statistics.recordAcquire("order:1", "owner-a", 10L);
        statistics.recordAcquire("order:2", "owner-b", 20L);
        statistics.recordRelease("order:1");

        // When
        List<Map<String, Object>> activeLocks = statistics.getActiveLocks();

        // Then
        assertThat(activeLocks).hasSize(1);
        assertThat(activeLocks.get(0).get("key")).isEqualTo("order:2");
    }

    @Test
    @DisplayName("키별 통계 조회")
    void testKeyStatistics() {
        // Given
        statistics.recordAcquire("order:lock", "owner-1", 50L);
        statistics.recordAcquire("order:lock", "owner-2", 100L);
        statistics.recordRelease("order:lock");
        statistics.recordTimeout("order:lock");

        // When
        Map<String, Object> keyStats = statistics.getKeyStatistics("order:lock");

        // Then
        assertThat(keyStats.get("key")).isEqualTo("order:lock");
        assertThat(keyStats.get("acquireCount")).isEqualTo(2L);
        assertThat(keyStats.get("timeoutCount")).isEqualTo(1L);
        assertThat(keyStats.get("averageWaitTimeMs")).isEqualTo(75.0);
    }

    @Test
    @DisplayName("존재하지 않는 키 통계 조회")
    void testNonExistentKeyStatistics() {
        Map<String, Object> stats = statistics.getKeyStatistics("nonexistent");
        assertThat(stats.get("exists")).isEqualTo(false);
    }

    @Test
    @DisplayName("통계 초기화")
    void testReset() {
        // Given
        statistics.recordAcquire("key1", "owner-1", 10L);
        statistics.recordTimeout("key2");

        // When
        statistics.reset();

        // Then
        Map<String, Object> stats = statistics.getGlobalStatistics();
        assertThat(stats.get("totalAcquired")).isEqualTo(0L);
        assertThat(stats.get("totalTimeouts")).isEqualTo(0L);
        assertThat(stats.get("trackedKeyCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("최대 대기/보유 시간 추적")
    void testMaxTimes() {
        // Given
        statistics.recordAcquire("key", "owner-1", 50L);
        statistics.recordRelease("key");
        statistics.recordAcquire("key", "owner-1", 200L);
        statistics.recordRelease("key");

        // When
        Map<String, Object> keyStats = statistics.getKeyStatistics("key");

        // Then
        assertThat(keyStats.get("maxWaitTimeMs")).isEqualTo(200L);
    }
}
