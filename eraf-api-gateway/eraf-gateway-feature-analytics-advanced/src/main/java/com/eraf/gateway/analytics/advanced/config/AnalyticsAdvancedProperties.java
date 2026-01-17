package com.eraf.gateway.analytics.advanced.config;

import com.eraf.gateway.analytics.advanced.domain.TimeSeriesMetric;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Analytics 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.analytics-advanced")
public class AnalyticsAdvancedProperties {

    /**
     * 기능 활성화 여부
     */
    private boolean enabled = false;

    /**
     * 실시간 대시보드 활성화
     */
    private boolean realTimeDashboard = true;

    /**
     * 비동기 기록 여부
     */
    private boolean asyncRecording = true;

    /**
     * 배치 크기 (비동기 기록 시)
     */
    private int batchSize = 100;

    /**
     * 비동기 처리 스레드 수
     */
    private int asyncThreads = 2;

    /**
     * 인메모리 저장소 최대 크기
     */
    private int maxStorageSize = 10000;

    /**
     * 집계 간격 (초)
     */
    private List<TimeSeriesMetric.AggregationWindow> aggregationIntervals = new ArrayList<>(List.of(
            TimeSeriesMetric.AggregationWindow.ONE_MINUTE,
            TimeSeriesMetric.AggregationWindow.FIVE_MINUTES,
            TimeSeriesMetric.AggregationWindow.ONE_HOUR
    ));

    /**
     * 보존 정책
     */
    private RetentionPolicy retentionPolicy = new RetentionPolicy();

    /**
     * Prometheus 익스포트
     */
    private PrometheusExport prometheus = new PrometheusExport();

    /**
     * Datadog 익스포트
     */
    private DatadogExport datadog = new DatadogExport();

    /**
     * Elasticsearch 익스포트
     */
    private ElasticsearchExport elasticsearch = new ElasticsearchExport();

    @Data
    public static class RetentionPolicy {
        /**
         * 원본 데이터 보존 기간 (시간)
         */
        private int rawDataRetentionHours = 24;

        /**
         * 1분 집계 데이터 보존 기간 (일)
         */
        private int oneMinuteRetentionDays = 7;

        /**
         * 5분 집계 데이터 보존 기간 (일)
         */
        private int fiveMinuteRetentionDays = 30;

        /**
         * 1시간 집계 데이터 보존 기간 (일)
         */
        private int oneHourRetentionDays = 90;

        /**
         * 1일 집계 데이터 보존 기간 (일)
         */
        private int oneDayRetentionDays = 365;
    }

    @Data
    public static class PrometheusExport {
        /**
         * Prometheus 익스포트 활성화
         */
        private boolean enabled = false;

        /**
         * 메트릭 prefix
         */
        private String metricsPrefix = "eraf_gateway";
    }

    @Data
    public static class DatadogExport {
        /**
         * Datadog 익스포트 활성화
         */
        private boolean enabled = false;

        /**
         * Datadog Agent 호스트
         */
        private String host = "localhost";

        /**
         * Datadog Agent 포트 (StatsD)
         */
        private int port = 8125;

        /**
         * 메트릭 prefix
         */
        private String metricsPrefix = "eraf.gateway";

        /**
         * 환경 태그
         */
        private String environment = "production";
    }

    @Data
    public static class ElasticsearchExport {
        /**
         * Elasticsearch 익스포트 활성화
         */
        private boolean enabled = false;

        /**
         * Elasticsearch 호스트
         */
        private String host = "localhost";

        /**
         * Elasticsearch 포트
         */
        private int port = 9200;

        /**
         * 인덱스 prefix
         */
        private String indexPrefix = "eraf-gateway";

        /**
         * 벌크 배치 크기
         */
        private int bulkBatchSize = 1000;
    }
}
