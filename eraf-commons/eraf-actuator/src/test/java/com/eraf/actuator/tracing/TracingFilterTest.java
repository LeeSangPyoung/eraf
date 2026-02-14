package com.eraf.actuator.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TracingFilter 테스트
 */
@ExtendWith(MockitoExtension.class)
class TracingFilterTest {

    private TracingFilter filter;
    private ErafTracingProperties properties;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        properties = new ErafTracingProperties();
        properties.setTraceIdHeader("X-Trace-ID");
        properties.setSpanIdHeader("X-Span-ID");
        properties.setParentSpanIdHeader("X-Parent-Span-ID");

        filter = new TracingFilter(properties);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        TraceContextHolder.clear();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        MDC.clear();
    }

    @Test
    @DisplayName("새로운 트레이스 컨텍스트 생성")
    void shouldCreateNewTraceContext() throws ServletException, IOException {
        // Given
        doAnswer(inv -> {
            // 필터 체인 내에서 컨텍스트 확인
            assertNotNull(TraceContextHolder.getContext());
            assertNotNull(TraceContextHolder.getContext().getTraceId());
            assertNotNull(TraceContextHolder.getContext().getSpanId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        // 응답 헤더에 트레이스 ID 포함
        assertNotNull(response.getHeader("X-Trace-ID"));
    }

    @Test
    @DisplayName("기존 트레이스 ID 전파")
    void shouldPropagateExistingTraceId() throws ServletException, IOException {
        // Given - X-Span-ID는 spanId로 사용되고, X-Parent-Span-ID가 parentSpanId로 사용됨
        request.addHeader("X-Trace-ID", "existing-trace-123");
        request.addHeader("X-Span-ID", "existing-span-456");
        request.addHeader("X-Parent-Span-ID", "parent-span-789");

        doAnswer(inv -> {
            TraceContext context = TraceContextHolder.getContext();
            assertEquals("existing-trace-123", context.getTraceId());
            assertEquals("existing-span-456", context.getSpanId());
            assertEquals("parent-span-789", context.getParentSpanId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals("existing-trace-123", response.getHeader("X-Trace-ID"));
    }

    @Test
    @DisplayName("MDC에 트레이스 정보 설정")
    void shouldSetMdcValues() throws ServletException, IOException {
        // Given
        doAnswer(inv -> {
            assertNotNull(MDC.get("traceId"));
            assertNotNull(MDC.get("spanId"));
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 완료 후 컨텍스트 정리")
    void shouldClearContextAfterFilter() throws ServletException, IOException {
        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(TraceContextHolder.getContext());
    }

    @Test
    @DisplayName("예외 발생해도 컨텍스트 정리")
    void shouldClearContextOnException() throws ServletException, IOException {
        // Given
        doThrow(new RuntimeException("error")).when(filterChain).doFilter(request, response);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                filter.doFilterInternal(request, response, filterChain));

        assertNull(TraceContextHolder.getContext());
    }

    @Test
    @DisplayName("기존 span ID 유지 및 parent span ID 전파")
    void shouldCreateNewSpanId() throws ServletException, IOException {
        // Given - X-Span-ID 헤더가 있으면 그대로 spanId로 사용
        // X-Parent-Span-ID 헤더가 parentSpanId로 사용됨
        request.addHeader("X-Trace-ID", "trace-123");
        request.addHeader("X-Span-ID", "incoming-span");
        request.addHeader("X-Parent-Span-ID", "parent-span");

        doAnswer(inv -> {
            TraceContext context = TraceContextHolder.getContext();
            assertEquals("incoming-span", context.getSpanId()); // X-Span-ID 그대로 사용
            assertEquals("parent-span", context.getParentSpanId()); // X-Parent-Span-ID 사용
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("커스텀 헤더명 지원")
    void shouldSupportCustomHeaderNames() throws ServletException, IOException {
        // Given
        properties.setTraceIdHeader("X-Request-ID");
        filter = new TracingFilter(properties);

        request.addHeader("X-Request-ID", "custom-trace-id");

        doAnswer(inv -> {
            assertEquals("custom-trace-id", TraceContextHolder.getContext().getTraceId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals("custom-trace-id", response.getHeader("X-Request-ID"));
    }
}
