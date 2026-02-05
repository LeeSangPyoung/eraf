package com.eraf.saga.core;

import com.eraf.saga.annotation.Saga;
import com.eraf.saga.annotation.SagaStep;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.repository.InMemorySagaRepository;
import com.eraf.saga.repository.SagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SagaOrchestratorTest {

    private SagaOrchestrator orchestrator;
    private SagaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySagaRepository();
        orchestrator = new SagaOrchestrator(repository);
    }

    @Test
    @DisplayName("Saga 정의 등록")
    void testRegisterSaga() {
        // Given
        SagaDefinition definition = new SagaDefinition("testSaga", TestSaga.class);
        definition.setTimeout(60000);
        definition.setMaxRetries(3);

        SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition(1, "step1", null);
        definition.addStep(step);

        // When
        orchestrator.register(definition);

        // Then
        SagaDefinition retrieved = orchestrator.getDefinition("testSaga");
        assertNotNull(retrieved);
        assertEquals("testSaga", retrieved.getName());
        assertEquals(1, retrieved.getTotalSteps());
    }

    @Test
    @DisplayName("Saga 실행 - 성공 케이스")
    void testExecuteSagaSuccess() throws Exception {
        // Given
        TestSaga testSaga = new TestSaga();
        SagaDefinition definition = createTestSagaDefinition(testSaga);
        orchestrator.register(definition);

        // When
        SagaExecution execution = orchestrator.execute("orderSaga", "testInput", "trace-123");

        // Then
        assertNotNull(execution);
        assertEquals("orderSaga", execution.getSagaName());
        assertEquals("trace-123", execution.getTraceId());
        assertEquals(SagaStatus.COMPLETED, execution.getStatus());
        assertTrue(testSaga.step1Executed);
        assertTrue(testSaga.step2Executed);
    }

    @Test
    @DisplayName("Saga 실행 - 실패 및 보상")
    void testExecuteSagaFailureAndCompensation() throws Exception {
        // Given
        FailingSaga failingSaga = new FailingSaga();
        SagaDefinition definition = createFailingSagaDefinition(failingSaga);
        orchestrator.register(definition);

        // When
        SagaExecution execution = orchestrator.execute("failingSaga", "testInput");

        // Then
        assertNotNull(execution);
        assertTrue(execution.getStatus() == SagaStatus.COMPENSATED ||
                   execution.getStatus() == SagaStatus.FAILED);
        assertTrue(failingSaga.step1Executed);
        assertTrue(failingSaga.step2Failed);
    }

    @Test
    @DisplayName("Saga 실행 상태 조회")
    void testGetExecution() {
        // Given
        TestSaga testSaga = new TestSaga();
        SagaDefinition definition = createTestSagaDefinition(testSaga);
        orchestrator.register(definition);

        SagaExecution execution = orchestrator.execute("orderSaga", "testInput");
        String executionId = execution.getId();

        // When
        SagaExecution retrieved = orchestrator.getExecution(executionId);

        // Then
        assertNotNull(retrieved);
        assertEquals(executionId, retrieved.getId());
    }

    private SagaDefinition createTestSagaDefinition(TestSaga saga) throws Exception {
        SagaDefinition definition = new SagaDefinition("orderSaga", TestSaga.class);
        definition.setSagaInstance(saga);
        definition.setTimeout(60000);
        definition.setMaxRetries(1);

        SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition(
                1, "createOrder", TestSaga.class.getDeclaredMethod("createOrder", SagaContext.class));
        definition.addStep(step1);

        SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition(
                2, "processPayment", TestSaga.class.getDeclaredMethod("processPayment", SagaContext.class));
        definition.addStep(step2);

        return definition;
    }

    private SagaDefinition createFailingSagaDefinition(FailingSaga saga) throws Exception {
        SagaDefinition definition = new SagaDefinition("failingSaga", FailingSaga.class);
        definition.setSagaInstance(saga);
        definition.setTimeout(60000);
        definition.setMaxRetries(0);

        SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition(
                1, "step1", FailingSaga.class.getDeclaredMethod("step1", SagaContext.class));
        step1.setCompensateMethod(FailingSaga.class.getDeclaredMethod("compensateStep1", SagaContext.class));
        definition.addStep(step1);

        SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition(
                2, "step2", FailingSaga.class.getDeclaredMethod("step2", SagaContext.class));
        definition.addStep(step2);

        return definition;
    }

    // Test Saga class
    static class TestSaga {
        boolean step1Executed = false;
        boolean step2Executed = false;

        public void createOrder(SagaContext context) {
            step1Executed = true;
            context.put("orderId", "ORDER-123");
        }

        public void processPayment(SagaContext context) {
            step2Executed = true;
        }
    }

    // Failing Saga class
    static class FailingSaga {
        boolean step1Executed = false;
        boolean step2Failed = false;
        boolean step1Compensated = false;

        public void step1(SagaContext context) {
            step1Executed = true;
        }

        public void step2(SagaContext context) {
            step2Failed = true;
            throw new RuntimeException("Step 2 failed!");
        }

        public void compensateStep1(SagaContext context) {
            step1Compensated = true;
        }
    }
}
