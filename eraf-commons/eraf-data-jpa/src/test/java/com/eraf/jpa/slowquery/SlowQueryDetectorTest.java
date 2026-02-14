package com.eraf.jpa.slowquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SlowQueryDetector 테스트
 */
class SlowQueryDetectorTest {

    private SlowQueryProperties properties;
    private SlowQueryDetector detector;

    @BeforeEach
    void setUp() {
        properties = new SlowQueryProperties();
        properties.setEnabled(true);
        properties.setThresholdMs(100);
        properties.setCriticalThresholdMs(500);

        detector = new SlowQueryDetector(properties);
    }

    @Test
    void inspect_WhenDisabled_ShouldReturnOriginalSql() {
        // given
        properties.setEnabled(false);
        SlowQueryDetector disabledDetector = new SlowQueryDetector(properties);
        String sql = "SELECT * FROM users";

        // when
        String result = disabledDetector.inspect(sql);

        // then
        assertThat(result).isEqualTo(sql);
    }

    @Test
    void inspect_WhenEnabled_ShouldReturnSql() {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        String result = detector.inspect(sql);

        // then
        assertThat(result).isEqualTo(sql);
    }

    @Test
    void recordQueryExecution_AboveThreshold_ShouldRecordSlowQuery() {
        // given
        String sql = "SELECT * FROM users";

        // when
        detector.recordQueryExecution(sql, 150);  // 150ms > threshold(100ms)

        // then - 통계에 기록되어야 함
        Map<String, Long> stats = detector.getSlowQueryStats();
        assertThat(stats).isNotEmpty();
    }

    @Test
    void recordQueryExecution_BelowThreshold_ShouldNotRecord() {
        // given
        String sql = "SELECT * FROM users";

        // when
        detector.recordQueryExecution(sql, 50);  // 50ms < threshold(100ms)

        // then - 통계에 기록되지 않아야 함
        Map<String, Long> stats = detector.getSlowQueryStats();
        assertThat(stats).isEmpty();
    }

    @Test
    void recordQueryExecution_WhenDisabled_ShouldNotRecord() {
        // given
        properties.setEnabled(false);
        SlowQueryDetector disabledDetector = new SlowQueryDetector(properties);
        String sql = "SELECT * FROM users";

        // when
        disabledDetector.recordQueryExecution(sql, 1000);

        // then
        Map<String, Long> stats = disabledDetector.getSlowQueryStats();
        assertThat(stats).isEmpty();
    }

    @Test
    void getSlowQueryStats_ShouldReturnCopyOfStats() {
        // given
        String sql1 = "SELECT * FROM users";
        String sql2 = "SELECT * FROM orders";

        detector.recordQueryExecution(sql1, 150);
        detector.recordQueryExecution(sql2, 200);

        // when
        Map<String, Long> stats = detector.getSlowQueryStats();

        // then
        assertThat(stats).hasSize(2);
        assertThat(stats).containsValue(150L);
        assertThat(stats).containsValue(200L);
    }

    @Test
    void clearStats_ShouldRemoveAllStats() {
        // given
        detector.recordQueryExecution("SELECT * FROM users", 150);
        detector.recordQueryExecution("SELECT * FROM orders", 200);

        // when
        detector.clearStats();

        // then
        Map<String, Long> stats = detector.getSlowQueryStats();
        assertThat(stats).isEmpty();
    }

    @Test
    void recordQueryExecution_SameQueryMultipleTimes_ShouldKeepMaxTime() {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        detector.recordQueryExecution(sql, 150);
        detector.recordQueryExecution(sql, 250);
        detector.recordQueryExecution(sql, 100);  // 낮은 값

        // then - 최대값이 유지되어야 함
        Map<String, Long> stats = detector.getSlowQueryStats();
        assertThat(stats).hasSize(1);

        // Normalized SQL은 파라미터가 제거되므로 동일한 키로 간주
        Long maxTime = stats.values().iterator().next();
        assertThat(maxTime).isEqualTo(250L);
    }
}
