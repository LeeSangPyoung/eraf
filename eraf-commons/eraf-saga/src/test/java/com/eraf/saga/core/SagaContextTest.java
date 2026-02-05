package com.eraf.saga.core;

import com.eraf.saga.execution.SagaExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SagaContextTest {

    @Test
    @DisplayName("컨텍스트 생성")
    void testCreate() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga", "trace-123");

        // When
        SagaContext context = new SagaContext(execution);

        // Then
        assertNotNull(context);
        assertEquals("testSaga", context.getSagaName());
        assertEquals("trace-123", context.getTraceId());
    }

    @Test
    @DisplayName("데이터 저장 및 조회")
    void testPutAndGet() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        SagaContext context = new SagaContext(execution);

        // When
        context.put("orderId", "ORDER-123");
        context.put("amount", 1000);

        // Then
        assertEquals("ORDER-123", context.get("orderId"));
        assertEquals(1000, (Integer) context.get("amount"));
    }

    @Test
    @DisplayName("데이터 존재 여부 확인")
    void testHas() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        SagaContext context = new SagaContext(execution);
        context.put("key1", "value1");

        // Then
        assertTrue(context.has("key1"));
        assertFalse(context.has("key2"));
    }

    @Test
    @DisplayName("기본값 포함 조회")
    void testGetWithDefault() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        SagaContext context = new SagaContext(execution);

        // When
        String value = context.get("nonexistent", "default");

        // Then
        assertEquals("default", value);
    }

    @Test
    @DisplayName("입력 데이터 설정 및 조회")
    void testInput() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        Object input = new Object();
        SagaContext context = new SagaContext(execution, input);

        // Then
        assertEquals(input, context.getInput());
    }

    @Test
    @DisplayName("ThreadLocal 컨텍스트")
    void testThreadLocal() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        SagaContext context = new SagaContext(execution);

        // When
        SagaContext.setCurrent(context);

        // Then
        assertEquals(context, SagaContext.current());

        // Cleanup
        SagaContext.clear();
        assertNull(SagaContext.current());
    }
}
