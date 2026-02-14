package com.eraf.notification.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SlackWebhookSender 테스트
 */
@ExtendWith(MockitoExtension.class)
class SlackWebhookSenderTest {

    @Mock
    private RestTemplate restTemplate;

    private SlackWebhookSender slackWebhookSender;
    private static final String WEBHOOK_URL = "https://test-webhook.example.com/slack/test";

    @BeforeEach
    void setUp() {
        slackWebhookSender = new SlackWebhookSender(WEBHOOK_URL, restTemplate);
    }

    @Test
    void sendText_ShouldSendTextMessage() {
        // given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(null);

        // when
        slackWebhookSender.sendText("Test message");

        // then
        verify(restTemplate).postForEntity(
                eq(WEBHOOK_URL),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void sendRich_ShouldSendAttachment() {
        // given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(null);

        // when
        slackWebhookSender.sendRich(
                "Alert",
                "System error occurred",
                "danger",
                Map.of("server", "prod-1", "status", "error")
        );

        // then
        verify(restTemplate).postForEntity(
                eq(WEBHOOK_URL),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}
