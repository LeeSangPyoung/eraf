package com.eraf.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ErafSessionPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafSessionProperties properties = new ErafSessionProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("세션 타임아웃 설정")
    void testSessionTimeout() {
        // Given
        ErafSessionProperties properties = new ErafSessionProperties();

        // When
        properties.setTimeout(Duration.ofHours(2));

        // Then
        assertEquals(Duration.ofHours(2), properties.getTimeout());
    }

    @Test
    @DisplayName("JWT 설정")
    void testJwtSettings() {
        // Given
        ErafSessionProperties properties = new ErafSessionProperties();
        ErafSessionProperties.Jwt jwt = properties.getJwt();

        // When
        jwt.setSecret("my-secret-key");
        jwt.setExpiration(Duration.ofHours(24));

        // Then
        assertEquals("my-secret-key", jwt.getSecret());
        assertEquals(Duration.ofHours(24), jwt.getExpiration());
    }
}
