package com.eraf.notification;

import com.eraf.notification.email.EmailSender;
import com.eraf.notification.impl.DefaultNotificationService;
import com.eraf.notification.push.PushSender;
import com.eraf.notification.sms.SmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private SmsSender smsSender;

    @Mock
    private PushSender pushSender;

    private DefaultNotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new DefaultNotificationService(emailSender, smsSender, pushSender);
    }

    @Test
    @DisplayName("이메일 발송")
    void testSendEmail() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        notificationService.sendEmail(to, subject, body);

        // Then
        verify(emailSender, times(1)).send(to, subject, body);
    }

    @Test
    @DisplayName("SMS 발송")
    void testSendSms() {
        // Given
        String phoneNumber = "010-1234-5678";
        String message = "Test Message";

        // When
        notificationService.sendSms(phoneNumber, message);

        // Then
        verify(smsSender, times(1)).send(phoneNumber, message);
    }

    @Test
    @DisplayName("푸시 알림 발송")
    void testSendPush() {
        // Given
        String token = "device-token-123";
        String title = "Test Title";
        String body = "Test Body";

        // When
        notificationService.sendPush(token, title, body);

        // Then
        verify(pushSender, times(1)).send(token, title, body);
    }

    @Test
    @DisplayName("다중 수신자 이메일 발송")
    void testSendEmailToMultiple() {
        // Given
        String[] recipients = {"user1@example.com", "user2@example.com"};
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        notificationService.sendEmail(recipients, subject, body);

        // Then
        verify(emailSender, times(2)).send(anyString(), eq(subject), eq(body));
    }
}
