package com.eraf.web.filter;

import com.eraf.core.logging.TraceContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * HTTP 요청/응답 로깅 필터
 * 모든 HTTP 요청과 응답을 로깅하고 TraceId를 관리
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final boolean includePayload;
    private final int maxPayloadLength;
    private final List<String> excludePatterns;
    private final double sampleRate;

    public RequestLoggingFilter() {
        this(true, 1000, Arrays.asList("/actuator", "/health", "/favicon.ico"), 1.0);
    }

    public RequestLoggingFilter(boolean includePayload, int maxPayloadLength, List<String> excludePatterns) {
        this(includePayload, maxPayloadLength, excludePatterns, 1.0);
    }

    public RequestLoggingFilter(boolean includePayload, int maxPayloadLength, List<String> excludePatterns, double sampleRate) {
        this.includePayload = includePayload;
        this.maxPayloadLength = maxPayloadLength;
        this.excludePatterns = excludePatterns;
        this.sampleRate = Math.max(0.0, Math.min(1.0, sampleRate));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 제외 패턴 체크
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 샘플링: sampleRate 확률로만 로깅 수행
        boolean shouldLog = (sampleRate >= 1.0) || (ThreadLocalRandom.current().nextDouble() < sampleRate);

        // TraceId 설정
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // TraceContext 설정
        TraceContextHolder.setTraceId(traceId);
        TraceContextHolder.setRequestId(requestId);
        TraceContextHolder.setClientIp(getClientIp(request));

        // 응답 헤더에 TraceId 추가
        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // 요청/응답 래핑 (페이로드 로깅용)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // 요청 로깅 (샘플링 적용)
            if (shouldLog) {
                logRequest(wrappedRequest, traceId);
            }

            // 필터 체인 실행
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 응답 로깅 (샘플링 적용)
            if (shouldLog) {
                logResponse(wrappedRequest, wrappedResponse, duration, traceId);
            }

            // 응답 본문 복사 (필수!)
            wrappedResponse.copyBodyToResponse();

            // TraceContext 정리
            TraceContextHolder.clear();
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludePatterns.stream().anyMatch(path::startsWith);
    }

    private void logRequest(ContentCachingRequestWrapper request, String traceId) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== HTTP REQUEST ==========\n");
        sb.append(String.format("TraceId: %s%n", traceId));
        sb.append(String.format("Method: %s%n", request.getMethod()));
        sb.append(String.format("URI: %s%n", request.getRequestURI()));
        sb.append(String.format("Query: %s%n", request.getQueryString()));
        sb.append(String.format("Client IP: %s%n", getClientIp(request)));
        sb.append(String.format("User-Agent: %s%n", request.getHeader("User-Agent")));

        if (includePayload && isJsonRequest(request)) {
            String payload = getRequestPayload(request);
            if (payload != null && !payload.isEmpty()) {
                sb.append(String.format("Payload: %s%n", truncate(payload)));
            }
        }

        sb.append("==================================");
        log.info(sb.toString());
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                             long duration, String traceId) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== HTTP RESPONSE ==========\n");
        sb.append(String.format("TraceId: %s%n", traceId));
        sb.append(String.format("Method: %s%n", request.getMethod()));
        sb.append(String.format("URI: %s%n", request.getRequestURI()));
        sb.append(String.format("Status: %d%n", response.getStatus()));
        sb.append(String.format("Duration: %dms%n", duration));

        if (includePayload && isJsonResponse(response)) {
            String payload = getResponsePayload(response);
            if (payload != null && !payload.isEmpty()) {
                sb.append(String.format("Payload: %s%n", truncate(payload)));
            }
        }

        sb.append("===================================");

        if (response.getStatus() >= 400) {
            log.warn(sb.toString());
        } else {
            log.info(sb.toString());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 헤더에 여러 IP가 있을 경우 첫 번째 IP 추출
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private boolean isJsonResponse(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String getRequestPayload(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getResponsePayload(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String truncate(String payload) {
        if (payload == null) {
            return null;
        }
        // 민감 정보 마스킹
        payload = maskSensitiveData(payload);

        if (payload.length() > maxPayloadLength) {
            return payload.substring(0, maxPayloadLength) + "...(truncated)";
        }
        return payload;
    }

    private String maskSensitiveData(String payload) {
        // 비밀번호, 토큰, 개인정보 등 민감 정보 마스킹
        return payload
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"****\"")
                .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"****\"")
                .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"****\"")
                .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"****\"")
                .replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"****\"")
                .replaceAll("\"creditCard\"\\s*:\\s*\"[^\"]*\"", "\"creditCard\":\"****\"")
                .replaceAll("\"ssn\"\\s*:\\s*\"[^\"]*\"", "\"ssn\":\"****\"")
                .replaceAll("\"apiKey\"\\s*:\\s*\"[^\"]*\"", "\"apiKey\":\"****\"")
                .replaceAll("\"apiSecret\"\\s*:\\s*\"[^\"]*\"", "\"apiSecret\":\"****\"")
                .replaceAll("\"pinCode\"\\s*:\\s*\"[^\"]*\"", "\"pinCode\":\"****\"")
                .replaceAll("\"authorization\"\\s*:\\s*\"[^\"]*\"", "\"authorization\":\"****\"")
                .replaceAll("\"cardNumber\"\\s*:\\s*\"[^\"]*\"", "\"cardNumber\":\"****\"")
                .replaceAll("\"cvv\"\\s*:\\s*\"[^\"]*\"", "\"cvv\":\"****\"")
                .replaceAll("\"privateKey\"\\s*:\\s*\"[^\"]*\"", "\"privateKey\":\"****\"");
    }
}
