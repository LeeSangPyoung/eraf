package com.eraf.notification.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slack Webhook 알림 전송
 */
public class SlackWebhookSender implements WebhookSender {

    private static final Logger log = LoggerFactory.getLogger(SlackWebhookSender.class);

    private final String webhookUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SlackWebhookSender(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public SlackWebhookSender(String webhookUrl, RestTemplate restTemplate) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendText(String message) {
        Map<String, Object> payload = Map.of("text", message);
        sendPayload(payload);
    }

    @Override
    public void sendRich(String title, String message, String color, Map<String, String> fields) {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("title", title);
        attachment.put("text", message);
        attachment.put("color", color);

        if (fields != null && !fields.isEmpty()) {
            List<Map<String, String>> fieldsList = new ArrayList<>();
            fields.forEach((key, value) -> {
                Map<String, String> field = new HashMap<>();
                field.put("title", key);
                field.put("value", value);
                field.put("short", "true");
                fieldsList.add(field);
            });
            attachment.put("fields", fieldsList);
        }

        Map<String, Object> payload = Map.of("attachments", List.of(attachment));
        sendPayload(payload);
    }

    @Override
    public void sendWithLevel(NotificationLevel level, String title, String message) {
        sendRich(title, message, level.getSlackColor(), null);
    }

    /**
     * 커스텀 페이로드 전송
     */
    public void sendCustom(Map<String, Object> payload) {
        sendPayload(payload);
    }

    /**
     * Blocks API를 사용한 메시지 전송 (더 다양한 레이아웃)
     */
    public void sendBlocks(String text, List<Map<String, Object>> blocks) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);  // Fallback text
        payload.put("blocks", blocks);
        sendPayload(payload);
    }

    private void sendPayload(Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.debug("Slack webhook sent successfully");

        } catch (Exception e) {
            log.error("Failed to send Slack webhook", e);
            throw new RuntimeException("Failed to send Slack notification", e);
        }
    }

    /**
     * Slack 에러 알림 (스택 트레이스 포함)
     */
    public void sendError(String title, Throwable throwable) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Exception", throwable.getClass().getSimpleName());
        fields.put("Message", throwable.getMessage());

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 0) {
            fields.put("Location", stackTrace[0].toString());
        }

        sendRich(":x: " + title, throwable.getMessage(),
                NotificationLevel.ERROR.getSlackColor(), fields);
    }
}
