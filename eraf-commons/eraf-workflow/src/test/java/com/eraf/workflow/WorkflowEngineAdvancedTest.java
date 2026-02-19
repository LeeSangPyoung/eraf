package com.eraf.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkflowEngine 고급 테스트")
class WorkflowEngineAdvancedTest {

    private WorkflowEngine engine;

    @BeforeEach
    void setUp() {
        engine = new WorkflowEngine();
    }

    @AfterEach
    void tearDown() {
        engine.close();
    }

    @Test
    @DisplayName("멀티 스텝 워크플로우 순차 실행")
    void multiStepWorkflow_ShouldExecuteSequentially() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("multi-step")
                .step(WorkflowStep.builder("step-1").type(WorkflowStepType.TASK).build())
                .step(WorkflowStep.builder("step-2").type(WorkflowStepType.TASK).build())
                .step(WorkflowStep.builder("step-3").type(WorkflowStepType.TASK).build())
                .build();

        engine.registerHandler("step-1", ctx -> {
            ctx.setVariable("step1Done", true);
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("step-2", ctx -> {
            ctx.setVariable("step2Done", true);
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("step-3", ctx -> {
            ctx.setVariable("step3Done", true);
            return WorkflowStepResult.completed();
        });

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(instance.getVariables()).containsEntry("step1Done", true);
        assertThat(instance.getVariables()).containsEntry("step2Done", true);
        assertThat(instance.getVariables()).containsEntry("step3Done", true);
        assertThat(instance.getHistory()).hasSize(3);
    }

    @Test
    @DisplayName("APPROVAL 스텝에서 일시정지 후 재개")
    void approvalWorkflow_ShouldSuspendAndResume() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("approval-flow")
                .step(WorkflowStep.builder("submit").type(WorkflowStepType.TASK).build())
                .step(WorkflowStep.builder("approve").type(WorkflowStepType.APPROVAL).build())
                .step(WorkflowStep.builder("complete").type(WorkflowStepType.TASK).build())
                .build();

        engine.registerHandler("submit", ctx -> {
            ctx.setVariable("submitted", true);
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("approve", ctx -> WorkflowStepResult.waiting());
        engine.registerHandler("complete", ctx -> {
            ctx.setVariable("completed", true);
            return WorkflowStepResult.completed();
        });

        // when - 워크플로우 시작
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of("docId", "DOC-001"));

        // then - SUSPENDED 상태
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.SUSPENDED);
        assertThat(instance.getCurrentStep()).isEqualTo("approve");
        assertThat(instance.getVariables()).containsEntry("submitted", true);

        // when - 재개
        WorkflowInstance resumed = engine.resumeWorkflow(
                instance.getId(), Map.of("approvedBy", "manager"));

        // then - COMPLETED 상태
        assertThat(resumed.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(resumed.getVariables()).containsEntry("completed", true);
        assertThat(resumed.getVariables()).containsEntry("approvedBy", "manager");
    }

    @Test
    @DisplayName("SUSPENDED가 아닌 인스턴스 resume 시 예외")
    void resumeWorkflow_NotSuspended_ShouldThrow() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("simple")
                .step(WorkflowStep.builder("done").build())
                .build();
        engine.registerHandler("done", ctx -> WorkflowStepResult.completed());
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThatThrownBy(() -> engine.resumeWorkflow(instance.getId()))
                .isInstanceOf(WorkflowException.class)
                .hasMessageContaining("not suspended");
    }

    @Test
    @DisplayName("스텝 실패 시 워크플로우 FAILED")
    void stepFailure_ShouldFailWorkflow() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("failing")
                .step(WorkflowStep.builder("fail-step").build())
                .build();
        engine.registerHandler("fail-step", ctx ->
                WorkflowStepResult.failed("Validation error"));

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(instance.getErrorMessage()).isEqualTo("Validation error");
    }

    @Test
    @DisplayName("핸들러 미등록 스텝 실행 시 FAILED")
    void missingHandler_ShouldFailWorkflow() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("no-handler")
                .step(WorkflowStep.builder("unregistered-step").build())
                .build();

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(instance.getErrorMessage()).contains("No handler registered");
    }

    @Test
    @DisplayName("조건 분기 - nextStep으로 이동")
    void conditionalBranching_ShouldFollowNextStep() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("branching")
                .step(WorkflowStep.builder("check").build())
                .step(WorkflowStep.builder("path-a").build())
                .step(WorkflowStep.builder("path-b").build())
                .build();

        engine.registerHandler("check", ctx -> {
            int amount = ctx.getVariable("amount", 0);
            if (amount > 1000) {
                return WorkflowStepResult.completedWithNext("path-b");
            }
            return WorkflowStepResult.completedWithNext("path-a");
        });
        engine.registerHandler("path-a", ctx -> {
            ctx.setVariable("path", "A");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("path-b", ctx -> {
            ctx.setVariable("path", "B");
            return WorkflowStepResult.completed();
        });

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of("amount", 5000));

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(instance.getVariables()).containsEntry("path", "B");
    }

    @Test
    @DisplayName("Step 간 변수 공유")
    void variableSharing_BetweenSteps() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("sharing")
                .step(WorkflowStep.builder("producer").build())
                .step(WorkflowStep.builder("consumer").build())
                .build();

        engine.registerHandler("producer", ctx -> {
            ctx.setVariable("sharedData", "hello from step 1");
            return WorkflowStepResult.completed(Map.of("outputKey", "outputValue"));
        });
        engine.registerHandler("consumer", ctx -> {
            String data = ctx.getVariable("sharedData");
            String output = ctx.getVariable("outputKey");
            ctx.setVariable("received", data + " | " + output);
            return WorkflowStepResult.completed();
        });

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getVariables())
                .containsEntry("received", "hello from step 1 | outputValue");
    }

    @Test
    @DisplayName("종료 상태 인스턴스 취소 시 예외")
    void cancelWorkflow_AlreadyTerminal_ShouldThrow() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("done")
                .step(WorkflowStep.builder("step").build())
                .build();
        engine.registerHandler("step", ctx -> WorkflowStepResult.completed());
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);

        // then
        assertThatThrownBy(() -> engine.cancelWorkflow(instance.getId()))
                .isInstanceOf(WorkflowException.class)
                .hasMessageContaining("terminal state");
    }

    @Test
    @DisplayName("상태별 인스턴스 조회")
    void getInstancesByStatus_ShouldFilterCorrectly() {
        // given
        WorkflowDefinition wf = WorkflowDefinition.builder("status-test")
                .step(WorkflowStep.builder("step").build())
                .build();
        engine.registerHandler("step", ctx -> WorkflowStepResult.completed());

        engine.startWorkflow(wf, Map.of());
        engine.startWorkflow(wf, Map.of());

        // when
        var completed = engine.getInstancesByStatus(WorkflowStatus.COMPLETED);
        var running = engine.getInstancesByStatus(WorkflowStatus.RUNNING);

        // then
        assertThat(completed).hasSize(2);
        assertThat(running).isEmpty();
    }

    @Test
    @DisplayName("최대 동시 워크플로우 제한")
    void maxConcurrentWorkflows_ShouldEnforceLimit() {
        // given
        WorkflowProperties props = new WorkflowProperties();
        props.setMaxConcurrentWorkflows(1);
        WorkflowEngine limitedEngine = new WorkflowEngine(props);

        WorkflowDefinition suspending = WorkflowDefinition.builder("suspending")
                .step(WorkflowStep.builder("wait").build())
                .build();
        limitedEngine.registerHandler("wait", ctx -> WorkflowStepResult.waiting());

        // 첫 번째 워크플로우 시작 (SUSPENDED 상태)
        limitedEngine.startWorkflow(suspending, Map.of());

        // when & then - 두 번째 워크플로우 시작 시 예외
        assertThatThrownBy(() -> limitedEngine.startWorkflow(suspending, Map.of()))
                .isInstanceOf(WorkflowException.class)
                .hasMessageContaining("Maximum concurrent workflows");

        limitedEngine.close();
    }

    @Test
    @DisplayName("Step 타임아웃")
    void stepTimeout_ShouldFailOnTimeout() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("timeout-test")
                .step(WorkflowStep.builder("slow-step")
                        .timeout(Duration.ofMillis(100))
                        .build())
                .build();
        engine.registerHandler("slow-step", ctx -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return WorkflowStepResult.completed();
        });

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.FAILED);
        assertThat(instance.getErrorMessage()).contains("timed out");
    }

    @Test
    @DisplayName("Step 재시도 - 첫 시도 실패 후 성공")
    void stepRetry_ShouldRetryOnFailure() {
        // given
        WorkflowDefinition definition = WorkflowDefinition.builder("retry-test")
                .step(WorkflowStep.builder("retry-step")
                        .retryable(true)
                        .maxRetries(2)
                        .build())
                .build();

        final int[] attemptCount = {0};
        engine.registerHandler("retry-step", ctx -> {
            attemptCount[0]++;
            if (attemptCount[0] < 2) {
                return WorkflowStepResult.failed("Temporary error");
            }
            return WorkflowStepResult.completed();
        });

        // when
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of());

        // then
        assertThat(instance.getStatus()).isEqualTo(WorkflowStatus.COMPLETED);
        assertThat(attemptCount[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("등록된 정의 이름 목록 조회")
    void getDefinitionNames_ShouldReturnAllRegistered() {
        // given
        engine.registerDefinition(WorkflowDefinition.builder("wf-a")
                .step(WorkflowStep.builder("s").build()).build());
        engine.registerDefinition(WorkflowDefinition.builder("wf-b")
                .step(WorkflowStep.builder("s").build()).build());

        // when
        var names = engine.getDefinitionNames();

        // then
        assertThat(names).containsExactlyInAnyOrder("wf-a", "wf-b");
    }

    @Test
    @DisplayName("getAllInstances 조회")
    void getAllInstances_ShouldReturnAll() {
        // given
        WorkflowDefinition wf = WorkflowDefinition.builder("all-test")
                .step(WorkflowStep.builder("s").build())
                .build();
        engine.registerHandler("s", ctx -> WorkflowStepResult.completed());
        engine.startWorkflow(wf, Map.of());

        // when
        var all = engine.getAllInstances();

        // then
        assertThat(all).hasSize(1);
    }
}
