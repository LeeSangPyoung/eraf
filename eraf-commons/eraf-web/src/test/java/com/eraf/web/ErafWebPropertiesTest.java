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
        ErafWebProperties.Cors cors = new ErafWebProperties.Cors();

        // When
        cors.setAllowedOrigins(new String[]{"http://localhost:3000"});
        cors.setAllowedMethods(new String[]{"GET", "POST"});
        cors.setAllowCredentials(true);

        // Then
        assertEquals(1, cors.getAllowedOrigins().length);
        assertEquals("http://localhost:3000", cors.getAllowedOrigins()[0]);
        assertTrue(cors.isAllowCredentials());
    }
}
