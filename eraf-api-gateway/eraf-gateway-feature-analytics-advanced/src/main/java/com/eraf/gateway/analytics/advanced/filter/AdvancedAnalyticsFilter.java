package com.eraf.gateway.analytics.advanced.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.gateway.analytics.advanced.domain.AdvancedApiCall;
import com.eraf.gateway.analytics.advanced.export.DatadogExporter;
import com.eraf.gateway.analytics.advanced.export.PrometheusExporter;
import com.eraf.gateway.analytics.advanced.service.AdvancedAnalyticsService;
import com.eraf.gateway.common.filter.GatewayFilter;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 고급 분석 필터
 * 더 세부적인 메트릭 수집 및 비동기 기록
 */
@Slf4j
@RequiredArgsConstructor
public class AdvancedAnalyticsFilter extends GatewayFilter {

    private final AdvancedAnalyticsService analyticsService;
    private final PrometheusExporter prometheusExporter;
    private final DatadogExporter datadogExporter;
    private final boolean enabled;
    private final boolean asyncRecording;
    private final boolean exportToPrometheus;
    private final boolean exportToDatadog;

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, ServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startTime = System.nanoTime();
        long gatewayStartTime = System.currentTimeMillis();
        String errorCode = null;
        String errorMessage = null;

        // Upstream 시작 시간 (실제로는 upstream 호출 직전에 측정)
        long upstreamStartTime = 0;
        long upstreamEndTime = 0;

        try {
            // Gateway 처리 시작
            upstreamStartTime = System.currentTimeMillis();

            filterChain.doFilter(requestWrapper, responseWrapper);

            // Upstream 처리 완료
            upstreamEndTime = System.currentTimeMillis();
        } catch (Exception e) {
            upstreamEndTime = System.currentTimeMillis();
            errorCode = "INTERNAL_ERROR";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long endTime = System.nanoTime();
            long totalLatencyMs = (endTime - startTime) / 1_000_000;
            long upstreamLatencyMs = upstreamEndTime - upstreamStartTime;
            long gatewayLatencyMs = totalLatencyMs - upstreamLatencyMs;

            // API Call 정보 수집
            AdvancedApiCall apiCall = buildAdvancedApiCall(
                    requestWrapper,
                    responseWrapper,
                    totalLatencyMs,
                    upstreamLatencyMs,
                    gatewayLatencyMs,
                    errorCode,
                    errorMessage
            );

            // 비동기 또는 동기로 기록
            if (asyncRecording) {
                analyticsService.recordWithDimensionsAsync(apiCall);
            } else {
                analyticsService.recordWithDimensions(apiCall);
            }

            // Prometheus 익스포트
            if (exportToPrometheus && prometheusExporter != null) {
                prometheusExporter.recordApiCall(apiCall);
            }

            // Datadog 익스포트
            if (exportToDatadog && datadogExporter != null) {
                datadogExporter.recordApiCall(apiCall);
            }

            // 응답 본문 복사
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * AdvancedApiCall 객체 생성
     */
    private AdvancedApiCall buildAdvancedApiCall(
            HttpServletRequest request,
            ContentCachingResponseWrapper response,
            long totalLatencyMs,
            long upstreamLatencyMs,
            long gatewayLatencyMs,
            String errorCode,
            String errorMessage) {

        // API Key 또는 JWT subject 추출
        String consumerIdentifier = extractConsumerIdentifier(request);
        String authMethod = extractAuthMethod(request);

        // Cache 정보
        boolean cacheHit = "HIT".equals(request.getAttribute("CACHE_STATUS"));
        String cacheKey = (String) request.getAttribute("CACHE_KEY");

        // Custom dimensions
        Map<String, String> customDimensions = extractCustomDimensions(request);
        String region = customDimensions.getOrDefault("region", "unknown");
        String clientType = customDimensions.getOrDefault("client_type", "unknown");
        String version = extractApiVersion(request);

        // TraceId
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = response.getHeader("X-Trace-Id");
        }
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        return AdvancedApiCall.builder()
                .id(UUID.randomUUID().toString())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .clientIp(HttpUtils.getClientIp(request))
                .apiKey((String) request.getAttribute("API_KEY"))
                .statusCode(response.getStatus())
                .totalLatencyMs(totalLatencyMs)
                .upstreamLatencyMs(upstreamLatencyMs)
                .gatewayLatencyMs(gatewayLatencyMs)
                .requestSize(request.getContentLength() > 0 ? request.getContentLength() : 0)
                .responseSize(response.getContentSize())
                .cacheHit(cacheHit)
                .cacheKey(cacheKey)
                .authMethod(authMethod)
                .consumerIdentifier(consumerIdentifier)
                .region(region)
                .clientType(clientType)
                .version(version)
                .customDimensions(customDimensions)
                .userAgent(request.getHeader("User-Agent"))
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Consumer identifier 추출 (API Key 또는 JWT subject)
     */
    private String extractConsumerIdentifier(HttpServletRequest request) {
        // API Key
        String apiKey = (String) request.getAttribute("API_KEY");
        if (apiKey != null) {
            return apiKey;
        }

        // JWT subject
        String jwtSubject = (String) request.getAttribute("JWT_SUBJECT");
        if (jwtSubject != null) {
            return jwtSubject;
        }

        // Client ID from header
        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null) {
            return clientId;
        }

        return "anonymous";
    }

    /**
     * Authentication method 추출
     */
    private String extractAuthMethod(HttpServletRequest request) {
        if (request.getAttribute("API_KEY") != null) {
            return "API_KEY";
        }
        if (request.getAttribute("JWT_SUBJECT") != null) {
            return "JWT";
        }
        if (request.getHeader("Authorization") != null) {
            String auth = request.getHeader("Authorization");
            if (auth.startsWith("Bearer ")) {
                return "BEARER_TOKEN";
            }
            if (auth.startsWith("Basic ")) {
                return "BASIC";
            }
        }
        return "NONE";
    }

    /**
     * Custom dimensions 추출
     */
    private Map<String, String> extractCustomDimensions(HttpServletRequest request) {
        Map<String, String> dimensions = new HashMap<>();

        // Region from header
        String region = request.getHeader("X-Region");
        if (region != null) {
            dimensions.put("region", region);
        }

        // Client type from User-Agent or header
        String clientType = request.getHeader("X-Client-Type");
        if (clientType == null) {
            clientType = detectClientType(request.getHeader("User-Agent"));
        }
        dimensions.put("client_type", clientType);

        // Application name
        String appName = request.getHeader("X-App-Name");
        if (appName != null) {
            dimensions.put("app_name", appName);
        }

        // Environment
        String env = request.getHeader("X-Environment");
        if (env != null) {
            dimensions.put("environment", env);
        }

        return dimensions;
    }

    /**
     * API version 추출
     */
    private String extractApiVersion(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/v1")) {
            return "v1";
        } else if (path.startsWith("/v2")) {
            return "v2";
        } else if (path.startsWith("/v3")) {
            return "v3";
        }

        String version = request.getHeader("X-API-Version");
        return version != null ? version : "unknown";
    }

    /**
     * User-Agent에서 클라이언트 타입 감지
     */
    private String detectClientType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }

        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "mobile";
        } else if (ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari")) {
            return "web";
        } else if (ua.contains("okhttp") || ua.contains("java") || ua.contains("python")) {
            return "api";
        }

        return "unknown";
    }
}
