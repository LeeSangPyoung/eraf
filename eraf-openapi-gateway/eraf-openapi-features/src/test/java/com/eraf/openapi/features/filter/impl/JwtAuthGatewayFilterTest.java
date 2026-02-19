package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JWT Authentication Gateway Filter 테스트")
class JwtAuthGatewayFilterTest {

    private JwtAuthGatewayFilter jwtAuthFilter;
    private GatewayFilterChain mockChain;
    private String secretKey;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthGatewayFilter();
        mockChain = mock(GatewayFilterChain.class);
        secretKey = "test-secret-key-for-jwt-authentication-minimum-256-bits-required";
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        when(mockChain.filter(any(ServerWebExchange.class)))
            .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공")
    void shouldAuthenticateWithValidToken() {
        // Given
        String token = createValidToken("user-123");
        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        // 헤더에 사용자 정보가 추가되었는지 확인
        assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
            .isEqualTo("user-123");
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 실패")
    void shouldFailWithoutAuthorizationHeader() {
        // Given
        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Bearer 접두사가 없으면 인증 실패")
    void shouldFailWithoutBearerPrefix() {
        // Given
        String token = createValidToken("user-123");
        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, token)  // Bearer 없음
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("만료된 토큰으로 인증 실패")
    void shouldFailWithExpiredToken() {
        // Given
        String expiredToken = createExpiredToken("user-123");
        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("잘못된 시크릿 키로 서명된 토큰은 인증 실패")
    void shouldFailWithInvalidSignature() {
        // Given
        String wrongKey = "wrong-secret-key-for-jwt-authentication-minimum-256-bits-required";
        SecretKey wrongSecretKey = Keys.hmacShaKeyFor(wrongKey.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
            .subject("user-123")
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .signWith(wrongSecretKey)
            .compact();

        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("제외 경로는 인증 생략")
    void shouldSkipAuthenticationForExcludedPaths() {
        // Given
        GatewayPlugin plugin = createPlugin(secretKey, List.of("/public", "/health"), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/public/api").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        // 인증 없이 통과되어야 함
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("required=false이고 토큰이 없으면 통과")
    void shouldPassWithoutTokenWhenNotRequired() {
        // Given
        GatewayPlugin plugin = createPlugin(secretKey, List.of(), false);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        // 인증 없이 통과되어야 함
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("토큰에서 사용자 정보와 클레임 추출")
    void shouldExtractUserInfoAndClaims() {
        // Given
        String token = Jwts.builder()
            .subject("user-123")
            .claim("userId", "user-123")
            .claim("username", "john.doe")
            .claim("email", "john@example.com")
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .signWith(key)
            .compact();

        GatewayPlugin plugin = createPlugin(secretKey, List.of(), true);
        GatewayFilter filter = jwtAuthFilter.createFilter(plugin);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When & Then
        StepVerifier.create(filter.filter(exchange, mockChain))
            .verifyComplete();

        assertThat(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
            .isEqualTo("user-123");
        assertThat(exchange.getRequest().getHeaders().getFirst("X-Username"))
            .isEqualTo("john.doe");
    }

    // Helper methods
    private String createValidToken(String userId) {
        return Jwts.builder()
            .subject(userId)
            .claim("userId", userId)
            .issuedAt(new Date())
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .signWith(key)
            .compact();
    }

    private String createExpiredToken(String userId) {
        return Jwts.builder()
            .subject(userId)
            .claim("userId", userId)
            .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
            .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
            .signWith(key)
            .compact();
    }

    private GatewayPlugin createPlugin(String secretKey, List<String> excludePaths, boolean required) {
        GatewayPlugin plugin = new GatewayPlugin();
        plugin.setName("jwt-auth");
        plugin.setConfig(Map.of(
            "secret_key", secretKey,
            "exclude_paths", excludePaths,
            "required", required
        ));
        return plugin;
    }
}
