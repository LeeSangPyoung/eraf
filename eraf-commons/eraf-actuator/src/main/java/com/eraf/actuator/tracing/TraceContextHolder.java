package com.eraf.actuator.tracing;

/**
 * 트레이스 컨텍스트 홀더 (ThreadLocal 기반)
 */
public final class TraceContextHolder {

    private static final ThreadLocal<TraceContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    /**
     * 현재 스레드의 트레이스 컨텍스트 조회
     */
    public static TraceContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 현재 스레드에 트레이스 컨텍스트 설정
     */
    public static void setContext(TraceContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 현재 스레드의 트레이스 컨텍스트 제거
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 현재 트레이스 ID 조회
     */
    public static String getTraceId() {
        TraceContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getTraceId() : null;
    }

    /**
     * 현재 스팬 ID 조회
     */
    public static String getSpanId() {
        TraceContext context = CONTEXT_HOLDER.get();
        return context != null ? context.getSpanId() : null;
    }

    /**
     * 자식 스팬 생성
     */
    public static TraceContext createChildSpan() {
        TraceContext current = CONTEXT_HOLDER.get();
        if (current == null) {
            return TraceContext.create();
        }
        return TraceContext.createChild(current);
    }
}
