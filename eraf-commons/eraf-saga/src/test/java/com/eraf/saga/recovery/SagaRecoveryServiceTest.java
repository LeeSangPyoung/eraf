package com.eraf.saga.recovery;

import com.eraf.saga.ErafSagaProperties;
import com.eraf.saga.core.SagaDefinition;
import com.eraf.saga.core.SagaOrchestrator;
import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.repository.InMemorySagaRepository;
import com.eraf.saga.repository.SagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaRecoveryServiceTest {

    @Mock
    private SagaOrchestrator orchestrator;

    private SagaRepository repository;
    private ErafSagaProperties properties;
    private SagaRecoveryService recoveryService;

    @BeforeEach
    void setUp() {
        repository = new InMemorySagaRepository();
        properties = new ErafSagaProperties();
        properties.setDefaultTimeout(Duration.ofMinutes(5));
        recoveryService = new SagaRecoveryService(repository, orchestrator, properties);
    }

    @Test
    @DisplayName("ID로 Saga 복구")
    void testRecoverById() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.IN_PROGRESS);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution recovered = recoveryService.recover(execution.getId());

        // Then
        assertNotNull(recovered);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 복구 시 예외")
    void testRecoverByIdNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                recoveryService.recover("nonexistent-id"));
    }

    @Test
    @DisplayName("정의 없는 Saga 복구 - 실패 처리")
    void testRecoverWithNoDefinition() {
        // Given
        SagaExecution execution = new SagaExecution("unknownSaga");
        execution.setStatus(SagaStatus.IN_PROGRESS);
        repository.save(execution);

        when(orchestrator.getDefinition("unknownSaga")).thenReturn(null);

        // When
        SagaExecution result = recoveryService.recover(execution);

        // Then
        assertEquals(SagaStatus.FAILED, result.getStatus());
        assertEquals("Saga definition not found", result.getFailureReason());
    }

    @Test
    @DisplayName("STARTED 상태 Saga 복구")
    void testRecoverStartedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.STARTED);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.recover(execution);

        // Then
        assertEquals(SagaStatus.COMPENSATING, result.getStatus()); // 보상 진행 중 상태
    }

    @Test
    @DisplayName("IN_PROGRESS 상태 Saga 복구")
    void testRecoverInProgressSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.IN_PROGRESS);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.recover(execution);

        // Then
        assertEquals(SagaStatus.COMPENSATING, result.getStatus());
    }

    @Test
    @DisplayName("COMPENSATING 상태 Saga 복구")
    void testRecoverCompensatingSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.COMPENSATING);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.recover(execution);

        // Then
        assertEquals(SagaStatus.COMPENSATING, result.getStatus()); // 계속 진행
    }

    @Test
    @DisplayName("완료된 Saga는 복구하지 않음")
    void testRecoverCompletedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.COMPLETED);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.recover(execution);

        // Then
        assertEquals(SagaStatus.COMPLETED, result.getStatus()); // 상태 유지
    }

    @Test
    @DisplayName("실패한 Saga 재시도")
    void testRetry() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.FAILED);
        execution.setRetryCount(0);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        definition.setMaxRetries(3);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.retry(execution.getId());

        // Then
        assertEquals(SagaStatus.STARTED, result.getStatus());
        assertEquals(1, result.getRetryCount());
        assertEquals(0, result.getCurrentStep());
        assertNull(result.getCompletedAt());
        assertNull(result.getFailureReason());
    }

    @Test
    @DisplayName("COMPENSATED Saga 재시도")
    void testRetryCompensatedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.COMPENSATED);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        definition.setMaxRetries(3);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When
        SagaExecution result = recoveryService.retry(execution.getId());

        // Then
        assertEquals(SagaStatus.STARTED, result.getStatus());
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과")
    void testRetryMaxRetriesExceeded() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.FAILED);
        execution.setRetryCount(3);
        repository.save(execution);

        SagaDefinition definition = new SagaDefinition("testSaga", Object.class);
        definition.setMaxRetries(3);
        when(orchestrator.getDefinition("testSaga")).thenReturn(definition);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                recoveryService.retry(execution.getId()));
    }

    @Test
    @DisplayName("진행 중 Saga는 재시도 불가")
    void testRetryInProgressSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.IN_PROGRESS);
        repository.save(execution);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                recoveryService.retry(execution.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 Saga 재시도")
    void testRetryNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                recoveryService.retry("nonexistent"));
    }

    @Test
    @DisplayName("Saga 정의 없이 재시도")
    void testRetryNoDefinition() {
        // Given
        SagaExecution execution = new SagaExecution("unknownSaga");
        execution.setStatus(SagaStatus.FAILED);
        repository.save(execution);

        when(orchestrator.getDefinition("unknownSaga")).thenReturn(null);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                recoveryService.retry(execution.getId()));
    }

    @Test
    @DisplayName("진행 중 Saga 취소")
    void testCancel() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.IN_PROGRESS);
        repository.save(execution);

        // When
        SagaExecution result = recoveryService.cancel(execution.getId());

        // Then
        assertEquals(SagaStatus.COMPENSATING, result.getStatus());
        assertEquals("Manually cancelled", result.getFailureReason());
    }

    @Test
    @DisplayName("STARTED 상태 Saga 취소")
    void testCancelStartedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.STARTED);
        repository.save(execution);

        // When
        SagaExecution result = recoveryService.cancel(execution.getId());

        // Then
        assertEquals(SagaStatus.COMPENSATING, result.getStatus());
    }

    @Test
    @DisplayName("완료된 Saga는 취소 불가")
    void testCancelCompletedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.COMPLETED);
        repository.save(execution);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                recoveryService.cancel(execution.getId()));
    }

    @Test
    @DisplayName("이미 실패한 Saga는 취소 불가")
    void testCancelFailedSaga() {
        // Given
        SagaExecution execution = new SagaExecution("testSaga");
        execution.setStatus(SagaStatus.FAILED);
        repository.save(execution);

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                recoveryService.cancel(execution.getId()));
    }

    @Test
    @DisplayName("존재하지 않는 Saga 취소")
    void testCancelNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                recoveryService.cancel("nonexistent"));
    }
}
