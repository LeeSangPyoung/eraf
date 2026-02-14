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

        // Then - 기본값
        assertEquals(Duration.ofSeconds(30), properties.getTimeout());

        // When
        properties.setTimeout(Duration.ofSeconds(10));

        // Then
        assertEquals(Duration.ofSeconds(10), properties.getTimeout());
    }

    @Test
    @DisplayName("재시도 설정")
    void testRetrySettings() {
        // Given
        ErafHttpProperties properties = new ErafHttpProperties();

        // Then - 기본값
        assertEquals(3, properties.getRetryCount());

        // When
        properties.setRetryCount(5);

        // Then
        assertEquals(5, properties.getRetryCount());
    }

    @Test
    @DisplayName("Circuit Breaker 및 컨텍스트 전파 설정")
    void testCircuitBreakerAndContextPropagation() {
        // Given
        ErafHttpProperties properties = new ErafHttpProperties();

        // Then - 기본값
        assertTrue(properties.isCircuitBreakerEnabled());
        assertTrue(properties.isContextPropagationEnabled());

        // When
        properties.setCircuitBreakerEnabled(false);
        properties.setContextPropagationEnabled(false);

        // Then
        assertFalse(properties.isCircuitBreakerEnabled());
        assertFalse(properties.isContextPropagationEnabled());
    }
}
