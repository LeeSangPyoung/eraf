package com.eraf.jpa.slowquery;

import java.time.Instant;
import java.util.List;

/**
 * Slow Query 이벤트 (알림 리스너에 전달)
 */
public class SlowQueryEvent {

    private final String sql;
    private final long executionTimeMs;
    private final boolean critical;
    private final String normalizedSql;
    private final List<String> explainPlan;
    private final Instant detectedAt;

    public SlowQueryEvent(String sql, long executionTimeMs, boolean critical,
                          String normalizedSql, List<String> explainPlan) {
        this.sql = sql;
        this.executionTimeMs = executionTimeMs;
        this.critical = critical;
        this.normalizedSql = normalizedSql;
        this.explainPlan = explainPlan;
        this.detectedAt = Instant.now();
    }

    public String getSql() {
        return sql;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public boolean isCritical() {
        return critical;
    }

    public String getNormalizedSql() {
        return normalizedSql;
    }

    public List<String> getExplainPlan() {
        return explainPlan;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    @Override
    public String toString() {
        return String.format("SlowQueryEvent{sql='%s', time=%dms, critical=%s}",
                normalizedSql, executionTimeMs, critical);
    }
}
