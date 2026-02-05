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
        tokenProvider = new ErafJwtTokenProvider(properties);
    }

    @Test
    @DisplayName("토큰 생성")
    void testGenerateToken() {
        // Given
        String subject = "user123";

        // When
        String token = tokenProvider.generateToken(subject);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("토큰에서 subject 추출")
    void testGetSubject() {
        // Given
        String subject = "user123";
        String token = tokenProvider.generateToken(subject);

        // When
        String extractedSubject = tokenProvider.getSubject(token);

        // Then
        assertEquals(subject, extractedSubject);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void testValidateTokenValid() {
        // Given
        String token = tokenProvider.generateToken("user123");

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
    void testGenerateTokenWithClaims() {
        // Given
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("userId", 123L);

        // When
        String token = tokenProvider.generateToken(subject, claims);

        // Then
        assertNotNull(token);
        assertEquals(subject, tokenProvider.getSubject(token));
    }
}
