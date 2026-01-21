package com.eraf.starter.serviceclient;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Service Client 설정
 */
@ConfigurationProperties(prefix = "eraf.service-client")
public class ErafServiceClientProperties {

    /**
     * 기본 타임아웃
     */
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * 기본 재시도 횟수
     */
    private int retryCount = 3;

    /**
     * Circuit Breaker 기본 활성화
     */
    private boolean circuitBreakerEnabled = true;

    /**
     * 컨텍스트 전파 활성화
     */
    private boolean contextPropagationEnabled = true;

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    public boolean isContextPropagationEnabled() {
        return contextPropagationEnabled;
    }

    public void setContextPropagationEnabled(boolean contextPropagationEnabled) {
        this.contextPropagationEnabled = contextPropagationEnabled;
    }
}
