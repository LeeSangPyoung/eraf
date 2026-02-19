package com.eraf.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Authentication Gateway Filter
 * JWT 토큰 검증 및 인증 처리
 *
 * <p>eraf-security 모듈이 classpath에 있을 경우, JwtTokenProvider를 사용하여
 * 서명 검증, 만료 시간 확인 등 완전한 JWT 검증을 수행합니다.</p>
 *
 * <p>eraf-security 모듈이 없을 경우, 기본적인 형식 검증만 수행합니다.</p>
 */
public class AuthenticationGatewayFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationGatewayFilter.class);
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";

    private final List<String> excludePatterns;
    private final Object jwtTokenProvider;
    private final Method validateTokenMethod;

    /**
     * JwtTokenProvider 없이 생성 (기본 형식 검증만 수행)
     */
    public AuthenticationGatewayFilter(List<String> excludePatterns) {
        this(excludePatterns, null);
    }

    /**
     * JwtTokenProvider와 함께 생성 (완전한 JWT 검증 수행)
     *
     * @param excludePatterns 인증 제외 패턴
     * @param jwtTokenProvider JWT 토큰 검증 제공자 (eraf-security 모듈의 JwtTokenProvider)
     */
    public AuthenticationGatewayFilter(List<String> excludePatterns, Object jwtTokenProvider) {
        this.excludePatterns = excludePatterns != null ? excludePatterns : List.of();
        this.jwtTokenProvider = jwtTokenProvider;

        // JwtTokenProvider의 validateToken 메서드를 리플렉션으로 찾기
        Method method = null;
        if (jwtTokenProvider != null) {
            try {
                method = jwtTokenProvider.getClass().getMethod("validateToken", String.class);
                log.info("AuthenticationGatewayFilter initialized with JwtTokenProvider - Full JWT validation enabled");
            } catch (NoSuchMethodException e) {
                log.warn("JwtTokenProvider.validateToken method not found - Falling back to basic validation");
            }
        } else {
            log.warn("AuthenticationGatewayFilter initialized without JwtTokenProvider - Only basic token format validation will be performed");
        }
        this.validateTokenMethod = method;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 제외 패턴 확인
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // JWT 토큰 검증
        if (!isValidToken(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 토큰이 유효한 경우, 사용자 정보를 헤더에 추가 (선택적)
        // 다운스트림 서비스가 사용할 수 있도록 사용자명 전달
        return chain.filter(exchange);
    }

    private boolean isExcludedPath(String path) {
        return excludePatterns.stream().anyMatch(pattern ->
                path.matches(pattern.replace("*", ".*"))
        );
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * JwtTokenProvider가 있으면 완전한 검증 수행:
     * - 서명 검증
     * - 만료 시간 확인
     * - 형식 검증
     *
     * JwtTokenProvider가 없으면 기본 형식 검증만 수행:
     * - JWT 형식 확인 (3개 파트로 구성)
     */
    private boolean isValidToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        // JwtTokenProvider를 사용한 완전한 검증
        if (jwtTokenProvider != null && validateTokenMethod != null) {
            try {
                return (boolean) validateTokenMethod.invoke(jwtTokenProvider, token);
            } catch (Exception e) {
                log.error("Error validating JWT token with JwtTokenProvider: {}", e.getMessage());
                return false;
            }
        }

        // Fallback: 기본 형식 검증 (3개 파트로 구성되어 있는지만 확인)
        log.debug("Performing basic JWT format validation (signature not verified)");
        return token.split("\\.").length == 3;
    }

    @Override
    public int getOrder() {
        return -50; // Rate Limit 다음 우선순위
    }
}
