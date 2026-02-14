package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API 메트릭 수집
 * API 호출 횟수, 응답 시간, 에러율, 요청/응답 크기
 */
public class ApiMetrics {

    private final MeterRegistry registry;

    public ApiMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * API 호출 시작
     */
    public ApiCallTimer start(String endpoint, String method) {
        return new ApiCallTimer(registry, endpoint, method);
    }

    /**
     * API 호출 카운트 증가
     */
    public void recordCall(String endpoint, String method, int statusCode) {
        Counter.builder("api.calls")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .tag("status.series", getStatusSeries(statusCode))
                .register(registry)
                .increment();
    }

    /**
     * API 응답 시간 기록
     */
    public void recordResponseTime(String endpoint, String method, long durationMs) {
        Timer.builder("api.response.time")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(100),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(3)
                )
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 요청 크기 기록
     */
    public void recordRequestSize(String endpoint, String method, long bytes) {
        DistributionSummary.builder("api.request.size")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(bytes);
    }

    /**
     * 응답 크기 기록
     */
    public void recordResponseSize(String endpoint, String method, long bytes) {
        DistributionSummary.builder("api.response.size")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(bytes);
    }

    /**
     * 에러 발생 기록
     */
    public void recordError(String endpoint, String method, String errorType) {
        Counter.builder("api.errors")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("error.type", errorType)
                .register(registry)
                .increment();
    }

    /**
     * 성공/실패 기록
     */
    public void recordOutcome(String endpoint, String method, boolean success) {
        Counter.builder("api.outcomes")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("outcome", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

    /**
     * 동시 요청 수 증가
     */
    public void incrementConcurrentRequests(String endpoint) {
        Counter.builder("api.concurrent.requests")
                .tag("endpoint", endpoint)
                .register(registry)
                .increment();
    }

    /**
     * 동시 요청 수 감소
     */
    public void decrementConcurrentRequests(String endpoint) {
        Counter.builder("api.concurrent.requests")
                .tag("endpoint", endpoint)
                .register(registry)
                .increment(-1);
    }

    /**
     * HTTP Status Code Series 반환 (1xx, 2xx, 3xx, 4xx, 5xx)
     */
    private String getStatusSeries(int statusCode) {
        if (statusCode >= 100 && statusCode < 200) return "1xx";
        if (statusCode >= 200 && statusCode < 300) return "2xx";
        if (statusCode >= 300 && statusCode < 400) return "3xx";
        if (statusCode >= 400 && statusCode < 500) return "4xx";
        if (statusCode >= 500 && statusCode < 600) return "5xx";
        return "unknown";
    }

    /**
     * API 호출 타이머 헬퍼
     */
    public static class ApiCallTimer {
        private final MeterRegistry registry;
        private final String endpoint;
        private final String method;
        private final Timer.Sample sample;

        private ApiCallTimer(MeterRegistry registry, String endpoint, String method) {
            this.registry = registry;
            this.endpoint = endpoint;
            this.method = method;
            this.sample = Timer.start(registry);
        }

        /**
         * API 호출 완료
         */
        public long stop(int statusCode) {
            // 응답 시간 기록
            long duration = sample.stop(Timer.builder("api.response.time")
                    .tag("endpoint", endpoint)
                    .tag("method", method)
                    .tag("status", String.valueOf(statusCode))
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .register(registry));

            // 호출 카운트
            Counter.builder("api.calls")
                    .tag("endpoint", endpoint)
                    .tag("method", method)
                    .tag("status", String.valueOf(statusCode))
                    .register(registry)
                    .increment();

            return duration;
        }

        /**
         * API 호출 실패
         */
        public long stopWithError(String errorType) {
            long duration = sample.stop(Timer.builder("api.response.time")
                    .tag("endpoint", endpoint)
                    .tag("method", method)
                    .tag("status", "error")
                    .register(registry));

            // 에러 카운트
            Counter.builder("api.errors")
                    .tag("endpoint", endpoint)
                    .tag("method", method)
                    .tag("error.type", errorType)
                    .register(registry)
                    .increment();

            return duration;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getMethod() {
            return method;
        }
    }

    /**
     * SLA 위반 체크
     */
    public static boolean isSlaViolation(long durationMs, long slaMs) {
        return durationMs > slaMs;
    }

    /**
     * Slow API 체크 (기본: 1초)
     */
    public static boolean isSlowApi(long durationMs) {
        return isSlaViolation(durationMs, 1000);
    }

    /**
     * Very Slow API 체크 (기본: 3초)
     */
    public static boolean isVerySlowApi(long durationMs) {
        return isSlaViolation(durationMs, 3000);
    }
}
