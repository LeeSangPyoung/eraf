package com.eraf.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafSchedulerPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafSchedulerProperties properties = new ErafSchedulerProperties();

        // Then
        assertNotNull(properties);
        assertTrue(properties.isEnabled());
    }

    @Test
    @DisplayName("스케줄러 비활성화")
    void testDisable() {
        // Given
        ErafSchedulerProperties properties = new ErafSchedulerProperties();

        // When
        properties.setEnabled(false);

        // Then
        assertFalse(properties.isEnabled());
    }
}
