package com.eraf.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenProvider 테스트
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret("mySecretKeyForTestingPurposesMustBeLongEnough256bits");
        properties.setAccessTokenExpiration(Duration.ofHours(1));
        properties.setRefreshTokenExpiration(Duration.ofDays(1));
        properties.setIssuer("eraf-test");

        tokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("액세스 토큰 생성")
    void shouldCreateAccessToken() {
        // createAccessToken(String username, String userId, Collection<String> roles)
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("리프레시 토큰 생성")
    void shouldCreateRefreshToken() {
        // createRefreshToken(String username, String userId)
        String token = tokenProvider.createRefreshToken("testuser", "user123");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("토큰 검증 성공")
    void shouldValidateValidToken() {
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void shouldFailValidationForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void shouldFailValidationForEmptyToken() {
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출")
    void shouldExtractUserIdFromToken() {
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        String userId = tokenProvider.getUserId(token);

        assertEquals("user123", userId);
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출")
    void shouldExtractUsernameFromToken() {
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        String username = tokenProvider.getUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("토큰에서 역할 추출")
    void shouldExtractRolesFromToken() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        String token = tokenProvider.createAccessToken("testuser", "user123", roles);

        Authentication authentication = tokenProvider.getAuthentication(token);
        List<String> extractedRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("ROLE_USER"));
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("토큰에서 Authentication 객체 생성")
    void shouldCreateAuthenticationFromToken() {
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        Authentication authentication = tokenProvider.getAuthentication(token);

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("testuser", authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("ErafUserDetails Principal 반환")
    void shouldReturnErafUserDetailsAsPrincipal() {
        String token = tokenProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        Authentication authentication = tokenProvider.getAuthentication(token);
        Object principal = authentication.getPrincipal();

        assertInstanceOf(ErafUserDetails.class, principal);
        ErafUserDetails userDetails = (ErafUserDetails) principal;
        assertEquals("user123", userDetails.getUserId());
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void shouldFailValidationForExpiredToken() {
        JwtProperties shortExpiry = new JwtProperties();
        shortExpiry.setSecret("mySecretKeyForTestingPurposesMustBeLongEnough256bits");
        shortExpiry.setAccessTokenExpiration(Duration.ofMillis(1)); // 1ms
        shortExpiry.setIssuer("eraf-test");

        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiry);
        String token = shortExpiryProvider.createAccessToken("testuser", "user123", List.of("ROLE_USER"));

        // 토큰 만료 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(shortExpiryProvider.validateToken(token));
    }
}
