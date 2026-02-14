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
    @DisplayName("DLQ 설정")
    void testDlqSettings() {
        // Given
        ErafRabbitProperties properties = new ErafRabbitProperties();

        // Then - 기본값 확인
        assertTrue(properties.isDlqEnabled());
        assertEquals(".dlq", properties.getDlqSuffix());

        // When - 값 변경
        properties.setDlqEnabled(false);
        properties.setDlqSuffix(".dead");

        // Then
        assertFalse(properties.isDlqEnabled());
        assertEquals(".dead", properties.getDlqSuffix());
    }

    @Test
    @DisplayName("재시도 설정")
    void testRetrySettings() {
        // Given
        ErafRabbitProperties properties = new ErafRabbitProperties();

        // Then - 기본값 확인
        assertEquals(3, properties.getRetryCount());
        assertEquals(1000, properties.getRetryInitialInterval());
        assertEquals(10000, properties.getRetryMaxInterval());
        assertEquals(2.0, properties.getRetryMultiplier());

        // When
        properties.setRetryCount(5);
        properties.setRetryInitialInterval(2000);
        properties.setRetryMaxInterval(30000);
        properties.setRetryMultiplier(3.0);

        // Then
        assertEquals(5, properties.getRetryCount());
        assertEquals(2000, properties.getRetryInitialInterval());
        assertEquals(30000, properties.getRetryMaxInterval());
        assertEquals(3.0, properties.getRetryMultiplier());
    }

    @Test
    @DisplayName("컨텍스트 전파 설정")
    void testContextPropagation() {
        // Given
        ErafRabbitProperties properties = new ErafRabbitProperties();

        // Then - 기본값
        assertTrue(properties.isContextPropagationEnabled());

        // When
        properties.setContextPropagationEnabled(false);

        // Then
        assertFalse(properties.isContextPropagationEnabled());
    }
}
