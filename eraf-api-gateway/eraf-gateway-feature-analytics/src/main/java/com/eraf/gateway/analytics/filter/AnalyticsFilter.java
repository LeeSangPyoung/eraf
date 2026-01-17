package com.eraf.gateway.analytics.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.gateway.common.filter.GatewayFilter;
import com.eraf.gateway.analytics.service.AnalyticsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * API Analytics 필터
 * 모든 요청/응답을 기록
 */
@Slf4j
@RequiredArgsConstructor
public class AnalyticsFilter extends GatewayFilter {

    private final AnalyticsService analyticsService;
    private final boolean enabled;

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, ServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startTime = System.currentTimeMillis();
        String errorCode = null;
        String errorMessage = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            errorCode = "INTERNAL_ERROR";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;

            // API Key 정보 (있는 경우)
            String apiKeyName = null;
            Object apiKeyAttr = request.getAttribute("API_KEY");
            if (apiKeyAttr != null) {
                apiKeyName = apiKeyAttr.toString();
            }

            // TraceId
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId == null) {
                traceId = responseWrapper.getHeader("X-Trace-Id");
            }

            analyticsService.recordApiCall(
                    path,
                    request.getMethod(),
                    HttpUtils.getClientIp(request),
                    apiKeyName,
                    responseWrapper.getStatus(),
                    responseTime,
                    requestWrapper.getContentLength(),
                    responseWrapper.getContentSize(),
                    request.getHeader("User-Agent"),
                    traceId,
                    errorCode,
                    errorMessage
            );

            // 응답 본문 복사
            responseWrapper.copyBodyToResponse();
        }
    }
}
