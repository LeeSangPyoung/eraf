package com.eraf.saga.execution;

import com.eraf.saga.core.SagaStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Saga 실행 인스턴스
 * 하나의 Saga 실행에 대한 상태를 추적합니다.
 */
public class SagaExecution {

    private String id;
    private String sagaName;
    private String traceId;
    private SagaStatus status;
    private int currentStep;
    private List<StepExecution> steps;
    private Map<String, Object> context;
    private Instant startedAt;
    private Instant completedAt;
    private String failureReason;
    private int retryCount;

    public SagaExecution() {
        this.id = UUID.randomUUID().toString();
        this.status = SagaStatus.STARTED;
        this.currentStep = 0;
        this.steps = new ArrayList<>();
        this.context = new HashMap<>();
        this.startedAt = Instant.now();
        this.retryCount = 0;
    }

    public SagaExecution(String sagaName) {
        this();
        this.sagaName = sagaName;
    }

    public SagaExecution(String sagaName, String traceId) {
        this(sagaName);
        this.traceId = traceId;
    }

    public void addStep(StepExecution step) {
        this.steps.add(step);
    }

    public StepExecution getCurrentStepExecution() {
        if (currentStep > 0 && currentStep <= steps.size()) {
            return steps.get(currentStep - 1);
        }
        return null;
    }

    public void markCompleted() {
        this.status = SagaStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = SagaStatus.COMPENSATING;
        this.failureReason = reason;
    }

    public void markCompensated() {
        this.status = SagaStatus.COMPENSATED;
        this.completedAt = Instant.now();
    }

    public void markCompensationFailed() {
        this.status = SagaStatus.FAILED;
        this.completedAt = Instant.now();
    }

    // Context helpers
    public void putContext(String key, Object value) {
        this.context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getContext(String key) {
        return (T) this.context.get(key);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSagaName() {
        return sagaName;
    }

    public void setSagaName(String sagaName) {
        this.sagaName = sagaName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public List<StepExecution> getSteps() {
        return steps;
    }

    public void setSteps(List<StepExecution> steps) {
        this.steps = steps;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
