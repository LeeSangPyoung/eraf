package com.eraf.gateway.analytics.advanced.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 시계열 메트릭 데이터
 * 시간별 집계를 위한 메트릭 저장 구조
 */
@Getter
@Builder
public class TimeSeriesMetric {

    private final String id;
    private final String metricName;           // latency_p95, error_rate, throughput_rps, etc.
    private final double value;
    private final String unit;                 // ms, percent, rps, bps, count

    private final LocalDateTime timestamp;
    private final AggregationWindow window;    // 1min, 5min, 1hour, 1day

    // Dimensions for filtering/grouping
    private final String path;
    private final String method;
    private final String region;
    private final String clientType;
    private final Map<String, String> dimensions;

    /**
     * 집계 윈도우
     */
    public enum AggregationWindow {
        ONE_MINUTE("1min", 60),
        FIVE_MINUTES("5min", 300),
        ONE_HOUR("1hour", 3600),
        ONE_DAY("1day", 86400);

        private final String label;
        private final int seconds;

        AggregationWindow(String label, int seconds) {
            this.label = label;
            this.seconds = seconds;
        }

        public String getLabel() {
            return label;
        }

        public int getSeconds() {
            return seconds;
        }
    }
}
