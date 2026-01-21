package com.eraf.starter.serviceclient;

import com.eraf.core.context.ErafContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * ERAF Client 요청 인터셉터
 * JWT, TraceId, UserId 자동 전파
 */
public class ErafClientRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public void apply(RequestTemplate template) {
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

        // Authorization 헤더는 이미 있으면 전파 (JWT 토큰)
        // 실제 토큰은 SecurityContext나 Request에서 가져와야 함
    }
}
