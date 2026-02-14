package com.eraf.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.function.Supplier;

/**
 * OpenTelemetry Tracing Utility
 */
public class TracingUtil {

    private final Tracer tracer;

    public TracingUtil(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * 현재 Span 가져오기
     */
    public Span currentSpan() {
        return Span.current();
    }

    /**
     * Span 시작 (자동 종료 X, 수동 관리 필요)
     */
    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
    }

    /**
     * Span 시작 with SpanKind
     */
    public Span startSpan(String spanName, SpanKind kind) {
        return tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();
    }

    /**
     * Span 내에서 코드 실행 (자동 종료)
     *
     * @param spanName Span 이름
     * @param runnable 실행할 코드
     */
    public void withSpan(String spanName, Runnable runnable) {
        Span span = startSpan(spanName);
        try (Scope scope = span.makeCurrent()) {
            runnable.run();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Span 내에서 코드 실행하고 결과 반환 (자동 종료)
     *
     * @param spanName Span 이름
     * @param supplier 실행할 코드
     * @return 결과 값
     */
    public <T> T withSpan(String spanName, Supplier<T> supplier) {
        Span span = startSpan(spanName);
        try (Scope scope = span.makeCurrent()) {
            return supplier.get();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * 현재 Span에 속성 추가
     */
    public void addAttribute(String key, String value) {
        currentSpan().setAttribute(key, value);
    }

    /**
     * 현재 Span에 속성 추가 (long)
     */
    public void addAttribute(String key, long value) {
        currentSpan().setAttribute(key, value);
    }

    /**
     * 현재 Span에 속성 추가 (boolean)
     */
    public void addAttribute(String key, boolean value) {
        currentSpan().setAttribute(key, value);
    }

    /**
     * 현재 Span에 이벤트 추가
     */
    public void addEvent(String eventName) {
        currentSpan().addEvent(eventName);
    }

    /**
     * 현재 Span에 예외 기록
     */
    public void recordException(Throwable throwable) {
        currentSpan().recordException(throwable);
        currentSpan().setStatus(StatusCode.ERROR, throwable.getMessage());
    }

    /**
     * Span 상태 설정
     */
    public void setStatus(StatusCode statusCode, String description) {
        currentSpan().setStatus(statusCode, description);
    }

    /**
     * Trace ID 가져오기
     */
    public String getTraceId() {
        return currentSpan().getSpanContext().getTraceId();
    }

    /**
     * Span ID 가져오기
     */
    public String getSpanId() {
        return currentSpan().getSpanContext().getSpanId();
    }

    /**
     * 현재 Span이 유효한지 확인
     */
    public boolean isSpanValid() {
        return currentSpan().getSpanContext().isValid();
    }
}
