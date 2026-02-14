package com.eraf.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventLog - 이벤트 로그")
class EventLogTest {

    @Test
    @DisplayName("기본 생성자")
    void defaultConstructor() {
        EventLog log = new EventLog();
        assertNull(log.getEventId());
        assertNull(log.getEventType());
        assertNull(log.getPayload());
    }

    @Test
    @DisplayName("3-arg 생성자")
    void threeArgConstructor() {
        EventLog log = new EventLog("evt-001", "OrderCreated", "payload");
        assertEquals("evt-001", log.getEventId());
        assertEquals("OrderCreated", log.getEventType());
        assertEquals("payload", log.getPayload());
        assertNotNull(log.getPublishedAt());
        assertEquals(EventLog.EventStatus.PUBLISHED, log.getStatus());
        assertEquals(0, log.getRetryCount());
    }

    @Test
    @DisplayName("setter/getter")
    void setterGetter() {
        EventLog log = new EventLog();
        log.setEventId("id-1");
        log.setEventType("type");
        log.setPayload("data");
        log.setPublisherId("publisher-1");
        log.setStatus(EventLog.EventStatus.PROCESSING);
        log.setErrorMessage("error");
        log.setRetryCount(3);

        assertEquals("id-1", log.getEventId());
        assertEquals("type", log.getEventType());
        assertEquals("data", log.getPayload());
        assertEquals("publisher-1", log.getPublisherId());
        assertEquals(EventLog.EventStatus.PROCESSING, log.getStatus());
        assertEquals("error", log.getErrorMessage());
        assertEquals(3, log.getRetryCount());
    }

    @Test
    @DisplayName("incrementRetryCount")
    void incrementRetryCount() {
        EventLog log = new EventLog("id", "type", null);
        assertEquals(0, log.getRetryCount());

        log.incrementRetryCount();
        assertEquals(1, log.getRetryCount());

        log.incrementRetryCount();
        assertEquals(2, log.getRetryCount());
    }

    @Test
    @DisplayName("markAsCompleted")
    void markAsCompleted() {
        EventLog log = new EventLog("id", "type", null);
        log.markAsCompleted();
        assertEquals(EventLog.EventStatus.COMPLETED, log.getStatus());
    }

    @Test
    @DisplayName("markAsFailed")
    void markAsFailed() {
        EventLog log = new EventLog("id", "type", null);
        log.markAsFailed("connection timeout");
        assertEquals(EventLog.EventStatus.FAILED, log.getStatus());
        assertEquals("connection timeout", log.getErrorMessage());
    }

    @Test
    @DisplayName("EventStatus 열거형")
    void eventStatus() {
        assertEquals(5, EventLog.EventStatus.values().length);
        assertNotNull(EventLog.EventStatus.valueOf("PUBLISHED"));
        assertNotNull(EventLog.EventStatus.valueOf("PROCESSING"));
        assertNotNull(EventLog.EventStatus.valueOf("COMPLETED"));
        assertNotNull(EventLog.EventStatus.valueOf("FAILED"));
        assertNotNull(EventLog.EventStatus.valueOf("RETRYING"));
    }
}
