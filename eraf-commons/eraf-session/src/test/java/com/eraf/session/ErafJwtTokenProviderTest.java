package com.eraf.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErafJwtTokenProviderTest {

    private ErafJwtTokenProvider tokenProvider;
    private static final String SECRET = "mySecretKeyForTestingPurposesOnlyMustBeLongEnough123456";

    @BeforeEach
    void setUp() {
        ErafSessionProperties properties = new ErafSessionProperties();
        ErafSessionProperties.Jwt jwt = properties.getJwt();
        jwt.setSecret(SECRET);
        jwt.setExpiration(Duration.ofHours(1));
        jwt.setRefreshExpiration(Duration.ofDays(7));
        tokenProvider = new ErafJwtTokenProvider(properties);
    }

    @Test
    @DisplayName("Access Token 생성")
    void testCreateAccessToken() {
        // Given
        String userId = "user123";

        // When
        String token = tokenProvider.createAccessToken(userId, null);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("토큰에서 userId 추출")
    void testGetUserId() {
        // Given
        String userId = "user123";
        String token = tokenProvider.createAccessToken(userId, null);

        // When
        String extractedUserId = tokenProvider.getUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void testValidateTokenValid() {
        // Given
        String token = tokenProvider.createAccessToken("user123", null);

        // When
        boolean isValid = tokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 토큰")
    void testValidateTokenInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("커스텀 클레임 포함 토큰 생성")
    void testCreateAccessTokenWithClaims() {
        // Given
        String userId = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("userId", 123L);

        // When
        String token = tokenProvider.createAccessToken(userId, claims);

        // Then
        assertNotNull(token);
        assertEquals(userId, tokenProvider.getUserId(token));
    }

    @Test
    @DisplayName("Refresh Token 생성")
    void testCreateRefreshToken() {
        // Given
        String userId = "user123";

        // When
        String token = tokenProvider.createRefreshToken(userId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("TokenPair 생성")
    void testCreateTokenPair() {
        // Given
        String userId = "user123";

        // When
        ErafJwtTokenProvider.TokenPair pair = tokenProvider.createTokenPair(userId, null);

        // Then
        assertNotNull(pair.getAccessToken());
        assertNotNull(pair.getRefreshToken());
        assertEquals(userId, tokenProvider.getUserId(pair.getAccessToken()));
    }

    @Test
    @DisplayName("Bearer 토큰 추출")
    void testResolveToken() {
        // Given
        String token = tokenProvider.createAccessToken("user123", null);
        String bearerToken = "Bearer " + token;

        // When
        String resolved = tokenProvider.resolveToken(bearerToken);

        // Then
        assertEquals(token, resolved);
    }

    @Test
    @DisplayName("Bearer 접두사 없으면 null 반환")
    void testResolveTokenWithoutPrefix() {
        // When
        String resolved = tokenProvider.resolveToken("just-a-token");

        // Then
        assertNull(resolved);
    }

    @Test
    @DisplayName("토큰 만료 여부 확인")
    void testIsTokenExpired() {
        // Given - 유효한 토큰 (1시간 만료)
        String token = tokenProvider.createAccessToken("user123", null);

        // When
        boolean expired = tokenProvider.isTokenExpired(token);

        // Then
        assertFalse(expired);
    }
}
