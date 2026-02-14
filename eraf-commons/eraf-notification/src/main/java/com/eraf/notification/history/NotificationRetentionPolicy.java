package com.eraf.notification.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 알림 이력 보존 정책
 *
 * 오래된 알림 이력을 자동으로 정리합니다.
 */
public class NotificationRetentionPolicy {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetentionPolicy.class);

    private final NotificationHistoryRepository repository;
    private final int retentionDays;

    public NotificationRetentionPolicy(NotificationHistoryRepository repository, int retentionDays) {
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    /**
     * 보존 기간이 지난 알림 이력 삭제
     * 매일 새벽 4시 실행
     */
    @Scheduled(cron = "${eraf.notification.retention.cron:0 0 4 * * ?}")
    @Transactional
    public void cleanupExpiredNotifications() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("Starting notification history cleanup (retention: {} days, cutoff: {})",
                retentionDays, cutoff);

        List<NotificationHistory> expired = repository.findBySentAtBefore(cutoff);

        if (!expired.isEmpty()) {
            repository.deleteAll(expired);
            log.info("Deleted {} expired notification history records", expired.size());
        } else {
            log.debug("No expired notification records found");
        }
    }

    /**
     * 수동 정리 (지정 일수 이전)
     */
    @Transactional
    public int cleanupBefore(int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        List<NotificationHistory> expired = repository.findBySentAtBefore(cutoff);
        if (!expired.isEmpty()) {
            repository.deleteAll(expired);
            log.info("Manually deleted {} notification history records older than {} days",
                    expired.size(), days);
        }
        return expired.size();
    }
}
