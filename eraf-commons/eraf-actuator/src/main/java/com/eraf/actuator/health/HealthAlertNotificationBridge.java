package com.eraf.actuator.health;

import com.eraf.notification.webhook.SlackWebhookSender;
import com.eraf.notification.webhook.TeamsWebhookSender;
import com.eraf.notification.webhook.WebhookSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health Alert → Notification 브릿지
 * <p>
 * Health Indicator 상태 변화를 감지하여 Slack/Teams Webhook으로 알림을 전송합니다.
 * {@code checkAndNotify()} 메서드를 주기적으로 호출하여 상태 변화를 모니터링합니다.
 *
 * <pre>{@code
 * // 스케줄러에서 호출
 * @Scheduled(fixedRate = 30000)
 * public void healthCheck() {
 *     healthAlertBridge.checkAndNotify();
 * }
 * }</pre>
 */
public class HealthAlertNotificationBridge {

    private static final Logger log = LoggerFactory.getLogger(HealthAlertNotificationBridge.class);

    private final List<HealthIndicator> healthIndicators;
    private final List<WebhookSender> webhookSenders;
    private final Map<String, Status> lastKnownStatus = new ConcurrentHashMap<>();
    private final String applicationName;

    public HealthAlertNotificationBridge(List<HealthIndicator> healthIndicators,
                                          List<WebhookSender> webhookSenders,
                                          String applicationName) {
        this.healthIndicators = healthIndicators != null ? healthIndicators : List.of();
        this.webhookSenders = webhookSenders != null ? webhookSenders : List.of();
        this.applicationName = applicationName != null ? applicationName : "eraf-app";
    }

    /**
     * 모든 Health Indicator를 확인하고, 상태가 변경된 경우 알림을 전송합니다.
     */
    public void checkAndNotify() {
        for (HealthIndicator indicator : healthIndicators) {
            String name = indicator.getClass().getSimpleName();
            try {
                Health health = indicator.health();
                Status currentStatus = health.getStatus();
                Status previousStatus = lastKnownStatus.put(name, currentStatus);

                if (previousStatus != null && !previousStatus.equals(currentStatus)) {
                    sendStatusChangeNotification(name, previousStatus, currentStatus, health);
                }
            } catch (Exception e) {
                log.warn("Failed to check health for {}: {}", name, e.getMessage());
            }
        }
    }

    private void sendStatusChangeNotification(String indicatorName, Status from, Status to, Health health) {
        WebhookSender.NotificationLevel level = to.equals(Status.UP)
                ? WebhookSender.NotificationLevel.INFO
                : WebhookSender.NotificationLevel.ERROR;

        String title = String.format("[%s] Health Status Changed: %s", applicationName, indicatorName);

        Map<String, String> fields = new HashMap<>();
        fields.put("Indicator", indicatorName);
        fields.put("Previous", from.getCode());
        fields.put("Current", to.getCode());
        fields.put("Time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        health.getDetails().forEach((key, value) ->
                fields.put(key, String.valueOf(value)));

        for (WebhookSender sender : webhookSenders) {
            try {
                sender.sendRich(title,
                        String.format("Health status changed from %s to %s", from.getCode(), to.getCode()),
                        level.getSlackColor(),
                        fields);
            } catch (Exception e) {
                log.error("Failed to send health alert via {}: {}", sender.getClass().getSimpleName(), e.getMessage());
            }
        }

        log.info("Health alert sent: {} changed {} → {}", indicatorName, from.getCode(), to.getCode());
    }

    /**
     * 현재 알려진 상태 맵 조회 (모니터링용)
     */
    public Map<String, Status> getLastKnownStatus() {
        return Map.copyOf(lastKnownStatus);
    }
}
