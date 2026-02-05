package com.eraf.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ErafHttpPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafHttpProperties properties = new ErafHttpProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("타임아웃 설정")
    void testTimeoutSettings() {
        // Given
        ErafHttpProperties properties = new ErafHttpProperties();

        // When
        properties.setConnectTimeout(Duration.ofSeconds(5));
        properties.setReadTimeout(Duration.ofSeconds(30));

        // Then
        assertEquals(Duration.ofSeconds(5), properties.getConnectTimeout());
        assertEquals(Duration.ofSeconds(30), properties.getReadTimeout());
    }

    @Test
    @DisplayName("재시도 설정")
    void testRetrySettings() {
        // Given
        ErafHttpProperties properties = new ErafHttpProperties();

        // When
        properties.setMaxRetries(3);

        // Then
        assertEquals(3, properties.getMaxRetries());
    }
}
