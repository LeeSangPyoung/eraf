package com.eraf.gateway.analytics;

import com.eraf.core.http.HttpUtils;
import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.filter.ApiKeyAuthFilter;
import com.eraf.gateway.domain.ApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;

/**
 * API Analytics 필터
 * 모든 요청/응답을 기록
 */
@Slf4j
@RequiredArgsConstructor
public class AnalyticsFilter extends OncePerRequestFilter {

    private final AnalyticsService analyticsService;
    private final List<String> excludePatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

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
            Object apiKeyAttr = request.getAttribute(ApiKeyAuthFilter.API_KEY_ATTRIBUTE);
            if (apiKeyAttr instanceof ApiKey) {
                apiKeyName = ((ApiKey) apiKeyAttr).getName();
            }

            // TraceId
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId == null) {
                traceId = response.getHeader("X-Trace-Id");
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

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
