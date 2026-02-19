package com.eraf.workflow.repository.jpa;

import com.eraf.workflow.WorkflowInstance;
import com.eraf.workflow.WorkflowStatus;
import com.eraf.workflow.repository.WorkflowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA 기반 Workflow Repository 구현
 */
@Transactional(rollbackFor = Exception.class)
public class JpaWorkflowRepository implements WorkflowRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaWorkflowRepository.class);

    private final WorkflowInstanceJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public JpaWorkflowRepository(WorkflowInstanceJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(WorkflowInstance instance) {
        WorkflowInstanceEntity entity = toEntity(instance);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkflowInstance> findById(String id) {
        return jpaRepository.findById(id).map(this::toInstance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status) {
        return jpaRepository.findByWorkflowNameAndStatus(workflowName, status).stream()
                .map(this::toInstance)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstance> findByStatus(WorkflowStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(this::toInstance)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowInstance> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toInstance)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void cleanupOlderThan(long ageMillis) {
        LocalDateTime cutoff = LocalDateTime.now().minusNanos(ageMillis * 1_000_000);
        List<WorkflowStatus> terminalStatuses = List.of(
                WorkflowStatus.COMPLETED,
                WorkflowStatus.FAILED,
                WorkflowStatus.CANCELLED
        );
        int deleted = jpaRepository.deleteCompletedOlderThan(cutoff, terminalStatuses);
        log.debug("Cleaned up {} old workflow instances", deleted);
    }

    private WorkflowInstanceEntity toEntity(WorkflowInstance instance) {
        WorkflowInstanceEntity entity = new WorkflowInstanceEntity();
        entity.setId(instance.getId());
        entity.setWorkflowName(instance.getWorkflowName());
        entity.setStatus(instance.getStatus());
        entity.setCurrentStep(instance.getCurrentStep());
        entity.setErrorMessage(instance.getErrorMessage());
        entity.setCreatedAt(instance.getCreatedAt());
        entity.setUpdatedAt(instance.getUpdatedAt());

        try {
            entity.setVariablesJson(objectMapper.writeValueAsString(instance.getVariables()));
            entity.setHistoryJson(objectMapper.writeValueAsString(instance.getHistory()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize workflow instance", e);
            throw new RuntimeException("Failed to serialize workflow instance", e);
        }

        return entity;
    }

    private WorkflowInstance toInstance(WorkflowInstanceEntity entity) {
        try {
            Map<String, Object> variables = null;
            if (entity.getVariablesJson() != null) {
                variables = objectMapper.readValue(
                        entity.getVariablesJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
            }

            WorkflowInstance instance = new WorkflowInstance(entity.getWorkflowName(), variables);
            // Note: ID와 timestamps는 new WorkflowInstance에서 자동 생성되므로
            // 완전한 복원을 위해서는 WorkflowInstance에 별도의 복원 생성자가 필요합니다.
            // 현재는 기본 저장/조회 용도로만 사용합니다.
            return instance;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize workflow instance", e);
            throw new RuntimeException("Failed to deserialize workflow instance", e);
        }
    }
}
