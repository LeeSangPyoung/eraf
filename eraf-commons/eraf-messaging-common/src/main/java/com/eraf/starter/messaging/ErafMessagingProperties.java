package com.eraf.starter.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF 메시징 설정
 */
@ConfigurationProperties(prefix = "eraf.messaging")
public class ErafMessagingProperties {

    /**
     * 메시징 타입 (kafka, rabbitmq, sqs)
     */
    private MessagingType type = MessagingType.KAFKA;

    /**
     * 기본 토픽/큐 이름
     */
    private String defaultDestination;

    /**
     * 컨텍스트 전파 활성화
     */
    private boolean contextPropagationEnabled = true;

    /**
     * 재시도 횟수
     */
    private int retryCount = 3;

    /**
     * DLQ 활성화
     */
    private boolean dlqEnabled = true;

    public enum MessagingType {
        KAFKA, RABBITMQ, SQS
    }

    public MessagingType getType() {
        return type;
    }

    public void setType(MessagingType type) {
        this.type = type;
    }

    public String getDefaultDestination() {
        return defaultDestination;
    }

    public void setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    public boolean isContextPropagationEnabled() {
        return contextPropagationEnabled;
    }

    public void setContextPropagationEnabled(boolean contextPropagationEnabled) {
        this.contextPropagationEnabled = contextPropagationEnabled;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isDlqEnabled() {
        return dlqEnabled;
    }

    public void setDlqEnabled(boolean dlqEnabled) {
        this.dlqEnabled = dlqEnabled;
    }
}
