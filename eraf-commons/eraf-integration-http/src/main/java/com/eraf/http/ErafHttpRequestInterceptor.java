package com.eraf.http;

import com.eraf.core.context.ErafContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * ERAF HTTP Client 요청 인터셉터
 * JWT, TraceId, UserId 자동 전파
 */
public class ErafHttpRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final boolean contextPropagationEnabled;

    public ErafHttpRequestInterceptor() {
        this(true);
    }

    public ErafHttpRequestInterceptor(boolean contextPropagationEnabled) {
        this.contextPropagationEnabled = contextPropagationEnabled;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!contextPropagationEnabled) {
            return;
        }

        // TraceId 전파
        String traceId = ErafContext.getTraceId();
        if (traceId != null && !template.headers().containsKey(TRACE_ID_HEADER)) {
            template.header(TRACE_ID_HEADER, traceId);
        }

        // RequestId 전파
        String requestId = ErafContext.getRequestId();
        if (requestId != null && !template.headers().containsKey(REQUEST_ID_HEADER)) {
            template.header(REQUEST_ID_HEADER, requestId);
        }

        // UserId 전파
        String userId = ErafContext.getCurrentUserId();
        if (userId != null && !template.headers().containsKey(USER_ID_HEADER)) {
            template.header(USER_ID_HEADER, userId);
        }

        // JWT 토큰 자동 전파 (SecurityContext 또는 RequestAttributes에서 추출)
        if (!template.headers().containsKey(AUTHORIZATION_HEADER)) {
            String token = extractJwtToken();
            if (token != null) {
                template.header(AUTHORIZATION_HEADER, token);
            }
        }
    }

    /**
     * JWT 토큰 추출 (Spring Security → ServletRequest 순서)
     */
    private String extractJwtToken() {
        // 1. Spring Security SecurityContext에서 추출 (Reflection)
        try {
            Class<?> securityContextHolder = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder");
            Object securityContext = securityContextHolder.getMethod("getContext").invoke(null);
            Object authentication = securityContext.getClass().getMethod("getAuthentication").invoke(securityContext);
            if (authentication != null) {
                Object credentials = authentication.getClass().getMethod("getCredentials").invoke(authentication);
                if (credentials instanceof String token && token.startsWith("eyJ")) {
                    return "Bearer " + token;
                }
            }
        } catch (Exception ignored) {
            // Spring Security not available
        }

        // 2. ServletRequest에서 추출 (RequestAttributes)
        try {
            Class<?> requestContextHolder = Class.forName(
                    "org.springframework.web.context.request.RequestContextHolder");
            Object attrs = requestContextHolder.getMethod("currentRequestAttributes").invoke(null);
            Class<?> servletAttrs = Class.forName(
                    "org.springframework.web.context.request.ServletRequestAttributes");
            if (servletAttrs.isInstance(attrs)) {
                Object request = servletAttrs.getMethod("getRequest").invoke(attrs);
                Object authHeader = request.getClass().getMethod("getHeader", String.class)
                        .invoke(request, "Authorization");
                if (authHeader instanceof String header && !header.isEmpty()) {
                    return header;
                }
            }
        } catch (Exception ignored) {
            // Web context not available
        }

        return null;
    }
}
