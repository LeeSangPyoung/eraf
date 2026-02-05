package com.eraf.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErafKafkaEventTest {

    @Test
    @DisplayName("기본 이벤트 생성")
    void testCreateEvent() {
        // When
        ErafKafkaEvent<String> event = new ErafKafkaEvent<>();

        // Then
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertEquals("1.0", event.getVersion());
    }

    @Test
    @DisplayName("이벤트 타입과 페이로드로 생성")
    void testCreateEventWithTypeAndPayload() {
        // Given
        String eventType = "ORDER_CREATED";
        OrderPayload payload = new OrderPayload("ORD-123", 10000);

        // When
        ErafKafkaEvent<OrderPayload> event = new ErafKafkaEvent<>(eventType, payload);

        // Then
        assertEquals(eventType, event.getEventType());
        assertEquals(payload, event.getPayload());
    }

    @Test
    @DisplayName("정적 팩토리 메서드로 생성")
    void testOfMethod() {
        // When
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("TEST_EVENT", "payload");

        // Then
        assertEquals("TEST_EVENT", event.getEventType());
        assertEquals("payload", event.getPayload());
    }

    @Test
    @DisplayName("빌더로 이벤트 생성")
    void testBuilder() {
        // Given
        Map<String, Object> metadata = Map.of("key1", "value1");

        // When
        ErafKafkaEvent<String> event = ErafKafkaEvent.<String>builder()
                .eventType("USER_REGISTERED")
                .source("user-service")
                .traceId("trace-123")
                .payload("user-data")
                .metadata(metadata)
                .build();

        // Then
        assertEquals("USER_REGISTERED", event.getEventType());
        assertEquals("user-service", event.getSource());
        assertEquals("trace-123", event.getTraceId());
        assertEquals("user-data", event.getPayload());
        assertEquals(metadata, event.getMetadata());
    }

    @Test
    @DisplayName("이벤트 ID 고유성")
    void testEventIdUniqueness() {
        // When
        ErafKafkaEvent<String> event1 = new ErafKafkaEvent<>();
        ErafKafkaEvent<String> event2 = new ErafKafkaEvent<>();

        // Then
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }

    @Test
    @DisplayName("타임스탬프 자동 설정")
    void testTimestampAutoSet() {
        // Given
        Instant before = Instant.now();

        // When
        ErafKafkaEvent<String> event = new ErafKafkaEvent<>();

        // Then
        Instant after = Instant.now();
        assertFalse(event.getTimestamp().isBefore(before));
        assertFalse(event.getTimestamp().isAfter(after));
    }

    @Test
    @DisplayName("Setter 동작 확인")
    void testSetters() {
        // Given
        ErafKafkaEvent<String> event = new ErafKafkaEvent<>();
        Instant customTimestamp = Instant.parse("2024-01-15T10:30:00Z");

        // When
        event.setEventId("custom-id");
        event.setEventType("CUSTOM_EVENT");
        event.setTimestamp(customTimestamp);
        event.setSource("custom-source");
        event.setTraceId("trace-abc");
        event.setVersion("2.0");
        event.setPayload("custom-payload");
        event.setMetadata(Map.of("meta", "data"));

        // Then
        assertEquals("custom-id", event.getEventId());
        assertEquals("CUSTOM_EVENT", event.getEventType());
        assertEquals(customTimestamp, event.getTimestamp());
        assertEquals("custom-source", event.getSource());
        assertEquals("trace-abc", event.getTraceId());
        assertEquals("2.0", event.getVersion());
        assertEquals("custom-payload", event.getPayload());
        assertEquals("data", event.getMetadata().get("meta"));
    }

    @Test
    @DisplayName("복잡한 페이로드 처리")
    void testComplexPayload() {
        // Given
        OrderPayload payload = new OrderPayload("ORD-999", 50000);

        // When
        ErafKafkaEvent<OrderPayload> event = ErafKafkaEvent.of("ORDER_CREATED", payload);

        // Then
        assertEquals("ORD-999", event.getPayload().orderId());
        assertEquals(50000, event.getPayload().amount());
    }

    @Test
    @DisplayName("null 페이로드 허용")
    void testNullPayload() {
        // When
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("EMPTY_EVENT", null);

        // Then
        assertEquals("EMPTY_EVENT", event.getEventType());
        assertNull(event.getPayload());
    }

    // Test payload record
    record OrderPayload(String orderId, int amount) {}
}
