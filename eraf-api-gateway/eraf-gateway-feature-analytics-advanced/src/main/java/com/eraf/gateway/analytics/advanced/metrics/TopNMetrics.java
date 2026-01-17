package com.eraf.gateway.analytics.advanced.metrics;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Top N 메트릭
 * 상위 소비자, API, 에러 등 추적
 */
@Getter
@Builder
public class TopNMetrics {

    // Top consumers (by request count)
    private final List<ConsumerMetric> topConsumers;

    // Top APIs (by request count)
    private final List<ApiMetric> topApis;

    // Top errors (by occurrence)
    private final List<ErrorMetric> topErrors;

    // Slowest endpoints
    private final List<SlowEndpointMetric> slowestEndpoints;

    // Top traffic sources (by IP)
    private final Map<String, Long> topTrafficSources;

    private final long timestamp;
    private final long windowSeconds;

    /**
     * Consumer 메트릭
     */
    @Getter
    @Builder
    public static class ConsumerMetric {
        private final String consumerIdentifier;  // API Key or JWT subject
        private final String authMethod;
        private final long requestCount;
        private final long errorCount;
        private final double errorRate;
        private final double avgLatencyMs;
        private final long totalBytesTransferred;
    }

    /**
     * API 메트릭
     */
    @Getter
    @Builder
    public static class ApiMetric {
        private final String path;
        private final String method;
        private final long requestCount;
        private final double requestsPerSecond;
        private final double avgLatencyMs;
        private final double p95LatencyMs;
        private final double errorRate;
    }

    /**
     * Error 메트릭
     */
    @Getter
    @Builder
    public static class ErrorMetric {
        private final String errorCode;
        private final String errorMessage;
        private final int statusCode;
        private final long occurrenceCount;
        private final String topAffectedPath;
        private final String firstOccurrence;
        private final String lastOccurrence;
    }

    /**
     * Slow Endpoint 메트릭
     */
    @Getter
    @Builder
    public static class SlowEndpointMetric {
        private final String path;
        private final String method;
        private final double avgLatencyMs;
        private final double p95LatencyMs;
        private final double p99LatencyMs;
        private final long requestCount;
        private final long slowRequestCount;  // Requests above threshold
    }
}
