package com.eraf.starter.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF 스케줄러 설정
 */
@ConfigurationProperties(prefix = "eraf.scheduler")
public class ErafSchedulerProperties {

    /**
     * 스케줄러 활성화
     */
    private boolean enabled = true;

    /**
     * 분산 락 활성화
     */
    private boolean distributedLockEnabled = true;

    /**
     * 기본 락 유지 시간
     */
    private Duration defaultLockAtMost = Duration.ofMinutes(5);

    /**
     * 기본 최소 락 유지 시간
     */
    private Duration defaultLockAtLeast = Duration.ofSeconds(30);

    /**
     * 테이블 이름 (JDBC 락 사용 시)
     */
    private String tableName = "shedlock";

    /**
     * 스레드 풀 크기
     */
    private int poolSize = 5;

    /**
     * 작업당 최대 이력 수
     */
    private int maxHistoryPerJob = 100;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDistributedLockEnabled() {
        return distributedLockEnabled;
    }

    public void setDistributedLockEnabled(boolean distributedLockEnabled) {
        this.distributedLockEnabled = distributedLockEnabled;
    }

    public Duration getDefaultLockAtMost() {
        return defaultLockAtMost;
    }

    public void setDefaultLockAtMost(Duration defaultLockAtMost) {
        this.defaultLockAtMost = defaultLockAtMost;
    }

    public Duration getDefaultLockAtLeast() {
        return defaultLockAtLeast;
    }

    public void setDefaultLockAtLeast(Duration defaultLockAtLeast) {
        this.defaultLockAtLeast = defaultLockAtLeast;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getMaxHistoryPerJob() {
        return maxHistoryPerJob;
    }

    public void setMaxHistoryPerJob(int maxHistoryPerJob) {
        this.maxHistoryPerJob = maxHistoryPerJob;
    }
}
