package com.eraf.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafRedisPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafRedisProperties properties = new ErafRedisProperties();

        // Then
        assertNotNull(properties);
        assertNotNull(properties.getKeyPrefix());
    }

    @Test
    @DisplayName("키 프리픽스 설정")
    void testKeyPrefix() {
        // Given
        ErafRedisProperties properties = new ErafRedisProperties();

        // When
        properties.setKeyPrefix("myapp:");

        // Then
        assertEquals("myapp:", properties.getKeyPrefix());
    }
}
