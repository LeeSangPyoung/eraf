package com.eraf.saga.recovery;

import com.eraf.saga.ErafSagaProperties;
import com.eraf.saga.core.SagaDefinition;
import com.eraf.saga.core.SagaOrchestrator;
import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.repository.SagaRepository;
import com.eraf.saga.repository.jpa.JpaSagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Saga 복구 서비스
 * 타임아웃되거나 중단된 Saga를 복구합니다.
 */
public class SagaRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(SagaRecoveryService.class);

    private final SagaRepository repository;
    private final SagaOrchestrator orchestrator;
    private final ErafSagaProperties properties;

    public SagaRecoveryService(SagaRepository repository,
                               SagaOrchestrator orchestrator,
                               ErafSagaProperties properties) {
        this.repository = repository;
        this.orchestrator = orchestrator;
        this.properties = properties;
    }

    /**
     * 타임아웃된 Saga 자동 복구 (스케줄러)
     */
    @Scheduled(fixedDelayString = "${eraf.saga.recovery-interval:60000}")
    public void recoverStuckSagas() {
        if (!(repository instanceof JpaSagaRepository)) {
            return; // JPA Repository에서만 지원
        }

        JpaSagaRepository jpaRepository = (JpaSagaRepository) repository;
        long timeoutMillis = properties.getDefaultTimeout().toMillis();

        List<SagaExecution> stuckSagas = jpaRepository.findStuckSagas(timeoutMillis);

        for (SagaExecution execution : stuckSagas) {
            log.warn("Found stuck saga: {} (id={}, status={})",
                    execution.getSagaName(), execution.getId(), execution.getStatus());

            try {
                recover(execution);
            } catch (Exception e) {
                log.error("Failed to recover saga: {} (id={})",
                        execution.getSagaName(), execution.getId(), e);
            }
        }
    }

    /**
     * 특정 Saga 복구
     */
    public SagaExecution recover(String executionId) {
        SagaExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + executionId));
        return recover(execution);
    }

    /**
     * Saga 복구 실행
     */
    public SagaExecution recover(SagaExecution execution) {
        log.info("Recovering saga: {} (id={})", execution.getSagaName(), execution.getId());

        SagaDefinition definition = orchestrator.getDefinition(execution.getSagaName());
        if (definition == null) {
            log.error("Saga definition not found for recovery: {}", execution.getSagaName());
            execution.setStatus(SagaStatus.FAILED);
            execution.setFailureReason("Saga definition not found");
            repository.save(execution);
            return execution;
        }

        // 상태에 따른 복구 전략
        switch (execution.getStatus()) {
            case STARTED:
            case IN_PROGRESS:
                // 보상 트랜잭션 실행
                return compensateAndFail(execution, definition, "Timeout - auto compensated");

            case COMPENSATING:
                // 보상 중이던 것 계속 진행
                return continueCompensation(execution, definition);

            default:
                log.warn("Saga {} is in terminal state: {}", execution.getId(), execution.getStatus());
                return execution;
        }
    }

    /**
     * 강제 보상 후 실패 처리
     */
    private SagaExecution compensateAndFail(SagaExecution execution, SagaDefinition definition, String reason) {
        execution.markFailed(reason);
        repository.save(execution);

        // 보상 로직은 Orchestrator에서 처리하므로 상태만 업데이트
        // 실제 보상은 SagaOrchestrator.compensate() 호출 필요
        log.info("Saga {} marked for compensation due to: {}", execution.getId(), reason);

        return execution;
    }

    /**
     * 보상 계속 진행
     */
    private SagaExecution continueCompensation(SagaExecution execution, SagaDefinition definition) {
        log.info("Continuing compensation for saga: {}", execution.getId());
        // 보상은 이미 진행 중이므로 상태만 반환
        return execution;
    }

    /**
     * 실패한 Saga 재시도
     */
    public SagaExecution retry(String executionId) {
        SagaExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + executionId));

        if (execution.getStatus() != SagaStatus.FAILED &&
            execution.getStatus() != SagaStatus.COMPENSATED) {
            throw new IllegalStateException("Can only retry FAILED or COMPENSATED sagas");
        }

        SagaDefinition definition = orchestrator.getDefinition(execution.getSagaName());
        if (definition == null) {
            throw new IllegalStateException("Saga definition not found: " + execution.getSagaName());
        }

        int maxRetries = definition.getMaxRetries();
        if (execution.getRetryCount() >= maxRetries) {
            throw new IllegalStateException("Max retries exceeded for saga: " + executionId);
        }

        log.info("Retrying saga: {} (id={}, attempt={})",
                execution.getSagaName(), execution.getId(), execution.getRetryCount() + 1);

        // 새로운 실행 생성 (원본 입력 데이터 사용)
        execution.incrementRetryCount();
        execution.setStatus(SagaStatus.STARTED);
        execution.setCurrentStep(0);
        execution.setCompletedAt(null);
        execution.setFailureReason(null);
        repository.save(execution);

        return execution;
    }

    /**
     * 진행 중인 Saga 강제 취소
     */
    public SagaExecution cancel(String executionId) {
        SagaExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + executionId));

        if (execution.getStatus() == SagaStatus.COMPLETED ||
            execution.getStatus() == SagaStatus.COMPENSATED ||
            execution.getStatus() == SagaStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel saga in terminal state: " + execution.getStatus());
        }

        log.info("Cancelling saga: {} (id={})", execution.getSagaName(), execution.getId());

        execution.markFailed("Manually cancelled");
        repository.save(execution);

        return execution;
    }
}
