package com.eraf.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MessagingProperties - 메시징 설정")
class MessagingPropertiesTest {

    @Test
    @DisplayName("기본값")
    void defaults() {
        MessagingProperties props = new MessagingProperties();
        assertEquals(MessagingProperties.MessagingType.KAFKA, props.getType());
        assertNull(props.getDefaultDestination());
        assertTrue(props.isContextPropagationEnabled());
        assertEquals(3, props.getRetryCount());
        assertTrue(props.isDlqEnabled());
    }

    @Test
    @DisplayName("setter/getter")
    void setterGetter() {
        MessagingProperties props = new MessagingProperties();
        props.setType(MessagingProperties.MessagingType.RABBITMQ);
        props.setDefaultDestination("my-topic");
        props.setContextPropagationEnabled(false);
        props.setRetryCount(5);
        props.setDlqEnabled(false);

        assertEquals(MessagingProperties.MessagingType.RABBITMQ, props.getType());
        assertEquals("my-topic", props.getDefaultDestination());
        assertFalse(props.isContextPropagationEnabled());
        assertEquals(5, props.getRetryCount());
        assertFalse(props.isDlqEnabled());
    }

    @Test
    @DisplayName("MessagingType 열거형")
    void messagingType() {
        assertEquals(3, MessagingProperties.MessagingType.values().length);
        assertNotNull(MessagingProperties.MessagingType.valueOf("KAFKA"));
        assertNotNull(MessagingProperties.MessagingType.valueOf("RABBITMQ"));
        assertNotNull(MessagingProperties.MessagingType.valueOf("SQS"));
    }
}
