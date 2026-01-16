package com.eraf.gateway.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.domain.RateLimitRule;
import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.exception.RateLimitExceededException;
import com.eraf.gateway.service.RateLimitService;
import com.eraf.gateway.util.GatewayResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Rate Limiting 필터
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final List<String> excludePatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 제외 패턴 확인
        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = HttpUtils.getClientIp(request);

        try {
            // IP 기반 Rate Limit 체크
            rateLimitService.checkRateLimit(path, clientIp, RateLimitRule.RateLimitType.IP);

            // Rate Limit 정보를 응답 헤더에 추가
            RateLimitService.RateLimitInfo info = rateLimitService.getRateLimitInfo(
                    path, clientIp, RateLimitRule.RateLimitType.IP);

            if (info != null) {
                response.setHeader("X-RateLimit-Limit", String.valueOf(info.getLimit()));
                response.setHeader("X-RateLimit-Remaining", String.valueOf(info.getRemaining()));
                response.setHeader("X-RateLimit-Reset", String.valueOf(info.getResetTimeSeconds()));
            }

            filterChain.doFilter(request, response);

        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);

            response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            response.setHeader("X-RateLimit-Limit", String.valueOf(e.getLimit()));
            response.setHeader("X-RateLimit-Remaining", "0");

            GatewayResponseUtils.sendError(response, GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
