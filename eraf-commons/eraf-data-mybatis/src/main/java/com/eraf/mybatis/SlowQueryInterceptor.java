package com.eraf.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Properties;

/**
 * MyBatis 슬로우 쿼리 감지 인터셉터
 *
 * <p>MapperID와 SQL을 함께 로깅하여 JPA/JDBC 레벨보다
 * 더 구체적인 슬로우 쿼리 정보를 제공합니다.
 *
 * <p>설정 (application.yml):
 * <pre>
 * eraf:
 *   mybatis:
 *     slow-query:
 *       enabled: true
 *       threshold-ms: 1000
 *       critical-threshold-ms: 3000
 * </pre>
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class})
})
public class SlowQueryInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryInterceptor.class);

    private final long thresholdMs;
    private final long criticalThresholdMs;
    private final boolean logSql;
    private final ApplicationEventPublisher eventPublisher;

    public SlowQueryInterceptor(long thresholdMs, long criticalThresholdMs, boolean logSql,
                                 ApplicationEventPublisher eventPublisher) {
        this.thresholdMs = thresholdMs;
        this.criticalThresholdMs = criticalThresholdMs;
        this.logSql = logSql;
        this.eventPublisher = eventPublisher;
    }

    public SlowQueryInterceptor(long thresholdMs, long criticalThresholdMs) {
        this(thresholdMs, criticalThresholdMs, true, null);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String mapperId = ms.getId();

        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= thresholdMs) {
                logSlowQuery(ms, elapsed, mapperId);
                if (eventPublisher != null) {
                    eventPublisher.publishEvent(new SlowQueryEvent(this, mapperId, elapsed, getSQL(ms, invocation)));
                }
            }
        }
    }

    private void logSlowQuery(MappedStatement ms, long elapsed, String mapperId) {
        boolean isCritical = elapsed >= criticalThresholdMs;

        StringBuilder sb = new StringBuilder();
        sb.append("[MYBATIS SLOW QUERY] ")
          .append(elapsed).append("ms")
          .append(isCritical ? " (CRITICAL!)" : "")
          .append(" | Mapper: ").append(mapperId)
          .append(" | Type: ").append(ms.getSqlCommandType());

        if (logSql) {
            sb.append(" | SQL: ").append(getSQL(ms, null));
        }

        if (isCritical) {
            log.error(sb.toString());
        } else {
            log.warn(sb.toString());
        }
    }

    private String getSQL(MappedStatement ms, Invocation invocation) {
        try {
            Object parameter = (invocation != null && invocation.getArgs().length > 1)
                    ? invocation.getArgs()[1] : null;
            BoundSql boundSql = ms.getBoundSql(parameter);
            return boundSql.getSql().replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "[SQL not available]";
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // Properties injection support (optional)
    }

    /**
     * MyBatis 슬로우 쿼리 이벤트
     */
    public static class SlowQueryEvent {
        private final Object source;
        private final String mapperId;
        private final long elapsedMs;
        private final String sql;

        public SlowQueryEvent(Object source, String mapperId, long elapsedMs, String sql) {
            this.source = source;
            this.mapperId = mapperId;
            this.elapsedMs = elapsedMs;
            this.sql = sql;
        }

        public Object getSource() { return source; }
        public String getMapperId() { return mapperId; }
        public long getElapsedMs() { return elapsedMs; }
        public String getSql() { return sql; }
    }
}
