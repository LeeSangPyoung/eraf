package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 데이터베이스 메트릭 수집
 * HikariCP 연결 풀 통계 및 쿼리 실행 시간 측정
 *
 * HikariCP는 optional dependency이므로 Reflection 사용
 */
public class DatabaseMetrics {

    private final Object dataSource;
    private final String dataSourceName;
    private final MeterRegistry registry;

    public DatabaseMetrics(Object dataSource, MeterRegistry registry) {
        this(dataSource, "default", registry);
    }

    public DatabaseMetrics(Object dataSource, String dataSourceName, MeterRegistry registry) {
        this.dataSource = dataSource;
        this.dataSourceName = dataSourceName;
        this.registry = registry;
    }

    /**
     * 메트릭 바인딩 시작
     */
    public void bind() {
        if (isHikariDataSource(dataSource)) {
            bindHikariMetrics();
        }
    }

    /**
     * HikariDataSource 확인 (Reflection)
     */
    private boolean isHikariDataSource(Object ds) {
        return ds != null && ds.getClass().getName().equals("com.zaxxer.hikari.HikariDataSource");
    }

    /**
     * HikariCP 메트릭 등록 (Reflection 사용)
     */
    private void bindHikariMetrics() {
        try {
            Class<?> hikariClass = dataSource.getClass();
            Method getPoolMXBean = hikariClass.getMethod("getHikariPoolMXBean");
            Object poolMXBean = getPoolMXBean.invoke(dataSource);

            if (poolMXBean == null) {
                return;
            }

            Class<?> poolMXBeanClass = poolMXBean.getClass();

            // 활성 연결 수
            bindGauge("db.pool.active", poolMXBean, poolMXBeanClass, "getActiveConnections",
                    "Active database connections");

            // 유휴 연결 수
            bindGauge("db.pool.idle", poolMXBean, poolMXBeanClass, "getIdleConnections",
                    "Idle database connections");

            // 전체 연결 수
            bindGauge("db.pool.total", poolMXBean, poolMXBeanClass, "getTotalConnections",
                    "Total database connections");

            // 대기 중인 스레드 수
            bindGauge("db.pool.pending", poolMXBean, poolMXBeanClass, "getThreadsAwaitingConnection",
                    "Threads awaiting database connection");

            // 최대 연결 수
            Method getMaxPoolSize = hikariClass.getMethod("getMaximumPoolSize");
            Gauge.builder("db.pool.max", dataSource, ds -> {
                try {
                    return ((Number) getMaxPoolSize.invoke(ds)).doubleValue();
                } catch (Exception e) {
                    return 0.0;
                }
            })
                    .tag("pool", dataSourceName)
                    .description("Maximum database connections")
                    .register(registry);

            // 최소 유휴 연결 수
            Method getMinIdle = hikariClass.getMethod("getMinimumIdle");
            Gauge.builder("db.pool.min.idle", dataSource, ds -> {
                try {
                    return ((Number) getMinIdle.invoke(ds)).doubleValue();
                } catch (Exception e) {
                    return 0.0;
                }
            })
                    .tag("pool", dataSourceName)
                    .description("Minimum idle database connections")
                    .register(registry);

        } catch (Exception e) {
            // HikariCP가 없거나 메서드가 변경된 경우 무시
        }
    }

    /**
     * Gauge 등록 헬퍼 (Reflection)
     */
    private void bindGauge(String name, Object target, Class<?> targetClass,
                           String methodName, String description) {
        try {
            Method method = targetClass.getMethod(methodName);
            Gauge.builder(name, target, obj -> {
                try {
                    return ((Number) method.invoke(obj)).doubleValue();
                } catch (Exception e) {
                    return 0.0;
                }
            })
                    .tag("pool", dataSourceName)
                    .description(description)
                    .register(registry);
        } catch (Exception e) {
            // 메서드가 없거나 접근할 수 없는 경우 무시
        }
    }

    /**
     * 쿼리 실행 시간 측정 샘플 생성
     */
    public static QueryTimer startQuery(MeterRegistry registry, String queryType) {
        return new QueryTimer(registry, queryType);
    }

    /**
     * 쿼리 타이머 헬퍼 클래스
     */
    public static class QueryTimer {
        private final MeterRegistry registry;
        private final String queryType;
        private final Timer.Sample sample;

        private QueryTimer(MeterRegistry registry, String queryType) {
            this.registry = registry;
            this.queryType = queryType;
            this.sample = Timer.start(registry);
        }

        /**
         * 쿼리 실행 완료 (성공)
         */
        public long stop() {
            return stop("success");
        }

        /**
         * 쿼리 실행 완료 (결과 지정)
         */
        public long stop(String result) {
            return sample.stop(Timer.builder("db.query.time")
                    .tag("type", queryType)
                    .tag("result", result)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry));
        }

        /**
         * 쿼리 실행 실패
         */
        public long stopWithError(String errorType) {
            return sample.stop(Timer.builder("db.query.time")
                    .tag("type", queryType)
                    .tag("result", "error")
                    .tag("error", errorType)
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(registry));
        }
    }

    /**
     * 쿼리 타입 상수
     */
    public static final class QueryType {
        private QueryType() {}

        public static final String SELECT = "SELECT";
        public static final String INSERT = "INSERT";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String BATCH = "BATCH";
        public static final String DDL = "DDL";
        public static final String PROCEDURE = "PROCEDURE";
    }

    /**
     * Slow Query 임계값 (밀리초)
     */
    public static final long SLOW_QUERY_THRESHOLD_MS = 1000;

    /**
     * Slow Query 검사
     */
    public static boolean isSlowQuery(long durationNanos) {
        return TimeUnit.NANOSECONDS.toMillis(durationNanos) >= SLOW_QUERY_THRESHOLD_MS;
    }
}
