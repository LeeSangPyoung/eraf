package com.eraf.notification.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    private SmtpEmailSender emailSender;

    @BeforeEach
    void setUp() {
        emailSender = new SmtpEmailSender(javaMailSender, "noreply@example.com");
    }

    @Test
    @DisplayName("단순 이메일 발송")
    void testSendSimpleEmail() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        emailSender.send(to, subject, body);

        // Then
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("빈 수신자 시 발송하지 않음")
    void testDoNotSendToEmptyRecipient() {
        // Given
        String to = "";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        emailSender.send(to, subject, body);

        // Then
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
