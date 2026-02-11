package com.eraf.openapi.filter;

import com.eraf.openapi.core.domain.GatewayApi;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 인증 필터 (WebFlux용)
 * <p>
 * Authorization 헤더에서 JWT 토큰을 추출하여 검증합니다.
 * 유효하지 않은 토큰은 401 Unauthorized로 차단합니다.
 * <p>
 * Order: 150 (ApiValidationFilter(100) 이후, 기타 필터 이전)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter, Ordered {

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final String JWT_AUTH_ATTR = "JWT_AUTHENTICATED";
    public static final String JWT_USERNAME_ATTR = "JWT_USERNAME";
    private static final int ORDER = 150;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        log.debug("JwtAuthFilter: {} {}", method, path);

        // JWT가 비활성화된 경우 통과
        if (!properties.isEnabled()) {
            log.debug("JWT authentication is disabled");
            return chain.filter(exchange);
        }

        // Skip 패턴에 해당하는 경우 통과
        if (shouldSkip(path)) {
            log.debug("Skipping JWT authentication for path: {}", path);
            return chain.filter(exchange);
        }

        // CORS preflight OPTIONS 요청은 통과 (브라우저가 토큰 없이 보냄)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Skipping JWT authentication for OPTIONS request: {}", path);
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("JWT token is missing for path: {}", path);
            return sendUnauthorizedResponse(exchange, "Missing or invalid authorization token");
        }

        // 토큰 유효성 검증
        if (!tokenProvider.validateToken(token)) {
            log.warn("JWT token validation failed for path: {}", path);
            return sendUnauthorizedResponse(exchange, "Invalid or expired token");
        }

        // 토큰에서 사용자 정보 추출
        try {
            String username = tokenProvider.getUsername(token);
            log.debug("JWT authentication successful for user: {} on path: {}", username, path);

            // Exchange attributes에 인증 정보 저장
            exchange.getAttributes().put(JWT_AUTH_ATTR, true);
            exchange.getAttributes().put(JWT_USERNAME_ATTR, username);

            // 토큰에서 사용자 권한 추출
            org.springframework.security.core.Authentication authentication =
                    tokenProvider.getAuthentication(token);

            List<String> userRoles = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .toList();

            // Admin API는 ROLE_ADMIN 필수
            if (path.startsWith("/admin/")) {
                if (!userRoles.contains("ROLE_ADMIN")) {
                    log.warn("User {} lacks ROLE_ADMIN for Admin API {} {}: user roles={}",
                            username, method, path, userRoles);
                    return sendForbiddenResponse(exchange, "Admin access required");
                }
                log.debug("Admin authorization successful for user: {} with roles: {} on path: {}",
                        username, userRoles, path);
            }

            // Role-based Authorization 체크 (프록시 API용)
            com.eraf.openapi.core.domain.GatewayApi matchedApi =
                    exchange.getAttribute(ApiValidationFilter.MATCHED_API_ATTR);

            if (matchedApi != null && matchedApi.getRequiredRoles() != null
                    && !matchedApi.getRequiredRoles().isEmpty()) {

                // 필요한 권한 중 하나라도 가지고 있는지 확인
                boolean hasRequiredRole = userRoles.stream()
                        .anyMatch(matchedApi.getRequiredRoles()::contains);

                if (!hasRequiredRole) {
                    log.warn("User {} lacks required roles for {} {}: required={}, user={}",
                            username, method, path, matchedApi.getRequiredRoles(), userRoles);
                    return sendForbiddenResponse(exchange, "Insufficient permissions");
                }

                log.debug("Role authorization successful for user: {} with roles: {} on path: {}",
                        username, userRoles, path);
            }

            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            return sendUnauthorizedResponse(exchange, "Token processing failed");
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(properties.getHeaderName());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenPrefix())) {
            return bearerToken.substring(properties.getTokenPrefix().length());
        }

        return null;
    }

    /**
     * Skip 패턴 확인
     */
    private boolean shouldSkip(String path) {
        if (properties.getSkipPatterns() == null) {
            return false;
        }

        return Arrays.stream(properties.getSkipPatterns())
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 401 Unauthorized 응답 전송
     */
    private Mono<Void> sendUnauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                message,
                exchange.getRequest().getPath().value()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private Mono<Void> sendForbiddenResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                message,
                exchange.getRequest().getPath().value()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
