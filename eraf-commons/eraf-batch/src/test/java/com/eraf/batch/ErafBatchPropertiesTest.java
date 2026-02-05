package com.eraf.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafBatchPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafBatchProperties properties = new ErafBatchProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("청크 크기 설정")
    void testChunkSize() {
        // Given
        ErafBatchProperties properties = new ErafBatchProperties();

        // When
        properties.setChunkSize(500);

        // Then
        assertEquals(500, properties.getChunkSize());
    }
}
