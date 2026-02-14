package com.eraf.workflow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow 런타임 인스턴스
 * <p>
 * WorkflowDefinition을 기반으로 실행된 워크플로우의 현재 상태를 관리합니다.
 * 변수(variables)를 통해 Step 간 데이터를 공유하고,
 * Step 실행 이력(history)을 기록합니다.
 */
public class WorkflowInstance {

    private final String id;
    private final String workflowName;
    private WorkflowStatus status;
    private String currentStep;
    private final Map<String, Object> variables;
    private final List<WorkflowStepExecution> history;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;

    public WorkflowInstance(String workflowName, Map<String, Object> variables) {
        this.id = UUID.randomUUID().toString();
        this.workflowName = workflowName;
        this.status = WorkflowStatus.CREATED;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        this.history = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public List<WorkflowStepExecution> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // --- 상태 변경 메서드 ---

    /**
     * 실행 시작
     */
    void markRunning(String firstStep) {
        this.status = WorkflowStatus.RUNNING;
        this.currentStep = firstStep;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 현재 Step 업데이트
     */
    void setCurrentStep(String stepName) {
        this.currentStep = stepName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 완료 처리
     */
    void markCompleted() {
        this.status = WorkflowStatus.COMPLETED;
        this.currentStep = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 실패 처리
     */
    void markFailed(String errorMessage) {
        this.status = WorkflowStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 일시 중단 (승인 대기)
     */
    void markSuspended() {
        this.status = WorkflowStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 취소 처리
     */
    void markCancelled() {
        this.status = WorkflowStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Step 실행 이력 추가
     */
    void addStepExecution(WorkflowStepExecution execution) {
        this.history.add(execution);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 변수 설정
     */
    void putVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    /**
     * 변수 일괄 설정
     */
    void putVariables(Map<String, Object> vars) {
        if (vars != null) {
            this.variables.putAll(vars);
        }
    }

    /**
     * 실행 가능 상태인지 확인
     */
    public boolean isExecutable() {
        return status == WorkflowStatus.CREATED || status == WorkflowStatus.SUSPENDED;
    }

    /**
     * 종료 상태인지 확인
     */
    public boolean isTerminal() {
        return status == WorkflowStatus.COMPLETED
                || status == WorkflowStatus.FAILED
                || status == WorkflowStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "WorkflowInstance{id='" + id +
                "', workflow='" + workflowName +
                "', status=" + status +
                ", currentStep='" + currentStep +
                "', createdAt=" + createdAt + "}";
    }
}
