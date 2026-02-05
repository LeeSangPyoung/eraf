package com.eraf.saga.event;

import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.core.StepStatus;

import java.time.Instant;

/**
 * Saga 이벤트
 * Saga 실행 상태 변경 시 발행되는 이벤트
 */
public class SagaEvent {

    private String eventId;
    private SagaEventType eventType;
    private String sagaId;
    private String sagaName;
    private String traceId;
    private SagaStatus sagaStatus;
    private Integer stepOrder;
    private String stepName;
    private StepStatus stepStatus;
    private String errorMessage;
    private Instant timestamp;

    public SagaEvent() {
        this.timestamp = Instant.now();
    }

    // Static factory methods
    public static SagaEvent sagaStarted(String sagaId, String sagaName, String traceId) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.SAGA_STARTED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setTraceId(traceId);
        event.setSagaStatus(SagaStatus.STARTED);
        return event;
    }

    public static SagaEvent sagaCompleted(String sagaId, String sagaName, String traceId) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.SAGA_COMPLETED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setTraceId(traceId);
        event.setSagaStatus(SagaStatus.COMPLETED);
        return event;
    }

    public static SagaEvent sagaFailed(String sagaId, String sagaName, String traceId, String error) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.SAGA_FAILED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setTraceId(traceId);
        event.setSagaStatus(SagaStatus.FAILED);
        event.setErrorMessage(error);
        return event;
    }

    public static SagaEvent sagaCompensated(String sagaId, String sagaName, String traceId) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.SAGA_COMPENSATED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setTraceId(traceId);
        event.setSagaStatus(SagaStatus.COMPENSATED);
        return event;
    }

    public static SagaEvent stepStarted(String sagaId, String sagaName, int stepOrder, String stepName) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.STEP_STARTED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setStepOrder(stepOrder);
        event.setStepName(stepName);
        event.setStepStatus(StepStatus.RUNNING);
        return event;
    }

    public static SagaEvent stepCompleted(String sagaId, String sagaName, int stepOrder, String stepName) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.STEP_COMPLETED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setStepOrder(stepOrder);
        event.setStepName(stepName);
        event.setStepStatus(StepStatus.SUCCESS);
        return event;
    }

    public static SagaEvent stepFailed(String sagaId, String sagaName, int stepOrder, String stepName, String error) {
        SagaEvent event = new SagaEvent();
        event.setEventType(SagaEventType.STEP_FAILED);
        event.setSagaId(sagaId);
        event.setSagaName(sagaName);
        event.setStepOrder(stepOrder);
        event.setStepName(stepName);
        event.setStepStatus(StepStatus.FAILED);
        event.setErrorMessage(error);
        return event;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public SagaEventType getEventType() { return eventType; }
    public void setEventType(SagaEventType eventType) { this.eventType = eventType; }
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getSagaName() { return sagaName; }
    public void setSagaName(String sagaName) { this.sagaName = sagaName; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public SagaStatus getSagaStatus() { return sagaStatus; }
    public void setSagaStatus(SagaStatus sagaStatus) { this.sagaStatus = sagaStatus; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public StepStatus getStepStatus() { return stepStatus; }
    public void setStepStatus(StepStatus stepStatus) { this.stepStatus = stepStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
