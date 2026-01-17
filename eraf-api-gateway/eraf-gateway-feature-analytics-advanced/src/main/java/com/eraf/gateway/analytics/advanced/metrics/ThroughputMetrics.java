package com.eraf.gateway.analytics.advanced.metrics;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Throughput 메트릭
 * 처리량 (requests/sec, bytes/sec) 추적
 */
@Getter
@Builder
public class ThroughputMetrics {

    private final String path;
    private final String method;

    // Request throughput
    private final double requestsPerSecond;
    private final double requestsPerMinute;
    private final long totalRequests;

    // Data throughput
    private final double bytesPerSecond;
    private final double megabytesPerSecond;
    private final long totalBytesIn;
    private final long totalBytesOut;

    // Peak metrics
    private final double peakRequestsPerSecond;
    private final double peakMegabytesPerSecond;
    private final long peakTimestamp;

    // Distribution by endpoint
    private final Map<String, Double> throughputByEndpoint;

    // Distribution by time window
    private final Map<String, Double> throughputByTimeWindow;  // HH:mm format

    private final long timestamp;
    private final long windowSeconds;

    /**
     * RPS 계산
     */
    public static double calculateRPS(long requestCount, long durationSeconds) {
        if (durationSeconds == 0) return 0.0;
        return (double) requestCount / durationSeconds;
    }

    /**
     * 대역폭 계산 (MB/s)
     */
    public static double calculateMBps(long bytes, long durationSeconds) {
        if (durationSeconds == 0) return 0.0;
        return (bytes / (1024.0 * 1024.0)) / durationSeconds;
    }

    /**
     * 현재 처리량이 용량을 초과하는지 확인
     */
    public boolean isOverCapacity(double maxRPS) {
        return requestsPerSecond > maxRPS;
    }
}
