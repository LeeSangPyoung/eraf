package com.eraf.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErafMessage - 표준 메시지 포맷")
class ErafMessageTest {

    @Test
    @DisplayName("기본 생성자")
    void defaultConstructor() {
        ErafMessage<String> msg = new ErafMessage<>();
        assertNotNull(msg.getMessageId());
        assertNotNull(msg.getTimestamp());
        assertNotNull(msg.getHeaders());
        assertTrue(msg.getHeaders().isEmpty());
        assertEquals(0, msg.getRetryCount());
        assertNull(msg.getPayload());
    }

    @Test
    @DisplayName("payload 생성자")
    void payloadConstructor() {
        ErafMessage<String> msg = new ErafMessage<>("hello");
        assertEquals("hello", msg.getPayload());
        assertEquals("String", msg.getType());
        assertNotNull(msg.getMessageId());
    }

    @Test
    @DisplayName("payload null인 경우")
    void payloadNullConstructor() {
        ErafMessage<String> msg = new ErafMessage<>(null);
        assertNull(msg.getPayload());
        assertNull(msg.getType());
    }

    @Test
    @DisplayName("of 팩토리 메서드")
    void ofFactory() {
        ErafMessage<Integer> msg = ErafMessage.of(42);
        assertEquals(42, msg.getPayload());
        assertEquals("Integer", msg.getType());
    }

    @Test
    @DisplayName("빌더 패턴 - withTraceId")
    void withTraceId() {
        ErafMessage<String> msg = ErafMessage.of("data")
                .withTraceId("trace-123");
        assertEquals("trace-123", msg.getTraceId());
    }

    @Test
    @DisplayName("빌더 패턴 - withSource")
    void withSource() {
        ErafMessage<String> msg = ErafMessage.of("data")
                .withSource("order-service");
        assertEquals("order-service", msg.getSource());
    }

    @Test
    @DisplayName("빌더 패턴 - withHeader")
    void withHeader() {
        ErafMessage<String> msg = ErafMessage.of("data")
                .withHeader("key1", "val1")
                .withHeader("key2", 42);
        assertEquals("val1", msg.getHeaders().get("key1"));
        assertEquals(42, msg.getHeaders().get("key2"));
        assertEquals(2, msg.getHeaders().size());
    }

    @Test
    @DisplayName("빌더 체이닝")
    void builderChaining() {
        ErafMessage<String> msg = ErafMessage.of("payload")
                .withTraceId("trace-1")
                .withSource("svc-a")
                .withHeader("h1", "v1");

        assertEquals("payload", msg.getPayload());
        assertEquals("trace-1", msg.getTraceId());
        assertEquals("svc-a", msg.getSource());
        assertEquals("v1", msg.getHeaders().get("h1"));
    }

    @Test
    @DisplayName("setter/getter")
    void setterGetter() {
        ErafMessage<String> msg = new ErafMessage<>();
        msg.setMessageId("custom-id");
        msg.setTraceId("trace-x");
        msg.setSource("src");
        msg.setType("CustomType");
        msg.setPayload("data");
        msg.setRetryCount(5);

        assertEquals("custom-id", msg.getMessageId());
        assertEquals("trace-x", msg.getTraceId());
        assertEquals("src", msg.getSource());
        assertEquals("CustomType", msg.getType());
        assertEquals("data", msg.getPayload());
        assertEquals(5, msg.getRetryCount());
    }

    @Test
    @DisplayName("메시지 ID 유일성")
    void uniqueMessageIds() {
        ErafMessage<String> m1 = new ErafMessage<>();
        ErafMessage<String> m2 = new ErafMessage<>();
        assertNotEquals(m1.getMessageId(), m2.getMessageId());
    }
}
