package com.eraf.notification.email;

import com.eraf.notification.ErafNotificationProperties;
import com.eraf.notification.NotificationService.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private Executor executor;

    private SmtpEmailSender emailSender;

    @BeforeEach
    void setUp() {
        ErafNotificationProperties properties = new ErafNotificationProperties();
        properties.getEmail().setFrom("noreply@example.com");
        emailSender = new SmtpEmailSender(javaMailSender, properties, executor);
    }

    @Test
    @DisplayName("단순 이메일 발송")
    void testSendSimpleEmail() {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailRequest request = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .html(false);

        // When
        emailSender.send(request);

        // Then
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
