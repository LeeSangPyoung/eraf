package com.eraf.saga.core;

/**
 * Saga 관련 예외
 */
public class SagaException extends RuntimeException {

    private String sagaName;
    private String executionId;
    private int stepOrder;

    public SagaException(String message) {
        super(message);
    }

    public SagaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SagaException(String sagaName, String message) {
        super(message);
        this.sagaName = sagaName;
    }

    public SagaException(String sagaName, String executionId, int stepOrder, String message, Throwable cause) {
        super(message, cause);
        this.sagaName = sagaName;
        this.executionId = executionId;
        this.stepOrder = stepOrder;
    }

    public String getSagaName() {
        return sagaName;
    }

    public String getExecutionId() {
        return executionId;
    }

    public int getStepOrder() {
        return stepOrder;
    }
}
