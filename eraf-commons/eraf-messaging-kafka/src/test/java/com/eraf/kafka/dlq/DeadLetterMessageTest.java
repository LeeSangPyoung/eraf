package com.eraf.kafka.dlq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Map<String, String> headers = new HashMap<>();
        headers.put("correlationId", "corr-123");

        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("orders-topic")
                .originalPartition(2)
                .originalOffset(1000L)
                .originalKey("order-123")
                .originalMessage("{\"orderId\":\"123\"}")
                .error(new RuntimeException("Deserialization failed"))
                .retryCount(3)
                .originalHeaders(headers)
                .build();

        assertEquals("orders-topic", message.getOriginalTopic());
        assertEquals(2, message.getOriginalPartition());
        assertEquals(1000L, message.getOriginalOffset());
        assertEquals("order-123", message.getOriginalKey());
        assertEquals("{\"orderId\":\"123\"}", message.getOriginalMessage());
        assertEquals("Deserialization failed", message.getErrorMessage());
        assertEquals("java.lang.RuntimeException", message.getErrorClass());
        assertNotNull(message.getErrorTimestamp());
        assertEquals(3, message.getRetryCount());
        assertEquals("corr-123", message.getOriginalHeaders().get("correlationId"));
    }

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .build();

        assertEquals(0, message.getOriginalPartition());
        assertEquals(0L, message.getOriginalOffset());
        assertEquals(0, message.getRetryCount());
        assertNull(message.getOriginalKey());
        assertNull(message.getOriginalHeaders());
    }

    @Test
    @DisplayName("Setter/Getter 동작")
    void shouldWorkWithSettersAndGetters() {
        DeadLetterMessage message = new DeadLetterMessage();

        message.setOriginalTopic("users-topic");
        message.setOriginalPartition(5);
        message.setOriginalOffset(500L);
        message.setOriginalKey("user-456");
        message.setOriginalMessage("{\"userId\":\"456\"}");
        message.setErrorMessage("Validation error");
        message.setErrorClass("IllegalArgumentException");
        message.setRetryCount(2);

        assertEquals("users-topic", message.getOriginalTopic());
        assertEquals(5, message.getOriginalPartition());
        assertEquals(500L, message.getOriginalOffset());
        assertEquals("user-456", message.getOriginalKey());
        assertEquals("{\"userId\":\"456\"}", message.getOriginalMessage());
        assertEquals("Validation error", message.getErrorMessage());
        assertEquals("IllegalArgumentException", message.getErrorClass());
        assertEquals(2, message.getRetryCount());
    }

    @Test
    @DisplayName("Headers null 허용")
    void shouldAllowNullHeaders() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .originalHeaders(null)
                .build();

        assertNull(message.getOriginalHeaders());
    }

    @Test
    @DisplayName("빈 Headers 허용")
    void shouldAllowEmptyHeaders() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .originalHeaders(new HashMap<>())
                .build();

        assertNotNull(message.getOriginalHeaders());
        assertTrue(message.getOriginalHeaders().isEmpty());
    }

    @Test
    @DisplayName("긴 에러 메시지 처리")
    void shouldHandleLongErrorMessage() {
        String longError = "x".repeat(5000);

        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .error(new RuntimeException(longError))
                .build();

        assertEquals(5000, message.getErrorMessage().length());
    }

    @Test
    @DisplayName("error() 빌더가 타임스탬프 자동 설정")
    void shouldSetTimestampViaError() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .error(new RuntimeException("test error"))
                .build();

        assertNotNull(message.getErrorTimestamp());
        assertNotNull(message.getStackTrace());
    }

    @Test
    @DisplayName("consumerGroup과 traceId 설정")
    void shouldSetConsumerGroupAndTraceId() {
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("test-topic")
                .originalMessage("{}")
                .consumerGroup("my-group")
                .traceId("trace-abc-123")
                .build();

        assertEquals("my-group", message.getConsumerGroup());
        assertEquals("trace-abc-123", message.getTraceId());
    }
}
