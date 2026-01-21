package com.eraf.starter.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF RabbitMQ 설정
 */
@ConfigurationProperties(prefix = "eraf.rabbitmq")
public class ErafRabbitProperties {

    /**
     * DLQ(Dead Letter Queue) 활성화
     */
    private boolean dlqEnabled = true;

    /**
     * DLQ 접미사
     */
    private String dlqSuffix = ".dlq";

    /**
     * 재시도 횟수
     */
    private int retryCount = 3;

    /**
     * 재시도 초기 간격 (ms)
     */
    private long retryInitialInterval = 1000;

    /**
     * 재시도 최대 간격 (ms)
     */
    private long retryMaxInterval = 10000;

    /**
     * 재시도 배수
     */
    private double retryMultiplier = 2.0;

    /**
     * 컨텍스트 전파 활성화
     */
    private boolean contextPropagationEnabled = true;

    public boolean isDlqEnabled() {
        return dlqEnabled;
    }

    public void setDlqEnabled(boolean dlqEnabled) {
        this.dlqEnabled = dlqEnabled;
    }

    public String getDlqSuffix() {
        return dlqSuffix;
    }

    public void setDlqSuffix(String dlqSuffix) {
        this.dlqSuffix = dlqSuffix;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getRetryInitialInterval() {
        return retryInitialInterval;
    }

    public void setRetryInitialInterval(long retryInitialInterval) {
        this.retryInitialInterval = retryInitialInterval;
    }

    public long getRetryMaxInterval() {
        return retryMaxInterval;
    }

    public void setRetryMaxInterval(long retryMaxInterval) {
        this.retryMaxInterval = retryMaxInterval;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }

    public boolean isContextPropagationEnabled() {
        return contextPropagationEnabled;
    }

    public void setContextPropagationEnabled(boolean contextPropagationEnabled) {
        this.contextPropagationEnabled = contextPropagationEnabled;
    }
}
