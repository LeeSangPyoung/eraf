package com.eraf.actuator.tracing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceContext 테스트
 */
class TraceContextTest {

    @Test
    @DisplayName("TraceContext 생성")
    void shouldCreateTraceContext() {
        TraceContext context = new TraceContext("trace-123", "span-456", "parent-789");

        assertEquals("trace-123", context.getTraceId());
        assertEquals("span-456", context.getSpanId());
        assertEquals("parent-789", context.getParentSpanId());
    }

    @Test
    @DisplayName("ParentSpanId null 허용")
    void shouldAllowNullParentSpanId() {
        TraceContext context = new TraceContext("trace-123", "span-456", null);

        assertEquals("trace-123", context.getTraceId());
        assertEquals("span-456", context.getSpanId());
        assertNull(context.getParentSpanId());
    }

    @Test
    @DisplayName("child span 생성")
    void shouldCreateChildSpan() {
        TraceContext parent = new TraceContext("trace-123", "span-456", null);

        TraceContext child = parent.createChild();

        assertEquals("trace-123", child.getTraceId()); // 동일한 traceId
        assertNotEquals("span-456", child.getSpanId()); // 새로운 spanId
        assertEquals("span-456", child.getParentSpanId()); // parent의 spanId가 parentSpanId
    }

    @Test
    @DisplayName("손자 span 생성")
    void shouldCreateGrandchildSpan() {
        TraceContext grandparent = new TraceContext("trace-123", "span-1", null);
        TraceContext parent = grandparent.createChild();
        TraceContext child = parent.createChild();

        assertEquals("trace-123", child.getTraceId());
        assertEquals(parent.getSpanId(), child.getParentSpanId());
        assertNotEquals(parent.getSpanId(), child.getSpanId());
    }

    @Test
    @DisplayName("createRoot로 새 트레이스 시작")
    void shouldCreateRootContext() {
        TraceContext root = TraceContext.createRoot();

        assertNotNull(root.getTraceId());
        assertNotNull(root.getSpanId());
        assertNull(root.getParentSpanId());
    }

    @Test
    @DisplayName("createRoot는 매번 다른 ID 생성")
    void shouldCreateUniqueRootContexts() {
        TraceContext root1 = TraceContext.createRoot();
        TraceContext root2 = TraceContext.createRoot();

        assertNotEquals(root1.getTraceId(), root2.getTraceId());
        assertNotEquals(root1.getSpanId(), root2.getSpanId());
    }

    @Test
    @DisplayName("createChild는 매번 다른 spanId 생성")
    void shouldCreateUniqueChildSpanIds() {
        TraceContext parent = TraceContext.createRoot();

        TraceContext child1 = parent.createChild();
        TraceContext child2 = parent.createChild();

        assertEquals(child1.getTraceId(), child2.getTraceId());
        assertNotEquals(child1.getSpanId(), child2.getSpanId());
        assertEquals(child1.getParentSpanId(), child2.getParentSpanId());
    }
}
