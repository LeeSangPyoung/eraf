package com.eraf.saga.execution;

import com.eraf.saga.core.StepStatus;

import java.time.Instant;

/**
 * Saga Step 실행 인스턴스
 */
public class StepExecution {

    private int order;
    private String name;
    private StepStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private String output;
    private String errorMessage;
    private int retryCount;

    public StepExecution() {
        this.status = StepStatus.PENDING;
        this.retryCount = 0;
    }

    public StepExecution(int order, String name) {
        this();
        this.order = order;
        this.name = name;
    }

    public void markRunning() {
        this.status = StepStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void markSuccess(String output) {
        this.status = StepStatus.SUCCESS;
        this.output = output;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = StepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public void markCompensating() {
        this.status = StepStatus.COMPENSATING;
    }

    public void markCompensated() {
        this.status = StepStatus.COMPENSATED;
        this.completedAt = Instant.now();
    }

    public void markSkipped() {
        this.status = StepStatus.SKIPPED;
    }

    public boolean isCompleted() {
        return status == StepStatus.SUCCESS ||
               status == StepStatus.COMPENSATED ||
               status == StepStatus.SKIPPED;
    }

    // Getters and Setters
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
