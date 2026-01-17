package com.eraf.gateway.analytics.advanced.metrics;

import lombok.Builder;
import lombok.Getter;

/**
 * Latency Percentile 메트릭
 * Kong Vitals / AWS API Gateway 스타일의 레이턴시 분포
 */
@Getter
@Builder
public class LatencyPercentiles {

    private final String path;
    private final String method;

    // Percentile values in milliseconds
    private final double p50;    // Median
    private final double p75;
    private final double p95;
    private final double p99;
    private final double p999;   // 99.9th percentile

    // Additional statistics
    private final double min;
    private final double max;
    private final double mean;
    private final double stdDev;

    private final long sampleCount;
    private final long timestamp;

    /**
     * 백분위수 타입
     */
    public enum Percentile {
        P50(0.50),
        P75(0.75),
        P90(0.90),
        P95(0.95),
        P99(0.99),
        P999(0.999);

        private final double value;

        Percentile(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }
}
