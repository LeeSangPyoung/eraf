package com.eraf.saga.execution;

import com.eraf.saga.core.SagaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SagaExecutionTest {

    private SagaExecution execution;

    @BeforeEach
    void setUp() {
        execution = new SagaExecution("testSaga");
    }

    @Test
    @DisplayName("기본 생성자")
    void testDefaultConstructor() {
        // Given
        SagaExecution defaultExecution = new SagaExecution();

        // Then
        assertNotNull(defaultExecution.getId());
        assertEquals(SagaStatus.STARTED, defaultExecution.getStatus());
        assertEquals(0, defaultExecution.getCurrentStep());
        assertNotNull(defaultExecution.getSteps());
        assertNotNull(defaultExecution.getContext());
        assertNotNull(defaultExecution.getStartedAt());
        assertEquals(0, defaultExecution.getRetryCount());
    }

    @Test
    @DisplayName("Saga 이름으로 생성")
    void testConstructorWithSagaName() {
        // Then
        assertEquals("testSaga", execution.getSagaName());
    }

    @Test
    @DisplayName("Saga 이름과 트레이스 ID로 생성")
    void testConstructorWithSagaNameAndTraceId() {
        // Given
        SagaExecution tracedExecution = new SagaExecution("testSaga", "trace-123");

        // Then
        assertEquals("testSaga", tracedExecution.getSagaName());
        assertEquals("trace-123", tracedExecution.getTraceId());
    }

    @Test
    @DisplayName("ID 고유성")
    void testIdUniqueness() {
        // Given
        SagaExecution execution1 = new SagaExecution();
        SagaExecution execution2 = new SagaExecution();

        // Then
        assertNotEquals(execution1.getId(), execution2.getId());
    }

    @Test
    @DisplayName("Step 추가")
    void testAddStep() {
        // Given
        StepExecution step1 = new StepExecution(1, "step1");
        StepExecution step2 = new StepExecution(2, "step2");

        // When
        execution.addStep(step1);
        execution.addStep(step2);

        // Then
        assertEquals(2, execution.getSteps().size());
    }

    @Test
    @DisplayName("현재 Step 조회")
    void testGetCurrentStepExecution() {
        // Given
        StepExecution step1 = new StepExecution(1, "step1");
        execution.addStep(step1);
        execution.setCurrentStep(1);

        // When
        StepExecution current = execution.getCurrentStepExecution();

        // Then
        assertNotNull(current);
        assertEquals("step1", current.getName());
    }

    @Test
    @DisplayName("현재 Step 조회 - 범위 벗어남")
    void testGetCurrentStepExecutionOutOfRange() {
        // Given
        execution.setCurrentStep(0);

        // When
        StepExecution current = execution.getCurrentStepExecution();

        // Then
        assertNull(current);
    }

    @Test
    @DisplayName("완료 상태 전환")
    void testMarkCompleted() {
        // When
        execution.markCompleted();

        // Then
        assertEquals(SagaStatus.COMPLETED, execution.getStatus());
        assertNotNull(execution.getCompletedAt());
    }

    @Test
    @DisplayName("실패 상태 전환 (보상 시작)")
    void testMarkFailed() {
        // When
        execution.markFailed("Step 2 failed");

        // Then
        assertEquals(SagaStatus.COMPENSATING, execution.getStatus());
        assertEquals("Step 2 failed", execution.getFailureReason());
    }

    @Test
    @DisplayName("보상 완료 상태 전환")
    void testMarkCompensated() {
        // When
        execution.markCompensated();

        // Then
        assertEquals(SagaStatus.COMPENSATED, execution.getStatus());
        assertNotNull(execution.getCompletedAt());
    }

    @Test
    @DisplayName("보상 실패 상태 전환")
    void testMarkCompensationFailed() {
        // When
        execution.markCompensationFailed();

        // Then
        assertEquals(SagaStatus.FAILED, execution.getStatus());
        assertNotNull(execution.getCompletedAt());
    }

    @Test
    @DisplayName("컨텍스트 저장 및 조회")
    void testContextOperations() {
        // When
        execution.putContext("orderId", "ORD-123");
        execution.putContext("amount", 10000);

        // Then
        assertEquals("ORD-123", execution.getContext("orderId"));
        assertEquals(10000, (Integer) execution.getContext("amount"));
    }

    @Test
    @DisplayName("재시도 횟수 증가")
    void testIncrementRetryCount() {
        // Then
        assertEquals(0, execution.getRetryCount());

        execution.incrementRetryCount();
        assertEquals(1, execution.getRetryCount());

        execution.incrementRetryCount();
        assertEquals(2, execution.getRetryCount());
    }

    @Test
    @DisplayName("Setter 동작 확인")
    void testSetters() {
        // When
        execution.setId("custom-id");
        execution.setSagaName("newSaga");
        execution.setTraceId("trace-456");
        execution.setStatus(SagaStatus.COMPLETED);
        execution.setCurrentStep(3);
        execution.setFailureReason("test failure");
        execution.setRetryCount(5);

        // Then
        assertEquals("custom-id", execution.getId());
        assertEquals("newSaga", execution.getSagaName());
        assertEquals("trace-456", execution.getTraceId());
        assertEquals(SagaStatus.COMPLETED, execution.getStatus());
        assertEquals(3, execution.getCurrentStep());
        assertEquals("test failure", execution.getFailureReason());
        assertEquals(5, execution.getRetryCount());
    }

    @Test
    @DisplayName("타임스탬프 설정")
    void testTimestampSetters() {
        // Given
        var now = java.time.Instant.now();

        // When
        execution.setStartedAt(now);
        execution.setCompletedAt(now.plusSeconds(100));

        // Then
        assertEquals(now, execution.getStartedAt());
        assertEquals(now.plusSeconds(100), execution.getCompletedAt());
    }

    @Test
    @DisplayName("Steps 목록 교체")
    void testSetSteps() {
        // Given
        var newSteps = java.util.List.of(
                new StepExecution(1, "newStep1"),
                new StepExecution(2, "newStep2")
        );

        // When
        execution.setSteps(new java.util.ArrayList<>(newSteps));

        // Then
        assertEquals(2, execution.getSteps().size());
    }

    @Test
    @DisplayName("Context 교체")
    void testSetContext() {
        // Given
        var newContext = new java.util.HashMap<String, Object>();
        newContext.put("key1", "value1");

        // When
        execution.setContext(newContext);

        // Then
        assertEquals("value1", execution.getContext("key1"));
    }
}
