package com.eraf.saga.repository.jpa;

import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;
import com.eraf.saga.execution.StepExecution;
import com.eraf.saga.repository.SagaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA 기반 Saga Repository 구현
 */
@Transactional
public class JpaSagaRepository implements SagaRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaSagaRepository.class);

    private final SagaExecutionJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public JpaSagaRepository(SagaExecutionJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(SagaExecution execution) {
        SagaExecutionEntity entity = toEntity(execution);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SagaExecution> findById(String id) {
        return jpaRepository.findById(id).map(this::toExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SagaExecution> findByTraceId(String traceId) {
        return jpaRepository.findByTraceId(traceId).stream()
                .map(this::toExecution)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SagaExecution> findBySagaNameAndStatus(String sagaName, SagaStatus status) {
        return jpaRepository.findBySagaNameAndStatus(sagaName, status).stream()
                .map(this::toExecution)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SagaExecution> findByStatus(SagaStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toExecution)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void cleanupOlderThan(long ageMillis) {
        Instant cutoff = Instant.now().minusMillis(ageMillis);
        List<SagaStatus> completedStatuses = List.of(
                SagaStatus.COMPLETED,
                SagaStatus.COMPENSATED,
                SagaStatus.FAILED
        );
        int deleted = jpaRepository.deleteCompletedOlderThan(cutoff, completedStatuses);
        log.debug("Cleaned up {} old saga executions", deleted);
    }

    /**
     * 타임아웃된 Saga 조회
     */
    @Transactional(readOnly = true)
    public List<SagaExecution> findStuckSagas(long timeoutMillis) {
        Instant cutoff = Instant.now().minusMillis(timeoutMillis);
        List<SagaStatus> activeStatuses = List.of(
                SagaStatus.STARTED,
                SagaStatus.IN_PROGRESS,
                SagaStatus.COMPENSATING
        );
        return jpaRepository.findStuckSagas(activeStatuses, cutoff).stream()
                .map(this::toExecution)
                .collect(Collectors.toList());
    }

    // Entity <-> Execution 변환

    private SagaExecutionEntity toEntity(SagaExecution execution) {
        SagaExecutionEntity entity = new SagaExecutionEntity();
        entity.setId(execution.getId());
        entity.setSagaName(execution.getSagaName());
        entity.setTraceId(execution.getTraceId());
        entity.setStatus(execution.getStatus());
        entity.setCurrentStep(execution.getCurrentStep());
        entity.setStartedAt(execution.getStartedAt());
        entity.setCompletedAt(execution.getCompletedAt());
        entity.setFailureReason(execution.getFailureReason());
        entity.setRetryCount(execution.getRetryCount());

        try {
            entity.setStepsJson(objectMapper.writeValueAsString(execution.getSteps()));
            entity.setContextJson(objectMapper.writeValueAsString(execution.getContext()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize saga execution", e);
            throw new RuntimeException("Failed to serialize saga execution", e);
        }

        return entity;
    }

    private SagaExecution toExecution(SagaExecutionEntity entity) {
        SagaExecution execution = new SagaExecution();
        execution.setId(entity.getId());
        execution.setSagaName(entity.getSagaName());
        execution.setTraceId(entity.getTraceId());
        execution.setStatus(entity.getStatus());
        execution.setCurrentStep(entity.getCurrentStep());
        execution.setStartedAt(entity.getStartedAt());
        execution.setCompletedAt(entity.getCompletedAt());
        execution.setFailureReason(entity.getFailureReason());
        execution.setRetryCount(entity.getRetryCount());

        try {
            if (entity.getStepsJson() != null) {
                List<StepExecution> steps = objectMapper.readValue(
                        entity.getStepsJson(),
                        new TypeReference<List<StepExecution>>() {}
                );
                execution.setSteps(steps);
            }
            if (entity.getContextJson() != null) {
                Map<String, Object> context = objectMapper.readValue(
                        entity.getContextJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
                execution.setContext(context);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize saga execution", e);
            throw new RuntimeException("Failed to deserialize saga execution", e);
        }

        return execution;
    }
}
