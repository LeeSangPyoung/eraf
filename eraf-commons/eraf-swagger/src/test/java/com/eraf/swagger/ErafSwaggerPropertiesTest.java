package com.eraf.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafSwaggerPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafSwaggerProperties properties = new ErafSwaggerProperties();

        // Then
        assertNotNull(properties);
        assertTrue(properties.isEnabled());
    }

    @Test
    @DisplayName("API 정보 설정")
    void testApiInfo() {
        // Given
        ErafSwaggerProperties properties = new ErafSwaggerProperties();
        ErafSwaggerProperties.ApiInfo apiInfo = properties.getApiInfo();

        // When
        apiInfo.setTitle("My API");
        apiInfo.setDescription("API Description");
        apiInfo.setVersion("1.0.0");

        // Then
        assertEquals("My API", apiInfo.getTitle());
        assertEquals("API Description", apiInfo.getDescription());
        assertEquals("1.0.0", apiInfo.getVersion());
    }

    @Test
    @DisplayName("Swagger 비활성화")
    void testDisable() {
        // Given
        ErafSwaggerProperties properties = new ErafSwaggerProperties();

        // When
        properties.setEnabled(false);

        // Then
        assertFalse(properties.isEnabled());
    }
}
