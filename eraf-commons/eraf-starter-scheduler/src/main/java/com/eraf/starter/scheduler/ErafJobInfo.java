package com.eraf.starter.scheduler;

import java.time.Instant;

/**
 * ERAF 작업 정보
 */
public class ErafJobInfo {

    private String name;
    private String group;
    private String description;
    private String cron;
    private Long fixedDelay;
    private Long fixedRate;
    private boolean lockEnabled;
    private String lockAtMostFor;
    private String lockAtLeastFor;
    private JobStatus status;
    private Instant lastExecutionTime;
    private Instant nextExecutionTime;
    private String lastExecutionResult;

    public enum JobStatus {
        SCHEDULED, RUNNING, PAUSED, COMPLETED, FAILED
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }

    public Long getFixedDelay() { return fixedDelay; }
    public void setFixedDelay(Long fixedDelay) { this.fixedDelay = fixedDelay; }

    public Long getFixedRate() { return fixedRate; }
    public void setFixedRate(Long fixedRate) { this.fixedRate = fixedRate; }

    public boolean isLockEnabled() { return lockEnabled; }
    public void setLockEnabled(boolean lockEnabled) { this.lockEnabled = lockEnabled; }

    public String getLockAtMostFor() { return lockAtMostFor; }
    public void setLockAtMostFor(String lockAtMostFor) { this.lockAtMostFor = lockAtMostFor; }

    public String getLockAtLeastFor() { return lockAtLeastFor; }
    public void setLockAtLeastFor(String lockAtLeastFor) { this.lockAtLeastFor = lockAtLeastFor; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public Instant getLastExecutionTime() { return lastExecutionTime; }
    public void setLastExecutionTime(Instant lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }

    public Instant getNextExecutionTime() { return nextExecutionTime; }
    public void setNextExecutionTime(Instant nextExecutionTime) { this.nextExecutionTime = nextExecutionTime; }

    public String getLastExecutionResult() { return lastExecutionResult; }
    public void setLastExecutionResult(String lastExecutionResult) { this.lastExecutionResult = lastExecutionResult; }
}
