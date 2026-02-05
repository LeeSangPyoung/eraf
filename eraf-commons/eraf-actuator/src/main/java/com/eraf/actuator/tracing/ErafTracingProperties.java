package com.eraf.actuator.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ERAF Tracing 설정
 */
@ConfigurationProperties(prefix = "eraf.actuator.tracing")
public class ErafTracingProperties {

    /**
     * 트레이싱 활성화
     */
    private boolean enabled = true;

    /**
     * 샘플링 비율 (0.0 ~ 1.0)
     */
    private float samplingRate = 1.0f;

    /**
     * 트레이스 ID 헤더 이름
     */
    private String traceIdHeader = "X-Trace-ID";

    /**
     * 스팬 ID 헤더 이름
     */
    private String spanIdHeader = "X-Span-ID";

    /**
     * 부모 스팬 ID 헤더 이름
     */
    private String parentSpanIdHeader = "X-Parent-Span-ID";

    /**
     * MDC에 트레이스 정보 추가
     */
    private boolean mdcEnabled = true;

    /**
     * 트레이싱 제외 URL 패턴
     */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/actuator/**",
            "/health/**",
            "/favicon.ico"
    ));

    /**
     * 응답 헤더에 트레이스 ID 포함
     */
    private boolean includeInResponse = true;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
    }

    public String getTraceIdHeader() {
        return traceIdHeader;
    }

    public void setTraceIdHeader(String traceIdHeader) {
        this.traceIdHeader = traceIdHeader;
    }

    public String getSpanIdHeader() {
        return spanIdHeader;
    }

    public void setSpanIdHeader(String spanIdHeader) {
        this.spanIdHeader = spanIdHeader;
    }

    public String getParentSpanIdHeader() {
        return parentSpanIdHeader;
    }

    public void setParentSpanIdHeader(String parentSpanIdHeader) {
        this.parentSpanIdHeader = parentSpanIdHeader;
    }

    public boolean isMdcEnabled() {
        return mdcEnabled;
    }

    public void setMdcEnabled(boolean mdcEnabled) {
        this.mdcEnabled = mdcEnabled;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public boolean isIncludeInResponse() {
        return includeInResponse;
    }

    public void setIncludeInResponse(boolean includeInResponse) {
        this.includeInResponse = includeInResponse;
    }
}
