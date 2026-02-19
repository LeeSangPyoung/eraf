package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Gateway Filter
 * JWT 토큰 검증 필터
 *
 * <p>ERAF Security 모듈의 JwtTokenProvider 로직을 활용하여 JWT 검증을 수행합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGatewayFilter implements PluginGatewayFilter, Ordered {

    private static final String USER_ID_KEY = "userId";

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
            // JWT 토큰 검증 (ERAF Security 모듈의 JwtTokenProvider 로직 활용)
            validateJwtToken(token, secretKey);

            // 토큰에서 사용자 정보 추출
            String userId = extractUserId(token, secretKey);
            String username = extractUsername(token, secretKey);

            // 검증 성공 시 사용자 정보를 헤더에 추가
            ServerWebExchange.Builder builder = exchange.mutate()
                    .request(r -> r.header("X-User-Id", userId != null ? userId : "")
                                  .header("X-Username", username != null ? username : ""));

            log.debug("JWT authentication successful for path: {}, userId: {}, username: {}", path, userId, username);
            return chain.filter(builder.build());

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.JWT_INVALID, e.getMessage());
        }
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @param secretKey 시크릿 키
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    private void validateJwtToken(String token, String secretKey) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token is empty");
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            JwtParser parser = Jwts.parser().verifyWith(key).build();
            parser.parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("JWT token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("JWT token unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("JWT token malformed: " + e.getMessage());
        } catch (SecurityException | io.jsonwebtoken.security.SecurityException e) {
            throw new IllegalArgumentException("JWT signature validation failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT token compact of handler are invalid: " + e.getMessage());
        }
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @param secretKey 시크릿 키
     * @return 사용자 ID (없으면 null)
     */
    private String extractUserId(String token, String secretKey) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            JwtParser parser = Jwts.parser().verifyWith(key).build();
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return claims.get(USER_ID_KEY, String.class);
        } catch (Exception e) {
            log.warn("Failed to extract userId from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰에서 사용자명 추출
     *
     * @param token JWT 토큰
     * @param secretKey 시크릿 키
     * @return 사용자명 (subject, 없으면 null)
     */
    private String extractUsername(String token, String secretKey) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            JwtParser parser = Jwts.parser().verifyWith(key).build();
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT: {}", e.getMessage());
            return null;
        }
    }
}
