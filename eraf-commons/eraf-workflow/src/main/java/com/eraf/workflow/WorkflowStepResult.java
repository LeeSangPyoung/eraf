package com.eraf.workflow;

import java.util.Collections;
import java.util.Map;

/**
 * Workflow Step 실행 결과
 * <p>
 * 핸들러가 반환하는 결과 객체로, 다음 동작을 결정합니다.
 * <ul>
 *   <li>COMPLETED - 다음 Step으로 진행</li>
 *   <li>FAILED - Workflow 실패 처리</li>
 *   <li>WAITING - Workflow를 SUSPENDED 상태로 전환 (승인 대기)</li>
 * </ul>
 */
public class WorkflowStepResult {

    private final WorkflowStepResultStatus status;
    private final Map<String, Object> outputData;
    private final String errorMessage;
    private final String nextStep;

    private WorkflowStepResult(WorkflowStepResultStatus status, Map<String, Object> outputData,
                                String errorMessage, String nextStep) {
        this.status = status;
        this.outputData = outputData != null ? Collections.unmodifiableMap(outputData) : Collections.emptyMap();
        this.errorMessage = errorMessage;
        this.nextStep = nextStep;
    }

    /**
     * 성공 결과 생성
     */
    public static WorkflowStepResult completed() {
        return new WorkflowStepResult(WorkflowStepResultStatus.COMPLETED, null, null, null);
    }

    /**
     * 성공 결과 생성 (출력 데이터 포함)
     */
    public static WorkflowStepResult completed(Map<String, Object> outputData) {
        return new WorkflowStepResult(WorkflowStepResultStatus.COMPLETED, outputData, null, null);
    }

    /**
     * 성공 결과 생성 (다음 Step 지정 - 조건 분기용)
     */
    public static WorkflowStepResult completedWithNext(String nextStep) {
        return new WorkflowStepResult(WorkflowStepResultStatus.COMPLETED, null, null, nextStep);
    }

    /**
     * 성공 결과 생성 (출력 데이터 + 다음 Step 지정)
     */
    public static WorkflowStepResult completedWithNext(Map<String, Object> outputData, String nextStep) {
        return new WorkflowStepResult(WorkflowStepResultStatus.COMPLETED, outputData, null, nextStep);
    }

    /**
     * 실패 결과 생성
     */
    public static WorkflowStepResult failed(String errorMessage) {
        return new WorkflowStepResult(WorkflowStepResultStatus.FAILED, null, errorMessage, null);
    }

    /**
     * 대기 결과 생성 (승인 대기 등)
     */
    public static WorkflowStepResult waiting() {
        return new WorkflowStepResult(WorkflowStepResultStatus.WAITING, null, null, null);
    }

    /**
     * 대기 결과 생성 (출력 데이터 포함)
     */
    public static WorkflowStepResult waiting(Map<String, Object> outputData) {
        return new WorkflowStepResult(WorkflowStepResultStatus.WAITING, outputData, null, null);
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

    public String getNextStep() {
        return nextStep;
    }

    public boolean isCompleted() {
        return status == WorkflowStepResultStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == WorkflowStepResultStatus.FAILED;
    }

    public boolean isWaiting() {
        return status == WorkflowStepResultStatus.WAITING;
    }

    @Override
    public String toString() {
        return "WorkflowStepResult{status=" + status +
                (errorMessage != null ? ", error='" + errorMessage + "'" : "") +
                (nextStep != null ? ", nextStep='" + nextStep + "'" : "") +
                "}";
    }
}
