package com.eraf.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trace Exporter 설정 프로퍼티
 *
 * Zipkin, OTLP, NONE 등 trace exporter 유형과 설정을 관리합니다.
 * 실제 exporter 구현은 Micrometer/OpenTelemetry 의존성에 의해 제공됩니다.
 */
@ConfigurationProperties(prefix = "eraf.observability.trace-exporter")
public class TraceExporterProperties {

    /**
     * Exporter 유형
     */
    private ExporterType type = ExporterType.NONE;

    /**
     * Exporter 엔드포인트 URL
     * - ZIPKIN: http://localhost:9411/api/v2/spans
     * - OTLP: http://localhost:4317
     */
    private String endpoint;

    /**
     * 샘플링 비율 (0.0 ~ 1.0)
     * 0.0 = 샘플링 안함, 1.0 = 모두 샘플링
     */
    private double sampleRate = 1.0;

    /**
     * 내보내기 타임아웃 (milliseconds)
     */
    private long timeoutMs = 10000;

    /**
     * 배치 내보내기 간격 (milliseconds)
     */
    private long scheduleDelayMs = 5000;

    /**
     * 최대 배치 크기
     */
    private int maxBatchSize = 512;

    public ExporterType getType() {
        return type;
    }

    public void setType(ExporterType type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public long getScheduleDelayMs() {
        return scheduleDelayMs;
    }

    public void setScheduleDelayMs(long scheduleDelayMs) {
        this.scheduleDelayMs = scheduleDelayMs;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * Trace Exporter 유형
     */
    public enum ExporterType {
        /** Zipkin exporter */
        ZIPKIN,
        /** OpenTelemetry Protocol exporter */
        OTLP,
        /** 비활성화 */
        NONE
    }
}
