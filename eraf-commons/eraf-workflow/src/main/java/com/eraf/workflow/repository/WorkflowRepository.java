package com.eraf.workflow.repository;

import com.eraf.workflow.WorkflowInstance;
import com.eraf.workflow.WorkflowStatus;

import java.util.List;
import java.util.Optional;

/**
 * Workflow 인스턴스 저장소 인터페이스
 */
public interface WorkflowRepository {

    /**
     * 인스턴스 저장
     */
    void save(WorkflowInstance instance);

    /**
     * ID로 조회
     */
    Optional<WorkflowInstance> findById(String id);

    /**
     * 워크플로우 이름과 상태로 조회
     */
    List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status);

    /**
     * 상태로 조회
     */
    List<WorkflowInstance> findByStatus(WorkflowStatus status);

    /**
     * 모든 인스턴스 조회
     */
    List<WorkflowInstance> findAll();

    /**
     * 삭제
     */
    void delete(String id);

    /**
     * 완료/실패 후 일정 기간 지난 항목 정리
     */
    void cleanupOlderThan(long ageMillis);
}
