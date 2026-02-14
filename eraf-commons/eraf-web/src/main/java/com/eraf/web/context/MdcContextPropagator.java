package com.eraf.web.context;

import org.slf4j.MDC;

/**
 * MDC (Mapped Diagnostic Context) 전파 유틸리티
 * RequestContext의 정보를 SLF4J MDC에 전파하여 로그에서 사용 가능하게 함
 */
public class MdcContextPropagator {

    // MDC 키 상수
    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";
    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String CLIENT_IP = "clientIp";
    public static final String METHOD = "method";
    public static final String URI = "uri";

    private MdcContextPropagator() {
        // Utility class
    }

    /**
     * RequestContext의 정보를 MDC에 전파
     */
    public static void propagate(RequestContext context) {
        if (context == null) {
            return;
        }

        putIfNotNull(REQUEST_ID, context.getRequestId());
        putIfNotNull(TRACE_ID, context.getTraceId());
        putIfNotNull(SESSION_ID, context.getSessionId());
        putIfNotNull(USER_ID, context.getUserId());
        putIfNotNull(USERNAME, context.getUsername());
        putIfNotNull(CLIENT_IP, context.getClientIp());
        putIfNotNull(METHOD, context.getMethod());
        putIfNotNull(URI, context.getUri());
    }

    /**
     * RequestContextHolder에서 현재 컨텍스트를 가져와 MDC에 전파
     */
    public static void propagateFromHolder() {
        RequestContext context = RequestContextHolder.getContext();
        if (context != null) {
            propagate(context);
        }
    }

    /**
     * MDC 초기화
     */
    public static void clear() {
        MDC.remove(REQUEST_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(SESSION_ID);
        MDC.remove(USER_ID);
        MDC.remove(USERNAME);
        MDC.remove(CLIENT_IP);
        MDC.remove(METHOD);
        MDC.remove(URI);
    }

    /**
     * 특정 키의 MDC 값 조회
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * 특정 키에 MDC 값 설정
     */
    public static void put(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * null이 아닐 때만 MDC에 추가
     */
    private static void putIfNotNull(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * 현재 MDC의 Request ID 조회
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * 현재 MDC의 Trace ID 조회
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 현재 MDC의 User ID 조회
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * 현재 MDC의 Client IP 조회
     */
    public static String getClientIp() {
        return MDC.get(CLIENT_IP);
    }
}
