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
        TraceContext context = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-456")
                .parentSpanId("parent-789")
                .build();

        assertEquals("trace-123", context.getTraceId());
        assertEquals("span-456", context.getSpanId());
        assertEquals("parent-789", context.getParentSpanId());
    }

    @Test
    @DisplayName("ParentSpanId null 허용")
    void shouldAllowNullParentSpanId() {
        TraceContext context = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-456")
                .build();

        assertEquals("trace-123", context.getTraceId());
        assertEquals("span-456", context.getSpanId());
        assertNull(context.getParentSpanId());
    }

    @Test
    @DisplayName("child span 생성")
    void shouldCreateChildSpan() {
        TraceContext parent = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-456")
                .build();

        TraceContext child = TraceContext.createChild(parent);

        assertEquals("trace-123", child.getTraceId()); // 동일한 traceId
        assertNotEquals("span-456", child.getSpanId()); // 새로운 spanId
        assertEquals("span-456", child.getParentSpanId()); // parent의 spanId가 parentSpanId
    }

    @Test
    @DisplayName("손자 span 생성")
    void shouldCreateGrandchildSpan() {
        TraceContext grandparent = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-1")
                .build();
        TraceContext parent = TraceContext.createChild(grandparent);
        TraceContext child = TraceContext.createChild(parent);

        assertEquals("trace-123", child.getTraceId());
        assertEquals(parent.getSpanId(), child.getParentSpanId());
        assertNotEquals(parent.getSpanId(), child.getSpanId());
    }

    @Test
    @DisplayName("create로 새 트레이스 시작")
    void shouldCreateRootContext() {
        TraceContext root = TraceContext.create();

        assertNotNull(root.getTraceId());
        assertNotNull(root.getSpanId());
        assertNull(root.getParentSpanId());
    }

    @Test
    @DisplayName("create는 매번 다른 ID 생성")
    void shouldCreateUniqueRootContexts() {
        TraceContext root1 = TraceContext.create();
        TraceContext root2 = TraceContext.create();

        assertNotEquals(root1.getTraceId(), root2.getTraceId());
        assertNotEquals(root1.getSpanId(), root2.getSpanId());
    }

    @Test
    @DisplayName("createChild는 매번 다른 spanId 생성")
    void shouldCreateUniqueChildSpanIds() {
        TraceContext parent = TraceContext.create();

        TraceContext child1 = TraceContext.createChild(parent);
        TraceContext child2 = TraceContext.createChild(parent);

        assertEquals(child1.getTraceId(), child2.getTraceId());
        assertNotEquals(child1.getSpanId(), child2.getSpanId());
        assertEquals(child1.getParentSpanId(), child2.getParentSpanId());
    }
}
