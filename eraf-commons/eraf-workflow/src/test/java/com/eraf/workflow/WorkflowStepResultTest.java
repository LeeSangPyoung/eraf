package com.eraf.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowStepResult 테스트")
class WorkflowStepResultTest {

    @Test
    @DisplayName("completed() 결과 생성")
    void completed_ShouldCreateCompletedResult() {
        WorkflowStepResult result = WorkflowStepResult.completed();

        assertThat(result.getStatus()).isEqualTo(WorkflowStepResultStatus.COMPLETED);
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.isFailed()).isFalse();
        assertThat(result.isWaiting()).isFalse();
        assertThat(result.getOutputData()).isEmpty();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getNextStep()).isNull();
    }

    @Test
    @DisplayName("completed(outputData) 결과 생성")
    void completed_WithOutputData_ShouldIncludeData() {
        Map<String, Object> output = Map.of("result", "success", "count", 10);
        WorkflowStepResult result = WorkflowStepResult.completed(output);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getOutputData()).containsEntry("result", "success");
        assertThat(result.getOutputData()).containsEntry("count", 10);
    }

    @Test
    @DisplayName("completedWithNext() 조건 분기 결과")
    void completedWithNext_ShouldSetNextStep() {
        WorkflowStepResult result = WorkflowStepResult.completedWithNext("approval-step");

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getNextStep()).isEqualTo("approval-step");
    }

    @Test
    @DisplayName("completedWithNext(outputData, nextStep) 결과")
    void completedWithNext_WithOutput_ShouldSetBoth() {
        Map<String, Object> output = Map.of("validated", true);
        WorkflowStepResult result = WorkflowStepResult.completedWithNext(output, "next-step");

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getNextStep()).isEqualTo("next-step");
        assertThat(result.getOutputData()).containsEntry("validated", true);
    }

    @Test
    @DisplayName("failed() 결과 생성")
    void failed_ShouldCreateFailedResult() {
        WorkflowStepResult result = WorkflowStepResult.failed("Validation error");

        assertThat(result.getStatus()).isEqualTo(WorkflowStepResultStatus.FAILED);
        assertThat(result.isFailed()).isTrue();
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.isWaiting()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Validation error");
    }

    @Test
    @DisplayName("waiting() 결과 생성")
    void waiting_ShouldCreateWaitingResult() {
        WorkflowStepResult result = WorkflowStepResult.waiting();

        assertThat(result.getStatus()).isEqualTo(WorkflowStepResultStatus.WAITING);
        assertThat(result.isWaiting()).isTrue();
        assertThat(result.isCompleted()).isFalse();
        assertThat(result.isFailed()).isFalse();
    }

    @Test
    @DisplayName("waiting(outputData) 결과 생성")
    void waiting_WithOutputData_ShouldIncludeData() {
        Map<String, Object> output = Map.of("approver", "manager");
        WorkflowStepResult result = WorkflowStepResult.waiting(output);

        assertThat(result.isWaiting()).isTrue();
        assertThat(result.getOutputData()).containsEntry("approver", "manager");
    }

    @Test
    @DisplayName("toString에 주요 정보 포함")
    void toString_ShouldContainKeyInfo() {
        WorkflowStepResult failed = WorkflowStepResult.failed("error msg");
        assertThat(failed.toString()).contains("FAILED");
        assertThat(failed.toString()).contains("error msg");

        WorkflowStepResult withNext = WorkflowStepResult.completedWithNext("next");
        assertThat(withNext.toString()).contains("COMPLETED");
        assertThat(withNext.toString()).contains("next");
    }
}
