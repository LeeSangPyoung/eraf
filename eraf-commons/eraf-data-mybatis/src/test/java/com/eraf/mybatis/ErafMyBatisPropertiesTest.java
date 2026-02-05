package com.eraf.mybatis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafMyBatisPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafMyBatisProperties properties = new ErafMyBatisProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("Mapper 위치 설정")
    void testMapperLocations() {
        // Given
        ErafMyBatisProperties properties = new ErafMyBatisProperties();

        // When
        properties.setMapperLocations("classpath:mappers/**/*.xml");

        // Then
        assertEquals("classpath:mappers/**/*.xml", properties.getMapperLocations());
    }
}
