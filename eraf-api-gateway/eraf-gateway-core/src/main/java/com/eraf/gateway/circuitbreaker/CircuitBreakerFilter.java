package com.eraf.gateway.circuitbreaker;

import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.exception.GatewayErrorCode;
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
 * Circuit Breaker 필터
 */
@Slf4j
@RequiredArgsConstructor
public class CircuitBreakerFilter extends OncePerRequestFilter {

    private final CircuitBreakerRegistry registry;
    private final List<String> excludePatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 경로 기반 Circuit Breaker 이름 생성
        String circuitBreakerName = getCircuitBreakerName(path);
        CircuitBreaker circuitBreaker = registry.getOrCreate(circuitBreakerName);

        if (!circuitBreaker.allowRequest()) {
            log.warn("Circuit breaker [{}] is OPEN, rejecting request to {}", circuitBreakerName, path);

            response.setHeader("X-Circuit-Breaker", circuitBreakerName);
            GatewayResponseUtils.sendError(response, GatewayErrorCode.CIRCUIT_BREAKER_OPEN);
            return;
        }

        try {
            filterChain.doFilter(request, response);

            // 응답 상태 코드로 성공/실패 판단
            int status = response.getStatus();
            if (status >= 500) {
                circuitBreaker.recordFailure();
            } else {
                circuitBreaker.recordSuccess();
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    private String getCircuitBreakerName(String path) {
        // /api/users/123 -> api-users
        String[] parts = path.split("/");
        if (parts.length >= 3) {
            return parts[1] + "-" + parts[2];
        } else if (parts.length >= 2) {
            return parts[1];
        }
        return "default";
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
