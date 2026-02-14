package com.eraf.workflow;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow Step 실행 이력 레코드
 * <p>
 * WorkflowInstance 내에서 각 Step의 실행 결과를 기록합니다.
 */
public class WorkflowStepExecution {

    private final String stepName;
    private final WorkflowStepResultStatus status;
    private final Map<String, Object> outputData;
    private final String errorMessage;
    private final LocalDateTime executedAt;

    public WorkflowStepExecution(String stepName, WorkflowStepResultStatus status,
                                  Map<String, Object> outputData, String errorMessage) {
        this.stepName = stepName;
        this.status = status;
        this.outputData = outputData != null
                ? Collections.unmodifiableMap(new HashMap<>(outputData))
                : Collections.emptyMap();
        this.errorMessage = errorMessage;
        this.executedAt = LocalDateTime.now();
    }

    public String getStepName() {
        return stepName;
    }

    public WorkflowStepResultStatus getStatus() {
        return status;
    }

    public Map<String, Object> getOutputData() {
        return outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    @Override
    public String toString() {
        return "WorkflowStepExecution{step='" + stepName +
                "', status=" + status +
                ", executedAt=" + executedAt + "}";
    }
}
