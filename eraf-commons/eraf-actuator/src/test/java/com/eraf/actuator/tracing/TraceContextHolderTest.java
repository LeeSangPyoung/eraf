package com.eraf.actuator.tracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceContextHolder 테스트
 */
class TraceContextHolderTest {

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
    }

    @Test
    @DisplayName("TraceContext 설정 및 조회")
    void shouldSetAndGetContext() {
        TraceContext context = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-456")
                .build();
        TraceContextHolder.setContext(context);

        TraceContext retrieved = TraceContextHolder.getContext();

        assertNotNull(retrieved);
        assertEquals("trace-123", retrieved.getTraceId());
        assertEquals("span-456", retrieved.getSpanId());
    }

    @Test
    @DisplayName("컨텍스트 없을 때 null 반환")
    void shouldReturnNullWhenNoContext() {
        TraceContext context = TraceContextHolder.getContext();

        assertNull(context);
    }

    @Test
    @DisplayName("clear 후 null 반환")
    void shouldReturnNullAfterClear() {
        TraceContext context = TraceContext.builder()
                .traceId("trace-123")
                .spanId("span-456")
                .build();
        TraceContextHolder.setContext(context);

        TraceContextHolder.clear();

        assertNull(TraceContextHolder.getContext());
    }

    @Test
    @DisplayName("다른 스레드는 독립적인 컨텍스트")
    void shouldHaveIndependentContextPerThread() throws InterruptedException {
        TraceContext mainContext = TraceContext.builder()
                .traceId("main-trace")
                .spanId("main-span")
                .build();
        TraceContextHolder.setContext(mainContext);

        final TraceContext[] otherThreadContext = new TraceContext[1];

        Thread otherThread = new Thread(() -> {
            TraceContext threadContext = TraceContext.builder()
                    .traceId("other-trace")
                    .spanId("other-span")
                    .build();
            TraceContextHolder.setContext(threadContext);
            otherThreadContext[0] = TraceContextHolder.getContext();
        });

        otherThread.start();
        otherThread.join();

        // 메인 스레드는 여전히 원래 컨텍스트 유지
        assertEquals("main-trace", TraceContextHolder.getContext().getTraceId());

        // 다른 스레드는 자신의 컨텍스트
        assertEquals("other-trace", otherThreadContext[0].getTraceId());
    }
}
