package com.eraf.core.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 도메인 이벤트 베이스 클래스
 */
public abstract class DomainEvent {

    private final String eventId;
    private final Instant timestamp;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }
}
