package com.eraf.core.event;

import java.time.Instant;

/**
 * 이벤트 로그 (이벤트 발행 기록)
 */
public class EventLog {

    private String eventId;
    private String eventType;
    private Object payload;
    private Instant publishedAt;
    private String publisherId;
    private EventStatus status;
    private String errorMessage;
    private int retryCount;

    public enum EventStatus {
        PUBLISHED, PROCESSING, COMPLETED, FAILED, RETRYING
    }

    public EventLog() {
    }

    public EventLog(String eventId, String eventType, Object payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.publishedAt = Instant.now();
        this.status = EventStatus.PUBLISHED;
        this.retryCount = 0;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsCompleted() {
        this.status = EventStatus.COMPLETED;
    }

    public void markAsFailed(String errorMessage) {
        this.status = EventStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
