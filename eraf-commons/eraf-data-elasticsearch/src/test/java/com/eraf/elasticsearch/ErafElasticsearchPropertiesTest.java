package com.eraf.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafElasticsearchPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafElasticsearchProperties properties = new ErafElasticsearchProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("호스트 설정")
    void testHosts() {
        // Given
        ErafElasticsearchProperties properties = new ErafElasticsearchProperties();

        // When
        properties.setHosts(new String[]{"localhost:9200", "localhost:9201"});

        // Then
        assertEquals(2, properties.getHosts().length);
        assertEquals("localhost:9200", properties.getHosts()[0]);
    }
}
