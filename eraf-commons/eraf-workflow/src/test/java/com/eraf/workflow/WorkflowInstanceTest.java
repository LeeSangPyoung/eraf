package com.eraf.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowInstance 테스트")
class WorkflowInstanceTest {

    private WorkflowInstance instance;

    @BeforeEach
    void setUp() {
        instance = new WorkflowInstance("test-workflow", Map.of("key", "value"));
    }

    @Test
    @DisplayName("초기 상태 확인")
    void newInstance_ShouldHaveCreatedStatus() {
        assertThat(instance.getId()).isNotNull();
        assertThat(instance.getWorkflowName()).isEqualTo("test-workflow");
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.CREATED);
        assertThat(instance.getCurrentStep()).isNull();
        assertThat(instance.getVariables()).containsEntry("key", "value");
        assertThat(instance.getHistory()).isEmpty();
        assertThat(instance.getCreatedAt()).isNotNull();
        assertThat(instance.getUpdatedAt()).isNotNull();
        assertThat(instance.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("null variables로 생성 시 빈 맵")
    void newInstance_WithNullVariables_ShouldHaveEmptyMap() {
        WorkflowInstance inst = new WorkflowInstance("wf", null);
        assertThat(inst.getVariables()).isEmpty();
    }

    @Test
    @DisplayName("RUNNING 상태 전환")
    void markRunning_ShouldUpdateStatusAndStep() {
        instance.markRunning("first-step");

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.RUNNING);
        assertThat(instance.getCurrentStep()).isEqualTo("first-step");
    }

    @Test
    @DisplayName("COMPLETED 상태 전환")
    void markCompleted_ShouldUpdateStatus() {
        instance.markRunning("step");
        instance.markCompleted();

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(instance.getCurrentStep()).isNull();
    }

    @Test
    @DisplayName("FAILED 상태 전환")
    void markFailed_ShouldUpdateStatusAndError() {
        instance.markRunning("step");
        instance.markFailed("Something went wrong");

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(instance.getErrorMessage()).isEqualTo("Something went wrong");
    }

    @Test
    @DisplayName("SUSPENDED 상태 전환")
    void markSuspended_ShouldUpdateStatus() {
        instance.markRunning("approval");
        instance.markSuspended();

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.SUSPENDED);
    }

    @Test
    @DisplayName("CANCELLED 상태 전환")
    void markCancelled_ShouldUpdateStatus() {
        instance.markRunning("step");
        instance.markCancelled();

        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.CANCELLED);
    }

    @Test
    @DisplayName("실행 가능 상태 확인")
    void isExecutable_ShouldReturnTrueForCreatedAndSuspended() {
        assertThat(instance.isExecutable()).isTrue(); // CREATED

        instance.markRunning("step");
        assertThat(instance.isExecutable()).isFalse(); // RUNNING

        instance.markSuspended();
        assertThat(instance.isExecutable()).isTrue(); // SUSPENDED
    }

    @Test
    @DisplayName("종료 상태 확인")
    void isTerminal_ShouldReturnTrueForTerminalStates() {
        assertThat(instance.isTerminal()).isFalse(); // CREATED

        instance.markRunning("step");
        assertThat(instance.isTerminal()).isFalse(); // RUNNING

        instance.markCompleted();
        assertThat(instance.isTerminal()).isTrue(); // COMPLETED
    }

    @Test
    @DisplayName("FAILED는 종료 상태")
    void isTerminal_Failed_ShouldBeTerminal() {
        instance.markRunning("step");
        instance.markFailed("error");
        assertThat(instance.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("CANCELLED는 종료 상태")
    void isTerminal_Cancelled_ShouldBeTerminal() {
        instance.markRunning("step");
        instance.markCancelled();
        assertThat(instance.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("Step 실행 이력 추가")
    void addStepExecution_ShouldAddToHistory() {
        WorkflowStepExecution execution = new WorkflowStepExecution(
                "step-1", WorkflowStepResultStatus.COMPLETED, Map.of("output", "data"), null);
        instance.addStepExecution(execution);

        assertThat(instance.getHistory()).hasSize(1);
        assertThat(instance.getHistory().get(0).getStepName()).isEqualTo("step-1");
        assertThat(instance.getHistory().get(0).getStatus()).isEqualTo(WorkflowStepResultStatus.COMPLETED);
    }

    @Test
    @DisplayName("변수 설정 및 일괄 설정")
    void putVariable_ShouldUpdateVariables() {
        instance.putVariable("newKey", "newValue");
        assertThat(instance.getVariables()).containsEntry("newKey", "newValue");

        instance.putVariables(Map.of("a", 1, "b", 2));
        assertThat(instance.getVariables()).containsEntry("a", 1);
        assertThat(instance.getVariables()).containsEntry("b", 2);
    }

    @Test
    @DisplayName("putVariables null 전달 시 안전")
    void putVariables_WithNull_ShouldNotThrow() {
        instance.putVariables(null);
        assertThat(instance.getVariables()).containsEntry("key", "value");
    }

    @Test
    @DisplayName("currentStep 업데이트")
    void setCurrentStep_ShouldUpdateStep() {
        instance.setCurrentStep("step-2");
        assertThat(instance.getCurrentStep()).isEqualTo("step-2");
    }

    @Test
    @DisplayName("toString에 주요 정보 포함")
    void toString_ShouldContainKeyInfo() {
        String str = instance.toString();
        assertThat(str).contains("test-workflow");
        assertThat(str).contains("CREATED");
    }
}
