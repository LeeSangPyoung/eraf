package com.eraf.saga.repository.jpa;

import com.eraf.saga.core.SagaStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Saga 실행 JPA Entity
 */
@Entity
@Table(name = "eraf_saga_execution", indexes = {
        @Index(name = "idx_saga_trace_id", columnList = "traceId"),
        @Index(name = "idx_saga_name_status", columnList = "sagaName, status"),
        @Index(name = "idx_saga_status", columnList = "status"),
        @Index(name = "idx_saga_completed_at", columnList = "completedAt")
})
public class SagaExecutionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String sagaName;

    @Column(length = 36)
    private String traceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SagaStatus status;

    @Column(nullable = false)
    private int currentStep;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String stepsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String contextJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String inputJson;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private int retryCount;

    @Version
    private Long version;

    public SagaExecutionEntity() {
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

    public String getStepsJson() {
        return stepsJson;
    }

    public void setStepsJson(String stepsJson) {
        this.stepsJson = stepsJson;
    }

    public String getContextJson() {
        return contextJson;
    }

    public void setContextJson(String contextJson) {
        this.contextJson = contextJson;
    }

    public String getInputJson() {
        return inputJson;
    }

    public void setInputJson(String inputJson) {
        this.inputJson = inputJson;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
