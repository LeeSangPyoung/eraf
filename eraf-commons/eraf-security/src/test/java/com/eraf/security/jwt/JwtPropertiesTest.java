package com.eraf.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtProperties 테스트
 */
class JwtPropertiesTest {

    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
    }

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() {
        assertEquals(Duration.ofHours(1), properties.getAccessTokenExpiration());
        assertEquals(Duration.ofDays(7), properties.getRefreshTokenExpiration());
        assertEquals("Authorization", properties.getHeaderName());
        assertEquals("Bearer ", properties.getTokenPrefix());
        assertEquals("eraf", properties.getIssuer());
        assertFalse(properties.isEnabled());
    }

    @Test
    @DisplayName("시크릿 키 설정")
    void shouldSetSecret() {
        properties.setSecret("mySecretKey123456789012345678901234567890");

        assertEquals("mySecretKey123456789012345678901234567890", properties.getSecret());
    }

    @Test
    @DisplayName("발급자 설정")
    void shouldSetIssuer() {
        properties.setIssuer("my-application");

        assertEquals("my-application", properties.getIssuer());
    }

    @Test
    @DisplayName("액세스 토큰 만료시간 설정")
    void shouldSetAccessTokenExpiration() {
        properties.setAccessTokenExpiration(Duration.ofMinutes(30));

        assertEquals(Duration.ofMinutes(30), properties.getAccessTokenExpiration());
    }

    @Test
    @DisplayName("리프레시 토큰 만료시간 설정")
    void shouldSetRefreshTokenExpiration() {
        properties.setRefreshTokenExpiration(Duration.ofDays(30));

        assertEquals(Duration.ofDays(30), properties.getRefreshTokenExpiration());
    }

    @Test
    @DisplayName("헤더명 변경")
    void shouldSetHeaderName() {
        properties.setHeaderName("X-Auth-Token");

        assertEquals("X-Auth-Token", properties.getHeaderName());
    }

    @Test
    @DisplayName("토큰 접두어 변경")
    void shouldSetTokenPrefix() {
        properties.setTokenPrefix("Token ");

        assertEquals("Token ", properties.getTokenPrefix());
    }

    @Test
    @DisplayName("모든 설정 한번에 변경")
    void shouldAllowAllConfigurationsToBeChanged() {
        properties.setSecret("newSecret123456789012345678901234567890");
        properties.setIssuer("new-issuer");
        properties.setAccessTokenExpiration(Duration.ofMinutes(15));
        properties.setRefreshTokenExpiration(Duration.ofDays(1));
        properties.setHeaderName("X-JWT");
        properties.setTokenPrefix("JWT ");

        assertEquals("newSecret123456789012345678901234567890", properties.getSecret());
        assertEquals("new-issuer", properties.getIssuer());
        assertEquals(Duration.ofMinutes(15), properties.getAccessTokenExpiration());
        assertEquals(Duration.ofDays(1), properties.getRefreshTokenExpiration());
        assertEquals("X-JWT", properties.getHeaderName());
        assertEquals("JWT ", properties.getTokenPrefix());
    }

    @Test
    @DisplayName("빈 토큰 접두어 허용")
    void shouldAllowEmptyTokenPrefix() {
        properties.setTokenPrefix("");

        assertEquals("", properties.getTokenPrefix());
    }

    @Test
    @DisplayName("스킵 패턴 설정")
    void shouldSetSkipPatterns() {
        String[] patterns = {"/public/**", "/health/**"};
        properties.setSkipPatterns(patterns);

        assertArrayEquals(patterns, properties.getSkipPatterns());
    }
}
