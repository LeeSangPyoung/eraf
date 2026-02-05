package com.eraf.saga.repository;

import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.execution.StepExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySagaRepositoryTest {

    private InMemorySagaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySagaRepository();
    }

    @Test
    @DisplayName("Saga 실행 저장 및 조회")
    void testSaveAndFindById() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga", "trace-123");

        // When
        repository.save(execution);
        Optional<SagaExecution> found = repository.findById(execution.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(execution.getId(), found.get().getId());
        assertEquals("testSaga", found.get().getSagaName());
    }

    @Test
    @DisplayName("TraceId로 조회")
    void testFindByTraceId() {
        // Given
        SagaExecution execution1 = new SagaExecution("saga1", "trace-123");
        SagaExecution execution2 = new SagaExecution("saga2", "trace-123");
        SagaExecution execution3 = new SagaExecution("saga3", "trace-456");

        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);

        // When
        List<SagaExecution> found = repository.findByTraceId("trace-123");

        // Then
        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("상태로 조회")
    void testFindByStatus() {
        // Given
        SagaExecution execution1 = new SagaExecution("saga1");
        execution1.setStatus(SagaStatus.COMPLETED);

        SagaExecution execution2 = new SagaExecution("saga2");
        execution2.setStatus(SagaStatus.FAILED);

        SagaExecution execution3 = new SagaExecution("saga3");
        execution3.setStatus(SagaStatus.COMPLETED);

        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);

        // When
        List<SagaExecution> completed = repository.findByStatus(SagaStatus.COMPLETED);
        List<SagaExecution> failed = repository.findByStatus(SagaStatus.FAILED);

        // Then
        assertEquals(2, completed.size());
        assertEquals(1, failed.size());
    }

    @Test
    @DisplayName("Saga 이름과 상태로 조회")
    void testFindBySagaNameAndStatus() {
        // Given
        SagaExecution execution1 = new SagaExecution("orderSaga");
        execution1.setStatus(SagaStatus.COMPLETED);

        SagaExecution execution2 = new SagaExecution("orderSaga");
        execution2.setStatus(SagaStatus.FAILED);

        SagaExecution execution3 = new SagaExecution("paymentSaga");
        execution3.setStatus(SagaStatus.COMPLETED);

        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);

        // When
        List<SagaExecution> found = repository.findBySagaNameAndStatus("orderSaga", SagaStatus.COMPLETED);

        // Then
        assertEquals(1, found.size());
        assertEquals("orderSaga", found.get(0).getSagaName());
    }

    @Test
    @DisplayName("삭제")
    void testDelete() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        repository.save(execution);
        String id = execution.getId();

        // When
        repository.delete(id);

        // Then
        Optional<SagaExecution> found = repository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Step 포함 저장 및 조회")
    void testSaveWithSteps() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.addStep(new StepExecution(1, "step1"));
        execution.addStep(new StepExecution(2, "step2"));

        // When
        repository.save(execution);
        Optional<SagaExecution> found = repository.findById(execution.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getSteps().size());
    }
}
