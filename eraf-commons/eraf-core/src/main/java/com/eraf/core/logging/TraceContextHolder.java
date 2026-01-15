package com.eraf.core.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * MDC 기반 추적 컨텍스트 관리
 */
public final class TraceContextHolder {

    public static final String TRACE_ID = "traceId";
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String CLIENT_IP = "clientIp";

    private TraceContextHolder() {
    }

    /**
     * Trace ID 설정 (없으면 생성)
     */
    public static String setTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            traceId = generateTraceId();
        }
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * Request ID 설정
     */
    public static String setRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            requestId = generateRequestId();
        }
        MDC.put(REQUEST_ID, requestId);
        return requestId;
    }

    /**
     * User ID 설정
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * Client IP 설정
     */
    public static void setClientIp(String clientIp) {
        if (clientIp != null) {
            MDC.put(CLIENT_IP, clientIp);
        }
    }

    /**
     * Trace ID 조회
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * Request ID 조회
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * User ID 조회
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * Client IP 조회
     */
    public static String getClientIp() {
        return MDC.get(CLIENT_IP);
    }

    /**
     * MDC 전체 초기화
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 특정 키 제거
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * Trace ID 생성
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Request ID 생성
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
