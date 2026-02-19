package com.eraf.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Workflow Engine 테스트")
class WorkflowEngineTest {

    private WorkflowEngine workflowEngine;
    private WorkflowDefinition simpleWorkflow;

    @BeforeEach
    void setUp() {
        workflowEngine = new WorkflowEngine();
        simpleWorkflow = createSimpleWorkflow();

        // 테스트용 핸들러 등록
        workflowEngine.registerHandler("test-step", context -> {
            context.setVariable("executed", true);
            return WorkflowStepResult.completed();
        });
    }

    @Test
    @DisplayName("워크플로우 정의 등록")
    void shouldRegisterWorkflowDefinition() {
        // When
        workflowEngine.registerDefinition(simpleWorkflow);

        // Then
        WorkflowDefinition retrieved = workflowEngine.getDefinition("simple-workflow");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getName()).isEqualTo("simple-workflow");
    }

    @Test
    @DisplayName("워크플로우 실행 및 완료")
    void shouldExecuteWorkflowSuccessfully() {
        // Given
        workflowEngine.registerDefinition(simpleWorkflow);
        Map<String, Object> input = new HashMap<>();
        input.put("userId", "user-123");
        input.put("orderId", "order-456");

        // When
        WorkflowInstance instance = workflowEngine.startWorkflow("simple-workflow", input);

        // Then
        assertThat(instance).isNotNull();
        assertThat(instance.getWorkflowName()).isEqualTo("simple-workflow");
        assertThat(instance.getVariables()).containsEntry("userId", "user-123");
        assertThat(instance.getVariables()).containsEntry("executed", true);
    }

    @Test
    @DisplayName("등록되지 않은 워크플로우 실행 시 예외 발생")
    void shouldThrowExceptionForUnregisteredWorkflow() {
        // When & Then
        assertThatThrownBy(() ->
            workflowEngine.startWorkflow("non-existent-workflow", Map.of())
        )
            .isInstanceOf(WorkflowException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("워크플로우 컨텍스트에 데이터 저장 및 조회")
    void shouldStoreAndRetrieveDataInContext() {
        // Given
        WorkflowContext context = new WorkflowContext("instance-1", "step-1", Map.of());
        context.setVariable("key1", "value1");
        context.setVariable("key2", 123);
        context.setVariable("key3", true);

        // When & Then
        assertThat((String) context.getVariable("key1")).isEqualTo("value1");
        assertThat((Integer) context.getVariable("key2")).isEqualTo(123);
        assertThat((Boolean) context.getVariable("key3")).isEqualTo(true);
    }

    @Test
    @DisplayName("워크플로우 인스턴스 상태 추적")
    void shouldTrackWorkflowInstanceStatus() {
        // Given
        workflowEngine.registerDefinition(simpleWorkflow);
        Map<String, Object> input = Map.of("testKey", "testValue");

        // When
        WorkflowInstance instance = workflowEngine.startWorkflow("simple-workflow", input);

        // Then
        assertThat(instance.getId()).isNotNull();
        assertThat(instance.getWorkflowName()).isEqualTo("simple-workflow");
        assertThat(instance.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("워크플로우 취소")
    void shouldCancelWorkflow() {
        // Given
        WorkflowDefinition suspendingWorkflow = createSuspendingWorkflow();
        workflowEngine.registerDefinition(suspendingWorkflow);

        // 승인 대기 상태를 만드는 핸들러
        workflowEngine.registerHandler("approval-step", context ->
            WorkflowStepResult.waiting()
        );

        WorkflowInstance instance = workflowEngine.startWorkflow("suspending-workflow", Map.of());

        // When
        WorkflowInstance cancelledInstance = workflowEngine.cancelWorkflow(instance.getId());

        // Then
        assertThat(cancelledInstance.getStatus()).isEqualTo(WorkflowStatus.CANCELLED);
    }

    @Test
    @DisplayName("존재하지 않는 인스턴스 조회 시 예외 발생")
    void shouldThrowExceptionForNonExistentInstance() {
        // When & Then
        assertThatThrownBy(() ->
            workflowEngine.getInstance("non-existent-id")
        )
            .isInstanceOf(WorkflowException.class)
            .hasMessageContaining("not found");
    }

    // Helper methods - Using builder pattern (production API)
    private WorkflowDefinition createSimpleWorkflow() {
        WorkflowStep step = WorkflowStep.builder("test-step")
                .type(WorkflowStepType.TASK)
                .build();

        return WorkflowDefinition.builder("simple-workflow")
                .description("A simple workflow for testing")
                .step(step)
                .build();
    }

    private WorkflowDefinition createSuspendingWorkflow() {
        WorkflowStep approvalStep = WorkflowStep.builder("approval-step")
                .type(WorkflowStepType.APPROVAL)
                .build();

        return WorkflowDefinition.builder("suspending-workflow")
                .step(approvalStep)
                .build();
    }

    // Alternative: Using simple API (matches README examples)
    @SuppressWarnings("unused")
    private WorkflowDefinition createSimpleWorkflowUsingSimpleAPI() {
        SimpleWorkflowDefinition workflow = new SimpleWorkflowDefinition();
        workflow.setName("simple-workflow");
        workflow.setDescription("A simple workflow for testing");

        SimpleWorkflowStep step = new SimpleWorkflowStep();
        step.setName("test-step");
        step.setType(WorkflowStepType.TASK);

        workflow.addStep(step);
        return workflow.build();
    }
}
