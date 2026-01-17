package com.eraf.gateway.ratelimit.advanced.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.filter.GatewayFilter;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import com.eraf.gateway.ratelimit.advanced.service.AdvancedRateLimitService;
import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import com.eraf.gateway.ratelimit.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 고급 Rate Limiting 필터
 * - 다양한 알고리즘 지원
 * - 분산 제한 (Redis)
 * - Consumer별 제한
 * - 헤더 기반 제한
 */
@Slf4j
@RequiredArgsConstructor
public class AdvancedRateLimitFilter extends GatewayFilter {

    private final AdvancedRateLimitService rateLimitService;
    private final boolean enabled;

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = request.getRequestURI();
        String clientIp = HttpUtils.getClientIp(request);

        // 헤더 정보 추출
        Map<String, String> headers = extractHeaders(request);

        try {
            // IP 기반 Rate Limit 체크
            rateLimitService.checkRateLimit(path, clientIp, RateLimitRule.RateLimitType.IP, headers);

            // Rate Limit 정보를 응답 헤더에 추가
            AdvancedRateLimitService.RateLimitInfo info = rateLimitService.getRateLimitInfo(
                    path, clientIp, RateLimitRule.RateLimitType.IP);

            if (info != null) {
                addRateLimitHeaders(httpResponse, info);
            }

            chain.doFilter(request, response);

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            handleRateLimitExceeded(httpResponse, e);
        }
    }

    /**
     * 요청 헤더 추출
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        return headers;
    }

    /**
     * Rate Limit 헤더 추가
     */
    private void addRateLimitHeaders(HttpServletResponse response, AdvancedRateLimitService.RateLimitInfo info) {
        // 표준 Rate Limit 헤더
        response.setHeader("X-RateLimit-Limit", String.valueOf(info.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(info.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTimeSeconds()));

        // 알고리즘 정보
        response.setHeader("X-RateLimit-Algorithm", info.getAlgorithm());

        // 추가 정보 (draft-ietf-httpapi-ratelimit-headers 준수)
        response.setHeader("RateLimit-Limit", String.valueOf(info.getLimit()));
        response.setHeader("RateLimit-Remaining", String.valueOf(info.getRemaining()));
        response.setHeader("RateLimit-Reset", String.valueOf(info.getResetTimeSeconds()));
    }

    /**
     * Rate Limit 초과 처리
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitExceededException e) throws IOException {
        // Retry-After 헤더 추가
        response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));

        // Rate Limit 헤더 추가
        response.setHeader("X-RateLimit-Limit", String.valueOf(e.getLimit()));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("RateLimit-Limit", String.valueOf(e.getLimit()));
        response.setHeader("RateLimit-Remaining", "0");

        // 에러 응답
        GatewayResponseUtils.sendError(response, GatewayErrorCode.RATE_LIMIT_EXCEEDED);
    }
}
