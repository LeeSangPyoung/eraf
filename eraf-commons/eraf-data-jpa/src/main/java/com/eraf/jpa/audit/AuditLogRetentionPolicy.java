package com.eraf.jpa.audit;

import com.eraf.jpa.ErafJpaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 감사 로그 보관 정책
 * 오래된 감사 로그를 자동으로 삭제하여 데이터베이스 용량 관리
 */
public class AuditLogRetentionPolicy {

    private static final Logger log = LoggerFactory.getLogger(AuditLogRetentionPolicy.class);

    private final AuditLogJpaRepository auditLogRepository;
    private final ErafJpaProperties.AuditRetention config;

    public AuditLogRetentionPolicy(AuditLogJpaRepository auditLogRepository,
                                   ErafJpaProperties.AuditRetention config) {
        this.auditLogRepository = auditLogRepository;
        this.config = config;
    }

    /**
     * 매일 자정에 실행 (Soft Delete)
     * Cron: Properties에서 설정 가능 (기본: 매일 02:00 AM)
     */
    @Scheduled(cron = "${eraf.jpa.audit-retention.cron:0 0 2 * * ?}")
    @Transactional
    public void softDeleteOldLogs() {
        if (!config.isEnabled()) {
            log.debug("Audit log retention policy is disabled");
            return;
        }

        try {
            Instant cutoffDate = Instant.now().minus(config.getRetentionDays(), ChronoUnit.DAYS);
            log.info("Starting soft delete of audit logs older than {} (retention: {} days)",
                    cutoffDate, config.getRetentionDays());

            List<AuditLogEntity> oldLogs = auditLogRepository.findByTimestampBeforeAndDeletedFalse(cutoffDate);
            int count = oldLogs.size();

            if (count == 0) {
                log.info("No audit logs to soft delete");
                return;
            }

            // Soft delete 처리
            Instant now = Instant.now();
            oldLogs.forEach(log -> {
                log.setDeleted(true);
                log.setDeletedAt(now);
                log.setDeletedBy("system:retention-policy");
            });

            auditLogRepository.saveAll(oldLogs);
            log.info("Soft deleted {} audit logs older than {} days", count, config.getRetentionDays());

        } catch (Exception e) {
            log.error("Failed to soft delete old audit logs: {}", e.getMessage(), e);
        }
    }

    /**
     * 주 1회 실행 (Hard Delete)
     * Cron: Properties에서 설정 가능 (기본: 매주 일요일 03:00 AM)
     */
    @Scheduled(cron = "${eraf.jpa.audit-retention.hard-delete-cron:0 0 3 * * SUN}")
    @Transactional
    public void hardDeleteOldLogs() {
        if (!config.isEnabled() || !config.isHardDeleteEnabled()) {
            log.debug("Audit log hard delete is disabled");
            return;
        }

        try {
            Instant cutoffDate = Instant.now().minus(config.getHardDeleteAfterDays(), ChronoUnit.DAYS);
            log.info("Starting hard delete of audit logs deleted before {} (hard delete after: {} days)",
                    cutoffDate, config.getHardDeleteAfterDays());

            int count = auditLogRepository.deleteByDeletedTrueAndDeletedAtBefore(cutoffDate);
            log.info("Hard deleted {} audit logs", count);

        } catch (Exception e) {
            log.error("Failed to hard delete old audit logs: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동으로 보관 정책 실행
     */
    @Transactional
    public RetentionResult executeRetentionPolicy() {
        Instant cutoffDate = Instant.now().minus(config.getRetentionDays(), ChronoUnit.DAYS);

        List<AuditLogEntity> oldLogs = auditLogRepository.findByTimestampBeforeAndDeletedFalse(cutoffDate);
        int softDeleteCount = oldLogs.size();

        Instant now = Instant.now();
        oldLogs.forEach(log -> {
            log.setDeleted(true);
            log.setDeletedAt(now);
            log.setDeletedBy("system:manual-retention");
        });

        auditLogRepository.saveAll(oldLogs);

        // Hard delete
        int hardDeleteCount = 0;
        if (config.isHardDeleteEnabled()) {
            Instant hardDeleteCutoff = Instant.now().minus(config.getHardDeleteAfterDays(), ChronoUnit.DAYS);
            hardDeleteCount = auditLogRepository.deleteByDeletedTrueAndDeletedAtBefore(hardDeleteCutoff);
        }

        return new RetentionResult(softDeleteCount, hardDeleteCount);
    }

    /**
     * 특정 기간의 로그 삭제 (Soft Delete)
     */
    @Transactional
    public int softDeleteByPeriod(Instant from, Instant to) {
        List<AuditLogEntity> logs = auditLogRepository.findByTimestampBetweenAndDeletedFalse(from, to);

        Instant now = Instant.now();
        logs.forEach(log -> {
            log.setDeleted(true);
            log.setDeletedAt(now);
            log.setDeletedBy("system:period-delete");
        });

        auditLogRepository.saveAll(logs);
        return logs.size();
    }

    /**
     * 특정 사용자의 로그 삭제 (Soft Delete)
     */
    @Transactional
    public int softDeleteByUser(String userId) {
        List<AuditLogEntity> logs = auditLogRepository.findByUserIdAndDeletedFalse(userId);

        Instant now = Instant.now();
        logs.forEach(log -> {
            log.setDeleted(true);
            log.setDeletedAt(now);
            log.setDeletedBy("system:user-delete");
        });

        auditLogRepository.saveAll(logs);
        return logs.size();
    }

    /**
     * 삭제된 로그 복원
     */
    @Transactional
    public int restoreDeletedLogs(List<Long> ids) {
        List<AuditLogEntity> logs = auditLogRepository.findAllById(ids);

        logs.forEach(log -> {
            if (log.getDeleted()) {
                log.setDeleted(false);
                log.setDeletedAt(null);
                log.setDeletedBy(null);
            }
        });

        auditLogRepository.saveAll(logs);
        return logs.size();
    }

    /**
     * 보관 정책 실행 결과
     */
    public static class RetentionResult {
        private final int softDeletedCount;
        private final int hardDeletedCount;

        public RetentionResult(int softDeletedCount, int hardDeletedCount) {
            this.softDeletedCount = softDeletedCount;
            this.hardDeletedCount = hardDeletedCount;
        }

        public int getSoftDeletedCount() {
            return softDeletedCount;
        }

        public int getHardDeletedCount() {
            return hardDeletedCount;
        }

        public int getTotalDeletedCount() {
            return softDeletedCount + hardDeletedCount;
        }

        @Override
        public String toString() {
            return String.format("RetentionResult{softDeleted=%d, hardDeleted=%d, total=%d}",
                    softDeletedCount, hardDeletedCount, getTotalDeletedCount());
        }
    }
}
