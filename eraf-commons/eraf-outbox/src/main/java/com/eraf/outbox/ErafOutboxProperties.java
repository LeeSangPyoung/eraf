package com.eraf.outbox;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Outbox Pattern 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.outbox")
public class ErafOutboxProperties {

    /**
     * Outbox 스케줄러 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 한 번에 처리할 메시지 수
     */
    @Positive @Max(1000)
    private int batchSize = 100;

    /**
     * 최대 재시도 횟수
     */
    @Positive
    private int maxRetries = 3;

    /**
     * 폴링 간격 (밀리초, fixedDelay)
     */
    @Positive
    private long pollInterval = 5000;

    /**
     * 처리 완료 메시지 보관 일수 (이후 자동 삭제)
     */
    @Positive
    private int cleanupDays = 7;

    /**
     * 정리 작업 Cron 표현식
     */
    @NotBlank
    private String cleanupCron = "0 0 3 * * ?";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getCleanupDays() {
        return cleanupDays;
    }

    public void setCleanupDays(int cleanupDays) {
        this.cleanupDays = cleanupDays;
    }

    public String getCleanupCron() {
        return cleanupCron;
    }

    public void setCleanupCron(String cleanupCron) {
        this.cleanupCron = cleanupCron;
    }
}
