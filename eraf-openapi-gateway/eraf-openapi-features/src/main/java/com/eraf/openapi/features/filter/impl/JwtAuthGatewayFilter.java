package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Gateway Filter
 * JWT 토큰 검증 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "jwt-auth";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // JWT 설정 추출
        String secretKey = (String) config.get("secret_key");
        List<String> excludePaths = (List<String>) config.getOrDefault("exclude_paths", List.of());
        boolean required = (boolean) config.getOrDefault("required", true);

        return (exchange, chain) -> applyJwtAuth(exchange, chain, secretKey, excludePaths, required);
    }

    private Mono<Void> applyJwtAuth(ServerWebExchange exchange, GatewayFilterChain chain,
                                      String secretKey, List<String> excludePaths, boolean required) {
        String path = exchange.getRequest().getPath().value();

        // Exclude paths 체크
        if (excludePaths.stream().anyMatch(path::startsWith)) {
            log.debug("Path excluded from JWT auth: {}", path);
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 JWT 토큰 추출
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            if (required) {
                log.warn("Missing or invalid Authorization header");
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.JWT_INVALID);
            }
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            // JWT 토큰 검증 (ERAF Security 모듈 활용)
            // TODO: JwtTokenProvider를 주입받아 실제 검증 로직 구현
            validateJwtToken(token, secretKey);

            // 검증 성공 시 사용자 정보를 헤더에 추가
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", extractUserId(token)))
                    .build();

            log.debug("JWT authentication successful for path: {}", path);
            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.JWT_INVALID, e.getMessage());
        }
    }

    private void validateJwtToken(String token, String secretKey) {
        // TODO: ERAF Security 모듈의 JwtTokenProvider를 활용한 실제 검증
        // 현재는 기본 구조만 구현
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private String extractUserId(String token) {
        // TODO: JWT 토큰에서 사용자 ID 추출
        return "user-from-jwt";
    }
}
