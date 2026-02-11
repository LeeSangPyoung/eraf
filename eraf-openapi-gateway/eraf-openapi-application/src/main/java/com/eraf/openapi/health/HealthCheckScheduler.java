package com.eraf.openapi.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Health Check 주기적 실행 Scheduler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;

    /**
     * 30초마다 모든 활성화된 Target에 대해 Health Check 수행
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void scheduleHealthChecks() {
        log.debug("Scheduled health check started");
        try {
            healthCheckService.performHealthChecks();
        } catch (Exception e) {
            log.error("Scheduled health check failed: {}", e.getMessage(), e);
        }
    }
}
