package com.eraf.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ErafSagaPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // Then
        assertTrue(properties.isEnabled());
        assertEquals("memory", properties.getRepositoryType());
        assertEquals(Duration.ofMinutes(5), properties.getDefaultTimeout());
        assertEquals(3, properties.getDefaultMaxRetries());
        assertEquals(Duration.ofSeconds(1), properties.getRetryDelay());
        assertEquals(Duration.ofDays(7), properties.getRetentionPeriod());
        assertEquals(Duration.ofHours(1), properties.getCleanupInterval());
        assertEquals(Duration.ofMinutes(1), properties.getRecoveryInterval());
        assertTrue(properties.isApiEnabled());
        assertEquals("/api/saga", properties.getApiPath());
        assertTrue(properties.isEventsEnabled());
        assertEquals("eraf.saga.events", properties.getEventTopic());
        assertEquals("spring", properties.getEventPublisherType());
    }

    @Test
    @DisplayName("enabled 설정")
    void testSetEnabled() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setEnabled(false);

        // Then
        assertFalse(properties.isEnabled());
    }

    @Test
    @DisplayName("repositoryType 설정")
    void testSetRepositoryType() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setRepositoryType("jpa");

        // Then
        assertEquals("jpa", properties.getRepositoryType());
    }

    @Test
    @DisplayName("defaultTimeout 설정")
    void testSetDefaultTimeout() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setDefaultTimeout(Duration.ofMinutes(10));

        // Then
        assertEquals(Duration.ofMinutes(10), properties.getDefaultTimeout());
    }

    @Test
    @DisplayName("defaultMaxRetries 설정")
    void testSetDefaultMaxRetries() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setDefaultMaxRetries(5);

        // Then
        assertEquals(5, properties.getDefaultMaxRetries());
    }

    @Test
    @DisplayName("retryDelay 설정")
    void testSetRetryDelay() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setRetryDelay(Duration.ofSeconds(5));

        // Then
        assertEquals(Duration.ofSeconds(5), properties.getRetryDelay());
    }

    @Test
    @DisplayName("retentionPeriod 설정")
    void testSetRetentionPeriod() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setRetentionPeriod(Duration.ofDays(30));

        // Then
        assertEquals(Duration.ofDays(30), properties.getRetentionPeriod());
    }

    @Test
    @DisplayName("cleanupInterval 설정")
    void testSetCleanupInterval() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setCleanupInterval(Duration.ofHours(6));

        // Then
        assertEquals(Duration.ofHours(6), properties.getCleanupInterval());
    }

    @Test
    @DisplayName("recoveryInterval 설정")
    void testSetRecoveryInterval() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setRecoveryInterval(Duration.ofMinutes(5));

        // Then
        assertEquals(Duration.ofMinutes(5), properties.getRecoveryInterval());
    }

    @Test
    @DisplayName("apiEnabled 설정")
    void testSetApiEnabled() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setApiEnabled(false);

        // Then
        assertFalse(properties.isApiEnabled());
    }

    @Test
    @DisplayName("apiPath 설정")
    void testSetApiPath() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setApiPath("/custom/saga");

        // Then
        assertEquals("/custom/saga", properties.getApiPath());
    }

    @Test
    @DisplayName("eventsEnabled 설정")
    void testSetEventsEnabled() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setEventsEnabled(false);

        // Then
        assertFalse(properties.isEventsEnabled());
    }

    @Test
    @DisplayName("eventTopic 설정")
    void testSetEventTopic() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setEventTopic("custom.saga.topic");

        // Then
        assertEquals("custom.saga.topic", properties.getEventTopic());
    }

    @Test
    @DisplayName("eventPublisherType 설정")
    void testSetEventPublisherType() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setEventPublisherType("messaging");

        // Then
        assertEquals("messaging", properties.getEventPublisherType());
    }

    @Test
    @DisplayName("전체 프로퍼티 커스터마이징")
    void testFullCustomization() {
        // Given
        ErafSagaProperties properties = new ErafSagaProperties();

        // When
        properties.setEnabled(true);
        properties.setRepositoryType("redis");
        properties.setDefaultTimeout(Duration.ofMinutes(15));
        properties.setDefaultMaxRetries(10);
        properties.setRetryDelay(Duration.ofSeconds(3));
        properties.setRetentionPeriod(Duration.ofDays(14));
        properties.setCleanupInterval(Duration.ofHours(12));
        properties.setRecoveryInterval(Duration.ofMinutes(2));
        properties.setApiEnabled(true);
        properties.setApiPath("/admin/saga");
        properties.setEventsEnabled(true);
        properties.setEventTopic("app.saga.events");
        properties.setEventPublisherType("kafka");

        // Then
        assertTrue(properties.isEnabled());
        assertEquals("redis", properties.getRepositoryType());
        assertEquals(Duration.ofMinutes(15), properties.getDefaultTimeout());
        assertEquals(10, properties.getDefaultMaxRetries());
        assertEquals(Duration.ofSeconds(3), properties.getRetryDelay());
        assertEquals(Duration.ofDays(14), properties.getRetentionPeriod());
        assertEquals(Duration.ofHours(12), properties.getCleanupInterval());
        assertEquals(Duration.ofMinutes(2), properties.getRecoveryInterval());
        assertTrue(properties.isApiEnabled());
        assertEquals("/admin/saga", properties.getApiPath());
        assertTrue(properties.isEventsEnabled());
        assertEquals("app.saga.events", properties.getEventTopic());
        assertEquals("kafka", properties.getEventPublisherType());
    }
}
