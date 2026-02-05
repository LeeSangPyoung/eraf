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
    @DisplayName("연결 풀 설정")
    void testConnectionPoolSettings() {
        // Given
        ErafTcpProperties properties = new ErafTcpProperties();

        // When
        properties.setMaxConnections(10);
        properties.setConnectionTimeout(5000);

        // Then
        assertEquals(10, properties.getMaxConnections());
        assertEquals(5000, properties.getConnectionTimeout());
    }
}
