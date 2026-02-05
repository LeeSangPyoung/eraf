package com.eraf.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ErafCachePropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // Then
        assertEquals("simple", properties.getType());
        assertEquals(Duration.ofMinutes(30), properties.getDefaultTtl());
        assertEquals(1000, properties.getMaxSize());
        assertEquals("eraf:cache:", properties.getKeyPrefix());
    }

    @Test
    @DisplayName("값 설정")
    void testSetValues() {
        // Given
        ErafCacheProperties properties = new ErafCacheProperties();

        // When
        properties.setType("caffeine");
        properties.setDefaultTtl(Duration.ofHours(1));
        properties.setMaxSize(5000);
        properties.setKeyPrefix("myapp:cache:");

        // Then
        assertEquals("caffeine", properties.getType());
        assertEquals(Duration.ofHours(1), properties.getDefaultTtl());
        assertEquals(5000, properties.getMaxSize());
        assertEquals("myapp:cache:", properties.getKeyPrefix());
    }
}
