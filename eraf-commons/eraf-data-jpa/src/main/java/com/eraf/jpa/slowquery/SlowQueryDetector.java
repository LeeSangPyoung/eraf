package com.eraf.jpa.slowquery;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Hibernate StatementInspector for detecting slow queries
 * SQL 실행 시간을 측정하고 임계값을 초과하면 경고 로그 출력
 */
public class SlowQueryDetector implements StatementInspector {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryDetector.class);

    private final SlowQueryProperties properties;
    private final ThreadLocal<QueryExecutionContext> contextHolder = new ThreadLocal<>();
    private final ConcurrentHashMap<String, Long> slowQueryStats = new ConcurrentHashMap<>();
    private final List<Consumer<SlowQueryEvent>> alertListeners = new CopyOnWriteArrayList<>();
    private volatile DataSource dataSource;

    public SlowQueryDetector(SlowQueryProperties properties) {
        this.properties = properties;
    }

    /**
     * DataSource 설정 (EXPLAIN 실행용)
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 슬로우 쿼리 알림 리스너 등록
     */
    public void addAlertListener(Consumer<SlowQueryEvent> listener) {
        alertListeners.add(listener);
    }

    @Override
    public String inspect(String sql) {
        if (!properties.isEnabled()) {
            return sql;
        }

        // SQL 실행 시작 시간 기록
        QueryExecutionContext context = new QueryExecutionContext(sql, System.currentTimeMillis());
        contextHolder.set(context);

        // Hook을 통해 실행 완료 시점을 캡처하기 위한 Wrapper
        // 주의: StatementInspector는 실행 전에만 호출되므로, 실제 측정은 제한적
        // 더 정확한 측정을 위해서는 ConnectionProvider나 DataSource 레벨 필요

        return sql;
    }

    /**
     * 쿼리 실행 완료 시 호출 (외부에서 명시적으로 호출 필요)
     */
    public void recordQueryExecution(String sql, long executionTimeMs) {
        if (!properties.isEnabled()) {
            return;
        }

        if (executionTimeMs >= properties.getThresholdMs()) {
            recordSlowQuery(sql, executionTimeMs);
        }
    }

    /**
     * Slow Query 로깅 및 통계 기록
     */
    private void recordSlowQuery(String sql, long executionTimeMs) {
        // 통계 업데이트
        String normalizedSql = normalizeSql(sql);
        slowQueryStats.merge(normalizedSql, executionTimeMs, Long::max);

        // 로깅 레벨 결정
        boolean isCritical = executionTimeMs >= properties.getCriticalThresholdMs();
        boolean logAsError = properties.isLogAsError() || isCritical;

        // EXPLAIN 실행 (SELECT 쿼리만)
        List<String> explainPlan = executeExplain(sql);

        StringBuilder logMessage = new StringBuilder();
        logMessage.append(String.format("[SLOW QUERY] Execution time: %dms%s\n",
                executionTimeMs,
                isCritical ? " (CRITICAL!)" : ""));
        logMessage.append("SQL: ").append(sql);

        if (!explainPlan.isEmpty()) {
            logMessage.append("\nEXPLAIN Plan:\n");
            for (String line : explainPlan) {
                logMessage.append("  ").append(line).append("\n");
            }
        }

        if (properties.isLogStackTrace()) {
            logMessage.append("\nStack Trace:\n");
            logMessage.append(captureStackTrace(properties.getStackTraceDepth()));
        }

        if (logAsError) {
            log.error(logMessage.toString());
        } else {
            log.warn(logMessage.toString());
        }

        // 알림 리스너 호출
        if (!alertListeners.isEmpty()) {
            SlowQueryEvent event = new SlowQueryEvent(sql, executionTimeMs, isCritical, normalizedSql, explainPlan);
            for (Consumer<SlowQueryEvent> listener : alertListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    log.debug("Slow query alert listener failed", e);
                }
            }
        }
    }

    /**
     * EXPLAIN 쿼리 실행 (SELECT 쿼리만)
     */
    private List<String> executeExplain(String sql) {
        if (dataSource == null) {
            return List.of();
        }

        String trimmedSql = sql.trim().toLowerCase();
        if (!trimmedSql.startsWith("select")) {
            return List.of();
        }

        List<String> plan = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("EXPLAIN " + sql)) {
            while (rs.next()) {
                plan.add(rs.getString(1));
            }
        } catch (Exception e) {
            log.debug("EXPLAIN 실행 실패: {}", e.getMessage());
        }
        return plan;
    }

    /**
     * SQL 정규화 (파라미터 제거하여 동일 쿼리 그룹화)
     */
    private String normalizeSql(String sql) {
        // 숫자, 문자열 리터럴을 ? 로 치환
        return sql
                .replaceAll("'[^']*'", "?")
                .replaceAll("\\b\\d+\\b", "?")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 스택 트레이스 캡처
     */
    private String captureStackTrace(int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();

        int limit = depth > 0 ? Math.min(depth + 2, stackTrace.length) : stackTrace.length;
        for (int i = 2; i < limit; i++) {  // 2부터 시작 (getStackTrace, captureStackTrace 제외)
            StackTraceElement element = stackTrace[i];
            // Hibernate 내부 스택은 제외
            if (!element.getClassName().startsWith("org.hibernate") &&
                !element.getClassName().startsWith("java.lang.reflect")) {
                sb.append("  at ").append(element.toString()).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Slow Query 통계 조회
     */
    public ConcurrentHashMap<String, Long> getSlowQueryStats() {
        return new ConcurrentHashMap<>(slowQueryStats);
    }

    /**
     * 통계 초기화
     */
    public void clearStats() {
        slowQueryStats.clear();
    }

    /**
     * 쿼리 실행 컨텍스트
     */
    private static class QueryExecutionContext {
        private final String sql;
        private final long startTime;

        public QueryExecutionContext(String sql, long startTime) {
            this.sql = sql;
            this.startTime = startTime;
        }

        public String getSql() {
            return sql;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getElapsedTimeMs() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
