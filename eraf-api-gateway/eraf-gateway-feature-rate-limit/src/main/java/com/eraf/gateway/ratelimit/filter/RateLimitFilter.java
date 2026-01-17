package com.eraf.gateway.ratelimit.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.filter.GatewayFilter;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import com.eraf.gateway.ratelimit.exception.RateLimitExceededException;
import com.eraf.gateway.ratelimit.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Rate Limiting 필터
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends GatewayFilter {

    private final RateLimitService rateLimitService;
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

        try {
            // IP 기반 Rate Limit 체크
            rateLimitService.checkRateLimit(path, clientIp, RateLimitRule.RateLimitType.IP);

            // Rate Limit 정보를 응답 헤더에 추가
            RateLimitService.RateLimitInfo info = rateLimitService.getRateLimitInfo(
                    path, clientIp, RateLimitRule.RateLimitType.IP);

            if (info != null) {
                httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(info.getLimit()));
                httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(info.getRemaining()));
                httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTimeSeconds()));
            }

            chain.doFilter(request, response);

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);

            httpResponse.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(e.getLimit()));
            httpResponse.setHeader("X-RateLimit-Remaining", "0");

            GatewayResponseUtils.sendError(httpResponse, GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }
}
