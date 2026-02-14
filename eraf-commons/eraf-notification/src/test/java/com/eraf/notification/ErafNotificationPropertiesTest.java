package com.eraf.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafNotificationPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafNotificationProperties properties = new ErafNotificationProperties();

        // Then
        assertNotNull(properties);
    }

    @Test
    @DisplayName("이메일 설정")
    void testEmailSettings() {
        // Given
        ErafNotificationProperties properties = new ErafNotificationProperties();
        ErafNotificationProperties.Email email = properties.getEmail();

        // When
        email.setEnabled(true);
        email.setFrom("noreply@example.com");

        // Then
        assertTrue(email.isEnabled());
        assertEquals("noreply@example.com", email.getFrom());
    }

    @Test
    @DisplayName("SMS 설정")
    void testSmsSettings() {
        // Given
        ErafNotificationProperties properties = new ErafNotificationProperties();
        ErafNotificationProperties.Sms sms = properties.getSms();

        // When
        sms.setEnabled(true);
        sms.setProvider(ErafNotificationProperties.Sms.SmsProvider.TWILIO);

        // Then
        assertTrue(sms.isEnabled());
        assertEquals(ErafNotificationProperties.Sms.SmsProvider.TWILIO, sms.getProvider());
    }
}
