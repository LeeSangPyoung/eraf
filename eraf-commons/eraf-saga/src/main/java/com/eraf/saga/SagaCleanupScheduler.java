package com.eraf.saga;

import com.eraf.saga.repository.SagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Saga 클린업 스케줄러
 * 완료된 Saga 실행 기록을 주기적으로 정리합니다.
 */
public class SagaCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SagaCleanupScheduler.class);

    private final SagaRepository repository;
    private final ErafSagaProperties properties;

    public SagaCleanupScheduler(SagaRepository repository, ErafSagaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${eraf.saga.cleanup-interval:3600000}")
    public void cleanup() {
        long retentionMillis = properties.getRetentionPeriod().toMillis();
        log.debug("Running saga cleanup, retention period: {} ms", retentionMillis);

        try {
            repository.cleanupOlderThan(retentionMillis);
            log.debug("Saga cleanup completed");
        } catch (Exception e) {
            log.error("Saga cleanup failed", e);
        }
    }
}
