package com.eraf.tcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafTcpPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafTcpProperties properties = new ErafTcpProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("서버 설정")
    void testServerSettings() {
        // Given
        ErafTcpProperties properties = new ErafTcpProperties();

        // When
        properties.setHost("127.0.0.1");
        properties.setPort(9999);

        // Then
        assertEquals("127.0.0.1", properties.getHost());
        assertEquals(9999, properties.getPort());
    }

    @Test
    @DisplayName("연결 설정")
    void testConnectionSettings() {
        // Given
        ErafTcpProperties properties = new ErafTcpProperties();

        // Then - 기본값 확인
        assertEquals(10000, properties.getConnectionTimeout());
        assertEquals(30000, properties.getReadTimeout());
        assertTrue(properties.isKeepAlive());
        assertTrue(properties.isTcpNoDelay());

        // When
        properties.setConnectionTimeout(5000);
        properties.setReadTimeout(15000);
        properties.setKeepAlive(false);
        properties.setTcpNoDelay(false);

        // Then
        assertEquals(5000, properties.getConnectionTimeout());
        assertEquals(15000, properties.getReadTimeout());
        assertFalse(properties.isKeepAlive());
        assertFalse(properties.isTcpNoDelay());
    }

    @Test
    @DisplayName("재연결 설정")
    void testReconnectSettings() {
        // Given
        ErafTcpProperties properties = new ErafTcpProperties();

        // Then - 기본값
        assertTrue(properties.isAutoReconnect());
        assertEquals(5000, properties.getReconnectInterval());
        assertEquals(10, properties.getMaxReconnectAttempts());

        // When
        properties.setAutoReconnect(false);
        properties.setReconnectInterval(10000);
        properties.setMaxReconnectAttempts(5);

        // Then
        assertFalse(properties.isAutoReconnect());
        assertEquals(10000, properties.getReconnectInterval());
        assertEquals(5, properties.getMaxReconnectAttempts());
    }
}
