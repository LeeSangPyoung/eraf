package com.eraf.saga;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Saga Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.saga")
public class ErafSagaProperties {

    /**
     * Saga 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * Repository 타입 (memory, jpa, redis)
     */
    private String repositoryType = "memory";

    /**
     * 기본 타임아웃
     */
    private Duration defaultTimeout = Duration.ofMinutes(5);

    /**
     * 기본 재시도 횟수
     */
    private int defaultMaxRetries = 3;

    /**
     * 재시도 간격
     */
    private Duration retryDelay = Duration.ofSeconds(1);

    /**
     * 완료된 Saga 보관 기간
     */
    private Duration retentionPeriod = Duration.ofDays(7);

    /**
     * 정리 작업 실행 주기
     */
    private Duration cleanupInterval = Duration.ofHours(1);

    /**
     * 복구 작업 실행 주기
     */
    private Duration recoveryInterval = Duration.ofMinutes(1);

    /**
     * REST API 활성화 여부
     */
    private boolean apiEnabled = true;

    /**
     * REST API 경로
     */
    private String apiPath = "/api/saga";

    /**
     * 이벤트 발행 활성화 여부
     */
    private boolean eventsEnabled = true;

    /**
     * 이벤트 발행 토픽
     */
    private String eventTopic = "eraf.saga.events";

    /**
     * 이벤트 발행 방식 (spring, messaging)
     */
    private String eventPublisherType = "spring";

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getDefaultMaxRetries() {
        return defaultMaxRetries;
    }

    public void setDefaultMaxRetries(int defaultMaxRetries) {
        this.defaultMaxRetries = defaultMaxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Duration getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(Duration retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public Duration getRecoveryInterval() {
        return recoveryInterval;
    }

    public void setRecoveryInterval(Duration recoveryInterval) {
        this.recoveryInterval = recoveryInterval;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }

    public void setApiEnabled(boolean apiEnabled) {
        this.apiEnabled = apiEnabled;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public String getEventPublisherType() {
        return eventPublisherType;
    }

    public void setEventPublisherType(String eventPublisherType) {
        this.eventPublisherType = eventPublisherType;
    }
}
