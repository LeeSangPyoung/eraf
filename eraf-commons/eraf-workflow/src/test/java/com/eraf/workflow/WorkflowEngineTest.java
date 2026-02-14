package com.eraf.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowEngineTest {

    private WorkflowEngine engine;

    @BeforeEach
    void setUp() {
        engine = new WorkflowEngine();
    }

    // --- 기본 실행 테스트 ---

    @Test
    @DisplayName("Workflow 정상 완료")
    void testStartWorkflow() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("simple-flow")
                .step(WorkflowStep.builder("step-1").build())
                .step(WorkflowStep.builder("step-2").build())
                .build();

        engine.registerHandler("step-1", context -> {
            context.setVariable("processed", true);
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("step-2", context -> {
            assertTrue((Boolean) context.getVariable("processed"));
            return WorkflowStepResult.completed();
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of("input", "test"));

        // Then
        assertNotNull(instance);
        assertNotNull(instance.getId());
        assertEquals("simple-flow", instance.getWorkflowName());
        assertEquals(WorkflowStatus.COMPLETED, instance.getStatus());
        assertNull(instance.getCurrentStep());
        assertEquals(2, instance.getHistory().size());
        assertTrue((Boolean) instance.getVariables().get("processed"));
    }

    @Test
    @DisplayName("모든 Step이 순차적으로 완료되는 Workflow")
    void testCompleteWorkflow() {
        // Given
        StringBuilder executionOrder = new StringBuilder();

        WorkflowDefinition definition = WorkflowDefinition.builder("sequential-flow")
                .step(WorkflowStep.builder("init").build())
                .step(WorkflowStep.builder("process").build())
                .step(WorkflowStep.builder("finalize").build())
                .build();

        engine.registerHandler("init", context -> {
            executionOrder.append("A");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("process", context -> {
            executionOrder.append("B");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("finalize", context -> {
            executionOrder.append("C");
            return WorkflowStepResult.completed();
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.COMPLETED, instance.getStatus());
        assertEquals("ABC", executionOrder.toString());
        assertEquals(3, instance.getHistory().size());
    }

    // --- 실패 테스트 ---

    @Test
    @DisplayName("Step 실패 시 Workflow 실패")
    void testFailedStep() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("failing-flow")
                .step(WorkflowStep.builder("good-step").build())
                .step(WorkflowStep.builder("bad-step").build())
                .step(WorkflowStep.builder("never-reached").build())
                .build();

        engine.registerHandler("good-step", context -> WorkflowStepResult.completed());
        engine.registerHandler("bad-step", context ->
                WorkflowStepResult.failed("Something went wrong"));
        engine.registerHandler("never-reached", context -> WorkflowStepResult.completed());

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.FAILED, instance.getStatus());
        assertEquals("Something went wrong", instance.getErrorMessage());
        assertEquals(2, instance.getHistory().size()); // good-step + bad-step만 실행됨
    }

    @Test
    @DisplayName("핸들러가 예외를 던지면 Workflow 실패")
    void testHandlerException() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("exception-flow")
                .step(WorkflowStep.builder("exploding-step").build())
                .build();

        engine.registerHandler("exploding-step", context -> {
            throw new RuntimeException("Boom!");
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.FAILED, instance.getStatus());
        assertNotNull(instance.getErrorMessage());
        assertTrue(instance.getErrorMessage().contains("Boom!"));
    }

    @Test
    @DisplayName("핸들러 미등록 시 Workflow 실패")
    void testMissingHandler() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("no-handler-flow")
                .step(WorkflowStep.builder("unregistered-step").build())
                .build();

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.FAILED, instance.getStatus());
        assertTrue(instance.getErrorMessage().contains("No handler registered"));
    }

    // --- 취소 테스트 ---

    @Test
    @DisplayName("Workflow 취소")
    void testCancelWorkflow() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("cancel-flow")
                .step(WorkflowStep.builder("wait-step")
                        .type(WorkflowStepType.APPROVAL)
                        .build())
                .build();

        engine.registerHandler("wait-step", context -> WorkflowStepResult.waiting());

        WorkflowInstance instance = engine.startWorkflow(definition, null);
        assertEquals(WorkflowStatus.SUSPENDED, instance.getStatus());

        // When
        WorkflowInstance cancelled = engine.cancelWorkflow(instance.getId());

        // Then
        assertEquals(WorkflowStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("이미 완료된 Workflow 취소 시 예외")
    void testCancelCompletedWorkflow() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("done-flow")
                .step(WorkflowStep.builder("only-step").build())
                .build();

        engine.registerHandler("only-step", context -> WorkflowStepResult.completed());
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertThrows(WorkflowException.class, () -> engine.cancelWorkflow(instance.getId()));
    }

    // --- 승인(SUSPENDED/RESUME) 테스트 ---

    @Test
    @DisplayName("APPROVAL Step에서 SUSPENDED 후 재개")
    void testSuspendAndResume() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("approval-flow")
                .step(WorkflowStep.builder("submit").build())
                .step(WorkflowStep.builder("approve")
                        .type(WorkflowStepType.APPROVAL)
                        .build())
                .step(WorkflowStep.builder("complete").build())
                .build();

        engine.registerHandler("submit", context -> {
            context.setVariable("documentId", "DOC-001");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("approve", context -> WorkflowStepResult.waiting());
        engine.registerHandler("complete", context -> {
            context.setVariable("completedBy", context.getString("approvedBy"));
            return WorkflowStepResult.completed();
        });

        // Step 1: 시작 -> submit 완료 -> approve에서 SUSPENDED
        WorkflowInstance instance = engine.startWorkflow(definition, null);
        assertEquals(WorkflowStatus.SUSPENDED, instance.getStatus());
        assertEquals("approve", instance.getCurrentStep());
        assertEquals("DOC-001", instance.getVariables().get("documentId"));

        // Step 2: 재개 (승인 정보 추가)
        WorkflowInstance resumed = engine.resumeWorkflow(
                instance.getId(), Map.of("approvedBy", "manager1"));

        // Then
        assertEquals(WorkflowStatus.COMPLETED, resumed.getStatus());
        assertEquals("manager1", resumed.getVariables().get("approvedBy"));
        assertEquals("manager1", resumed.getVariables().get("completedBy"));
    }

    @Test
    @DisplayName("SUSPENDED가 아닌 Workflow 재개 시 예외")
    void testResumeNonSuspendedWorkflow() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("test-flow")
                .step(WorkflowStep.builder("s1").build())
                .build();
        engine.registerHandler("s1", context -> WorkflowStepResult.completed());
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertThrows(WorkflowException.class, () -> engine.resumeWorkflow(instance.getId()));
    }

    // --- 조건 분기 테스트 ---

    @Test
    @DisplayName("CONDITIONAL Step에서 분기")
    void testConditionalBranching() {
        // Given - 조건 분기 후 공통 종료 Step으로 합류하는 워크플로우
        WorkflowDefinition definition = WorkflowDefinition.builder("conditional-flow")
                .step(WorkflowStep.builder("check")
                        .type(WorkflowStepType.CONDITIONAL)
                        .build())
                .step(WorkflowStep.builder("approved-path")
                        .nextStep("finish")
                        .build())
                .step(WorkflowStep.builder("rejected-path")
                        .nextStep("finish")
                        .build())
                .step(WorkflowStep.builder("finish").build())
                .build();

        engine.registerHandler("check", context -> {
            int amount = context.getInteger("amount");
            if (amount > 1000) {
                return WorkflowStepResult.completedWithNext("rejected-path");
            }
            return WorkflowStepResult.completedWithNext("approved-path");
        });
        engine.registerHandler("approved-path", context -> {
            context.setVariable("result", "approved");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("rejected-path", context -> {
            context.setVariable("result", "rejected");
            return WorkflowStepResult.completed();
        });
        engine.registerHandler("finish", context -> WorkflowStepResult.completed());

        // When - 금액 500 (승인 경로): check -> approved-path -> finish
        Map<String, Object> vars1 = new HashMap<>();
        vars1.put("amount", 500);
        WorkflowInstance approved = engine.startWorkflow(definition, vars1);
        assertEquals(WorkflowStatus.COMPLETED, approved.getStatus());
        assertEquals("approved", approved.getVariables().get("result"));

        // When - 금액 2000 (거절 경로): check -> rejected-path -> finish
        Map<String, Object> vars2 = new HashMap<>();
        vars2.put("amount", 2000);
        WorkflowInstance rejected = engine.startWorkflow(definition, vars2);
        assertEquals(WorkflowStatus.COMPLETED, rejected.getStatus());
        assertEquals("rejected", rejected.getVariables().get("result"));
    }

    // --- 재시도 테스트 ---

    @Test
    @DisplayName("재시도 가능한 Step에서 재시도 후 성공")
    void testRetryableStep() {
        // Given
        int[] attempts = {0};
        WorkflowDefinition definition = WorkflowDefinition.builder("retry-flow")
                .step(WorkflowStep.builder("flaky-step")
                        .maxRetries(2)
                        .build())
                .build();

        engine.registerHandler("flaky-step", context -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return WorkflowStepResult.completed();
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.COMPLETED, instance.getStatus());
        assertEquals(3, attempts[0]); // 1 initial + 2 retries
    }

    @Test
    @DisplayName("재시도 횟수 초과 시 실패")
    void testRetryExhausted() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("retry-fail-flow")
                .step(WorkflowStep.builder("always-failing")
                        .maxRetries(1)
                        .build())
                .build();

        engine.registerHandler("always-failing", context -> {
            throw new RuntimeException("Always fails");
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertEquals(WorkflowStatus.FAILED, instance.getStatus());
        assertTrue(instance.getErrorMessage().contains("Always fails"));
    }

    // --- 변수 전달 테스트 ---

    @Test
    @DisplayName("Step 간 변수 공유")
    void testVariableSharing() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("var-flow")
                .step(WorkflowStep.builder("producer").build())
                .step(WorkflowStep.builder("consumer").build())
                .build();

        engine.registerHandler("producer", context -> {
            context.setVariable("key1", "value1");
            context.setVariable("key2", 42);
            return WorkflowStepResult.completed(Map.of("outputKey", "outputValue"));
        });
        engine.registerHandler("consumer", context -> {
            assertEquals("value1", context.getString("key1"));
            assertEquals(42, context.getInteger("key2"));
            assertEquals("outputValue", context.getString("outputKey"));
            return WorkflowStepResult.completed();
        });

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, Map.of("initial", "data"));

        // Then
        assertEquals(WorkflowStatus.COMPLETED, instance.getStatus());
        assertEquals("data", instance.getVariables().get("initial"));
        assertEquals("value1", instance.getVariables().get("key1"));
    }

    // --- 상태 조회 테스트 ---

    @Test
    @DisplayName("인스턴스 상태 조회")
    void testGetStatus() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("status-flow")
                .step(WorkflowStep.builder("s1").build())
                .build();
        engine.registerHandler("s1", context -> WorkflowStepResult.completed());
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // When
        WorkflowStatus status = engine.getStatus(instance.getId());

        // Then
        assertEquals(WorkflowStatus.COMPLETED, status);
    }

    @Test
    @DisplayName("존재하지 않는 인스턴스 조회 시 예외")
    void testGetNonExistentInstance() {
        // Then
        assertThrows(WorkflowException.class, () -> engine.getInstance("nonexistent-id"));
    }

    @Test
    @DisplayName("정의 이름으로 Workflow 시작")
    void testStartByDefinitionName() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("named-flow")
                .step(WorkflowStep.builder("s1").build())
                .build();
        engine.registerDefinition(definition);
        engine.registerHandler("s1", context -> WorkflowStepResult.completed());

        // When
        WorkflowInstance instance = engine.startWorkflow("named-flow", null);

        // Then
        assertEquals(WorkflowStatus.COMPLETED, instance.getStatus());
    }

    @Test
    @DisplayName("존재하지 않는 정의로 Workflow 시작 시 예외")
    void testStartByNonExistentDefinitionName() {
        // Then
        assertThrows(WorkflowException.class, () ->
                engine.startWorkflow("nonexistent", null));
    }

    // --- 동시 실행 제한 테스트 ---

    @Test
    @DisplayName("최대 동시 실행 제한")
    void testMaxConcurrentWorkflows() {
        // Given
        WorkflowProperties props = new WorkflowProperties();
        props.setMaxConcurrentWorkflows(1);
        WorkflowEngine limitedEngine = new WorkflowEngine(props);

        WorkflowDefinition definition = WorkflowDefinition.builder("limited-flow")
                .step(WorkflowStep.builder("wait")
                        .type(WorkflowStepType.APPROVAL)
                        .build())
                .build();

        limitedEngine.registerHandler("wait", context -> WorkflowStepResult.waiting());

        // When - 첫 번째는 성공
        WorkflowInstance first = limitedEngine.startWorkflow(definition, null);
        assertEquals(WorkflowStatus.SUSPENDED, first.getStatus());

        // Then - 두 번째는 제한에 걸림
        assertThrows(WorkflowException.class, () ->
                limitedEngine.startWorkflow(definition, null));
    }

    // --- 정의 관리 테스트 ---

    @Test
    @DisplayName("정의 이름 목록 조회")
    void testGetDefinitionNames() {
        // Given
        engine.registerDefinition(WorkflowDefinition.builder("flow-a")
                .step(WorkflowStep.builder("s1").build()).build());
        engine.registerDefinition(WorkflowDefinition.builder("flow-b")
                .step(WorkflowStep.builder("s1").build()).build());

        // When
        var names = engine.getDefinitionNames();

        // Then
        assertEquals(2, names.size());
        assertTrue(names.contains("flow-a"));
        assertTrue(names.contains("flow-b"));
    }

    // --- WorkflowInstance 상태 헬퍼 테스트 ---

    @Test
    @DisplayName("WorkflowInstance isTerminal 확인")
    void testInstanceIsTerminal() {
        // Given
        WorkflowDefinition definition = WorkflowDefinition.builder("terminal-flow")
                .step(WorkflowStep.builder("s1").build())
                .build();
        engine.registerHandler("s1", context -> WorkflowStepResult.completed());

        // When
        WorkflowInstance instance = engine.startWorkflow(definition, null);

        // Then
        assertTrue(instance.isTerminal());
        assertFalse(instance.isExecutable());
    }
}
