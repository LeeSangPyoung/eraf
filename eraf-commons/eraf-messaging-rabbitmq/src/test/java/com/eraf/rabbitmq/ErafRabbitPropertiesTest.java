package com.eraf.rabbitmq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafRabbitPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafRabbitProperties properties = new ErafRabbitProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("Exchange 설정")
    void testExchangeSettings() {
        // Given
        ErafRabbitProperties properties = new ErafRabbitProperties();

        // When
        properties.setDefaultExchange("eraf.exchange");

        // Then
        assertEquals("eraf.exchange", properties.getDefaultExchange());
    }
}
