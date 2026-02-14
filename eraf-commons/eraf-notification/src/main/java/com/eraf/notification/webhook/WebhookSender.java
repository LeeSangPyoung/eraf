package com.eraf.notification.webhook;

import java.util.Map;

/**
 * Webhook 알림 전송 인터페이스
 */
public interface WebhookSender {

    /**
     * 텍스트 메시지 전송
     *
     * @param message 메시지 내용
     */
    void sendText(String message);

    /**
     * 리치 메시지 전송 (포맷팅, 색상, 버튼 등)
     *
     * @param title 제목
     * @param message 메시지 내용
     * @param color 색상 (예: "good", "warning", "danger" 또는 hex)
     * @param fields 추가 필드 (key-value)
     */
    void sendRich(String title, String message, String color, Map<String, String> fields);

    /**
     * 알림 레벨에 따른 메시지 전송
     *
     * @param level 알림 레벨 (INFO, WARNING, ERROR)
     * @param title 제목
     * @param message 메시지
     */
    void sendWithLevel(NotificationLevel level, String title, String message);

    /**
     * 알림 레벨
     */
    enum NotificationLevel {
        INFO("good", "#36a64f"),      // Green
        WARNING("warning", "#ff9800"), // Orange
        ERROR("danger", "#f44336");    // Red

        private final String slackColor;
        private final String hexColor;

        NotificationLevel(String slackColor, String hexColor) {
            this.slackColor = slackColor;
            this.hexColor = hexColor;
        }

        public String getSlackColor() {
            return slackColor;
        }

        public String getHexColor() {
            return hexColor;
        }
    }
}
