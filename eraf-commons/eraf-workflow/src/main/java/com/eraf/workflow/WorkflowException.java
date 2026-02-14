package com.eraf.workflow;

/**
 * Workflow 실행 중 발생하는 예외
 */
public class WorkflowException extends RuntimeException {

    private final String workflowInstanceId;

    public WorkflowException(String message) {
        super(message);
        this.workflowInstanceId = null;
    }

    public WorkflowException(String message, String workflowInstanceId) {
        super(message);
        this.workflowInstanceId = workflowInstanceId;
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
        this.workflowInstanceId = null;
    }

    public WorkflowException(String message, String workflowInstanceId, Throwable cause) {
        super(message, cause);
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }
}
