package com.eraf.gateway.analytics.advanced.metrics;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Error Rate 메트릭
 * HTTP 4xx, 5xx 에러율 추적
 */
@Getter
@Builder
public class ErrorRateMetrics {

    private final String path;
    private final String method;

    // Overall metrics
    private final long totalRequests;
    private final long successCount;      // 2xx
    private final long clientErrorCount;  // 4xx
    private final long serverErrorCount;  // 5xx

    // Error rates (percentage)
    private final double errorRate;        // (4xx + 5xx) / total
    private final double clientErrorRate;  // 4xx / total
    private final double serverErrorRate;  // 5xx / total
    private final double successRate;      // 2xx / total

    // Detailed status code breakdown
    private final Map<Integer, Long> statusCodeDistribution;

    // Top errors by endpoint
    private final Map<String, Long> errorsByEndpoint;

    // Top error codes
    private final Map<String, Long> errorCodeDistribution;  // Application error codes

    private final long timestamp;
    private final long windowSeconds;  // Time window for this metric

    /**
     * 에러율 계산
     */
    public static double calculateErrorRate(long errorCount, long totalCount) {
        if (totalCount == 0) return 0.0;
        return (errorCount * 100.0) / totalCount;
    }

    /**
     * 에러율이 임계값을 초과하는지 확인
     */
    public boolean isErrorRateAboveThreshold(double threshold) {
        return errorRate > threshold;
    }
}
