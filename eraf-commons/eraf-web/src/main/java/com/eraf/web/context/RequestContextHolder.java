package com.eraf.web.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * RequestContext ThreadLocal 홀더
 * HTTP 요청 컨텍스트를 ThreadLocal에 저장하고 관리
 */
public class RequestContextHolder {

    private static final Logger log = LoggerFactory.getLogger(RequestContextHolder.class);

    private static final ThreadLocal<RequestContext> contextHolder = new InheritableThreadLocal<>();

    private RequestContextHolder() {
        // Utility class
    }

    /**
     * 현재 스레드의 RequestContext 설정
     */
    public static void setContext(RequestContext context) {
        if (context == null) {
            log.warn("Attempting to set null RequestContext");
            return;
        }
        contextHolder.set(context);
        log.debug("RequestContext set: {}", context);
    }

    /**
     * 현재 스레드의 RequestContext 조회
     */
    public static RequestContext getContext() {
        return contextHolder.get();
    }

    /**
     * 현재 스레드의 RequestContext 조회 (Optional)
     */
    public static Optional<RequestContext> getContextOptional() {
        return Optional.ofNullable(contextHolder.get());
    }

    /**
     * RequestContext가 존재하는지 확인
     */
    public static boolean hasContext() {
        return contextHolder.get() != null;
    }

    /**
     * 현재 스레드의 RequestContext 제거
     */
    public static void clear() {
        RequestContext context = contextHolder.get();
        if (context != null) {
            log.debug("RequestContext cleared: {}", context.getRequestId());
        }
        contextHolder.remove();
    }

    // 편의 메서드들

    /**
     * 현재 요청의 Request ID 조회
     */
    public static String getRequestId() {
        return getContextOptional()
                .map(RequestContext::getRequestId)
                .orElse(null);
    }

    /**
     * 현재 요청의 Trace ID 조회
     */
    public static String getTraceId() {
        return getContextOptional()
                .map(RequestContext::getTraceId)
                .orElse(null);
    }

    /**
     * 현재 요청의 User ID 조회
     */
    public static String getUserId() {
        return getContextOptional()
                .map(RequestContext::getUserId)
                .orElse(null);
    }

    /**
     * 현재 요청의 Username 조회
     */
    public static String getUsername() {
        return getContextOptional()
                .map(RequestContext::getUsername)
                .orElse(null);
    }

    /**
     * 현재 요청의 Client IP 조회
     */
    public static String getClientIp() {
        return getContextOptional()
                .map(RequestContext::getClientIp)
                .orElse(null);
    }

    /**
     * 현재 요청의 User-Agent 조회
     */
    public static String getUserAgent() {
        return getContextOptional()
                .map(RequestContext::getUserAgent)
                .orElse(null);
    }

    /**
     * 현재 요청의 URI 조회
     */
    public static String getUri() {
        return getContextOptional()
                .map(RequestContext::getUri)
                .orElse(null);
    }

    /**
     * 현재 요청의 HTTP Method 조회
     */
    public static String getMethod() {
        return getContextOptional()
                .map(RequestContext::getMethod)
                .orElse(null);
    }

    /**
     * 커스텀 속성 저장 (새 컨텍스트 생성)
     */
    public static void setAttribute(String name, Object value) {
        RequestContext current = getContext();
        if (current == null) {
            log.warn("Cannot set attribute '{}' because RequestContext is not initialized", name);
            return;
        }

        // 불변 객체이므로 새로운 컨텍스트 생성
        RequestContext newContext = RequestContext.builder()
                .requestId(current.getRequestId())
                .traceId(current.getTraceId())
                .sessionId(current.getSessionId())
                .userId(current.getUserId())
                .username(current.getUsername())
                .clientIp(current.getClientIp())
                .userAgent(current.getUserAgent())
                .method(current.getMethod())
                .uri(current.getUri())
                .queryString(current.getQueryString())
                .locale(current.getLocale())
                .requestTime(current.getRequestTime())
                .headers(current.getHeaders())
                .attributes(current.getAttributes())
                .attribute(name, value)
                .build();

        setContext(newContext);
    }

    /**
     * 커스텀 속성 조회
     */
    public static <T> T getAttribute(String name) {
        return getContextOptional()
                .map(ctx -> ctx.<T>getAttribute(name))
                .orElse(null);
    }
}
