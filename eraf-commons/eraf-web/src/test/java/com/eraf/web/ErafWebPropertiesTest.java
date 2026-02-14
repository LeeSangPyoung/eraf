package com.eraf.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafWebPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafWebProperties properties = new ErafWebProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("CORS 설정")
    void testCorsSettings() {
        // Given
        ErafWebProperties properties = new ErafWebProperties();

        // When
        properties.setCorsAllowedOrigins(new String[]{"http://localhost:3000"});
        properties.setCorsAllowedMethods(new String[]{"GET", "POST"});
        properties.setCorsEnabled(true);

        // Then
        assertEquals(1, properties.getCorsAllowedOrigins().length);
        assertEquals("http://localhost:3000", properties.getCorsAllowedOrigins()[0]);
        assertTrue(properties.isCorsEnabled());
    }
}
