package com.eraf.kafka.dlq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeadLetterMessage 테스트
 */
class DeadLetterMessageTest {

    @Test
    @DisplayName("Builder를 통한 메시지 생성")
    void shouldCreateMessageUsingBuilder() {
        Instant timestamp = Instant.now();
        Map<String, String> headers = new HashMap<>();
        headers.put("correlationId", "corr-123");

        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("orders-topic")
                .originalPartition(2)
                .originalOffset(1000L)
                .key("order-123")
                .payload("{\"orderId\":\"123\"}")
                .errorMessage("Deserialization failed")
                .errorClass("com.fasterxml.jackson.core.JsonParseException")
                .timestamp(timestamp)
                .retryCount(3)
                .headers(headers)
                .build();

        assertEquals("orders-topic", message.getOriginalTopic());
        assertEquals(2, message.getOriginalPartition());
        assertEquals(1000L, message.getOriginalOffset());
        assertEquals("order-123", message.getKey());
        assertEquals("{\"orderId\":\"123\"}", message.getPayload());
        assertEquals("Deserialization failed", message.getErrorMessage());
        assertEquals("com.fasterxml.jackson.core.JsonParseException", message.getErrorClass());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(3, message.getRetryCount());
        assertEquals("corr-123", message.getHeaders().get("correlationId"));
    }

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .payload("{}")
                .build();

        assertEquals(0, message.getOriginalPartition());
        assertEquals(0L, message.getOriginalOffset());
        assertEquals(0, message.getRetryCount());
        assertNull(message.getKey());
        assertNull(message.getHeaders());
    }

    @Test
    @DisplayName("Setter/Getter 동작")
    void shouldWorkWithSettersAndGetters() {
        DeadLetterMessage message = new DeadLetterMessage();

        message.setOriginalTopic("users-topic");
        message.setOriginalPartition(5);
        message.setOriginalOffset(500L);
        message.setKey("user-456");
        message.setPayload("{\"userId\":\"456\"}");
        message.setErrorMessage("Validation error");
        message.setErrorClass("IllegalArgumentException");
        message.setRetryCount(2);

        assertEquals("users-topic", message.getOriginalTopic());
        assertEquals(5, message.getOriginalPartition());
        assertEquals(500L, message.getOriginalOffset());
        assertEquals("user-456", message.getKey());
        assertEquals("{\"userId\":\"456\"}", message.getPayload());
        assertEquals("Validation error", message.getErrorMessage());
        assertEquals("IllegalArgumentException", message.getErrorClass());
        assertEquals(2, message.getRetryCount());
    }

    @Test
    @DisplayName("Headers null 허용")
    void shouldAllowNullHeaders() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .payload("{}")
                .headers(null)
                .build();

        assertNull(message.getHeaders());
    }

    @Test
    @DisplayName("빈 Headers 허용")
    void shouldAllowEmptyHeaders() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .payload("{}")
                .headers(new HashMap<>())
                .build();

        assertNotNull(message.getHeaders());
        assertTrue(message.getHeaders().isEmpty());
    }

    @Test
    @DisplayName("긴 에러 메시지 처리")
    void shouldHandleLongErrorMessage() {
        String longError = "x".repeat(5000);

        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .payload("{}")
                .errorMessage(longError)
                .build();

        assertEquals(5000, message.getErrorMessage().length());
    }

    @Test
    @DisplayName("Timestamp 설정")
    void shouldSetTimestamp() {
        Instant before = Instant.now();
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .payload("{}")
                .timestamp(Instant.now())
                .build();
        Instant after = Instant.now();

        assertNotNull(message.getTimestamp());
        assertTrue(!message.getTimestamp().isBefore(before));
        assertTrue(!message.getTimestamp().isAfter(after));
    }
}
