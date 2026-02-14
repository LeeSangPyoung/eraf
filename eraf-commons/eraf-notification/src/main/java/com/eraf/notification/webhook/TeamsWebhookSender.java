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
 * Microsoft Teams Webhook 알림 전송
 */
public class TeamsWebhookSender implements WebhookSender {

    private static final Logger log = LoggerFactory.getLogger(TeamsWebhookSender.class);

    private final String webhookUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TeamsWebhookSender(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public TeamsWebhookSender(String webhookUrl, RestTemplate restTemplate) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendText(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("@type", "MessageCard");
        payload.put("@context", "https://schema.org/extensions");
        payload.put("text", message);

        sendPayload(payload);
    }

    @Override
    public void sendRich(String title, String message, String color, Map<String, String> fields) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("@type", "MessageCard");
        payload.put("@context", "https://schema.org/extensions");
        payload.put("title", title);
        payload.put("text", message);
        payload.put("themeColor", convertColor(color));

        if (fields != null && !fields.isEmpty()) {
            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();

            List<Map<String, String>> factsList = new ArrayList<>();
            fields.forEach((key, value) -> {
                Map<String, String> fact = new HashMap<>();
                fact.put("name", key);
                fact.put("value", value);
                factsList.add(fact);
            });

            section.put("facts", factsList);
            sections.add(section);
            payload.put("sections", sections);
        }

        sendPayload(payload);
    }

    @Override
    public void sendWithLevel(NotificationLevel level, String title, String message) {
        String emoji = switch (level) {
            case INFO -> "ℹ️";
            case WARNING -> "⚠️";
            case ERROR -> "❌";
        };

        sendRich(emoji + " " + title, message, level.getHexColor(), null);
    }

    /**
     * 액션 버튼이 있는 메시지 전송
     */
    public void sendWithActions(String title, String message, String color,
                                  Map<String, String> fields,
                                  List<Action> actions) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("@type", "MessageCard");
        payload.put("@context", "https://schema.org/extensions");
        payload.put("title", title);
        payload.put("text", message);
        payload.put("themeColor", convertColor(color));

        if (fields != null && !fields.isEmpty()) {
            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();

            List<Map<String, String>> factsList = new ArrayList<>();
            fields.forEach((key, value) -> {
                Map<String, String> fact = new HashMap<>();
                fact.put("name", key);
                fact.put("value", value);
                factsList.add(fact);
            });

            section.put("facts", factsList);
            sections.add(section);
            payload.put("sections", sections);
        }

        if (actions != null && !actions.isEmpty()) {
            List<Map<String, Object>> potentialActions = new ArrayList<>();
            for (Action action : actions) {
                Map<String, Object> actionMap = new HashMap<>();
                actionMap.put("@type", "OpenUri");
                actionMap.put("name", action.name);

                List<Map<String, String>> targets = List.of(
                        Map.of("os", "default", "uri", action.url)
                );
                actionMap.put("targets", targets);

                potentialActions.add(actionMap);
            }
            payload.put("potentialAction", potentialActions);
        }

        sendPayload(payload);
    }

    /**
     * 커스텀 페이로드 전송
     */
    public void sendCustom(Map<String, Object> payload) {
        sendPayload(payload);
    }

    private void sendPayload(Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.debug("Teams webhook sent successfully");

        } catch (Exception e) {
            log.error("Failed to send Teams webhook", e);
            throw new RuntimeException("Failed to send Teams notification", e);
        }
    }

    /**
     * 색상 변환 (Slack 스타일 → Hex)
     */
    private String convertColor(String color) {
        return switch (color) {
            case "good" -> "36a64f";
            case "warning" -> "ff9800";
            case "danger" -> "f44336";
            default -> color.replace("#", "");
        };
    }

    /**
     * Teams 에러 알림
     */
    public void sendError(String title, Throwable throwable) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Exception", throwable.getClass().getSimpleName());
        fields.put("Message", throwable.getMessage());

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace.length > 0) {
            fields.put("Location", stackTrace[0].toString());
        }

        sendRich("❌ " + title, throwable.getMessage(),
                NotificationLevel.ERROR.getHexColor(), fields);
    }

    /**
     * Action Button
     */
    public static class Action {
        public final String name;
        public final String url;

        public Action(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
