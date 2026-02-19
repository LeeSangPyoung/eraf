package com.eraf.jpa.slowquery;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Slow Query 감지 설정
 */
@ConfigurationProperties(prefix = "eraf.jpa.slow-query")
public class SlowQueryProperties {

    /**
     * Slow Query 감지 활성화 여부
     */
    private boolean enabled = false;

    /**
     * Slow Query 기준 시간 (밀리초)
     * 이 시간보다 오래 걸리는 쿼리는 경고 로그 출력
     */
    @Positive
    private long thresholdMs = 1000;

    /**
     * Slow Query를 에러 레벨로 로깅할지 여부 (false면 WARN 레벨)
     */
    private boolean logAsError = false;

    /**
     * 쿼리 로그에 바인드 파라미터 포함 여부
     */
    private boolean logParameters = true;

    /**
     * 쿼리 로그에 스택 트레이스 포함 여부
     */
    private boolean logStackTrace = false;

    /**
     * 스택 트레이스 깊이 (0이면 전체)
     */
    @PositiveOrZero
    private int stackTraceDepth = 10;

    /**
     * 매우 느린 쿼리 기준 시간 (밀리초)
     * 이 시간을 초과하면 무조건 ERROR 레벨로 로깅
     */
    @Positive
    private long criticalThresholdMs = 5000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getThresholdMs() {
        return thresholdMs;
    }

    public void setThresholdMs(long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public boolean isLogAsError() {
        return logAsError;
    }

    public void setLogAsError(boolean logAsError) {
        this.logAsError = logAsError;
    }

    public boolean isLogParameters() {
        return logParameters;
    }

    public void setLogParameters(boolean logParameters) {
        this.logParameters = logParameters;
    }

    public boolean isLogStackTrace() {
        return logStackTrace;
    }

    public void setLogStackTrace(boolean logStackTrace) {
        this.logStackTrace = logStackTrace;
    }

    public int getStackTraceDepth() {
        return stackTraceDepth;
    }

    public void setStackTraceDepth(int stackTraceDepth) {
        this.stackTraceDepth = stackTraceDepth;
    }

    public long getCriticalThresholdMs() {
        return criticalThresholdMs;
    }

    public void setCriticalThresholdMs(long criticalThresholdMs) {
        this.criticalThresholdMs = criticalThresholdMs;
    }
}
