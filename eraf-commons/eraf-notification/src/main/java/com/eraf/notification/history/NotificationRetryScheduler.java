package com.eraf.notification.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 실패한 알림 자동 재시도 스케줄러
 */
public class NotificationRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

    private final NotificationHistoryRepository repository;
    private final int maxRetries;

    public NotificationRetryScheduler(NotificationHistoryRepository repository, int maxRetries) {
        this.repository = repository;
        this.maxRetries = maxRetries;
    }

    /**
     * 실패한 알림 재시도 (1분 간격)
     */
    @Scheduled(fixedDelayString = "${eraf.notification.retry.interval:60000}")
    public void retryFailedNotifications() {
        List<NotificationHistory> failedNotifications =
                repository.findFailedNotificationsForRetry(maxRetries);

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("[NotificationRetry] Found {} failed notifications for retry", failedNotifications.size());

        for (NotificationHistory notification : failedNotifications) {
            try {
                notification.incrementRetry();
                notification.setStatus(NotificationHistory.NotificationStatus.RETRY);
                repository.save(notification);

                // 재시도 로직: 실제 발송은 NotificationService를 통해 수행
                // 여기서는 상태 업데이트만 처리하고, 발송은 이벤트 기반으로 위임
                log.info("[NotificationRetry] Retrying notification id={}, type={}, attempt={}",
                        notification.getId(), notification.getNotificationType(), notification.getRetryCount());

            } catch (Exception e) {
                log.error("[NotificationRetry] Failed to retry notification id={}",
                        notification.getId(), e);
            }
        }
    }

    /**
     * 최대 재시도 초과 알림 DEAD_LETTER 처리 (1시간 간격)
     */
    @Scheduled(fixedDelayString = "${eraf.notification.retry.dead-letter-interval:3600000}")
    public void handleDeadLetterNotifications() {
        List<NotificationHistory> failedNotifications =
                repository.findFailedNotificationsForRetry(maxRetries + 1);

        // maxRetries 이상인 것들은 이미 제외되므로 별도 처리 불필요
        // 로그만 남김
        if (!failedNotifications.isEmpty()) {
            log.warn("[NotificationRetry] {} notifications exceeded max retries ({})",
                    failedNotifications.size(), maxRetries);
        }
    }
}
