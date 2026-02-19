package com.eraf.workflow.repository.jpa;

import com.eraf.workflow.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Workflow 인스턴스 Spring Data JPA Repository
 */
public interface WorkflowInstanceJpaRepository extends JpaRepository<WorkflowInstanceEntity, String> {

    List<WorkflowInstanceEntity> findByStatus(WorkflowStatus status);

    List<WorkflowInstanceEntity> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status);

    @Modifying
    @Query("DELETE FROM WorkflowInstanceEntity w WHERE w.updatedAt < :cutoff AND w.status IN :statuses")
    int deleteCompletedOlderThan(@Param("cutoff") LocalDateTime cutoff,
                                  @Param("statuses") List<WorkflowStatus> statuses);

    @Query("SELECT w FROM WorkflowInstanceEntity w WHERE w.status IN :statuses AND w.updatedAt < :cutoff")
    List<WorkflowInstanceEntity> findStuckWorkflows(@Param("statuses") List<WorkflowStatus> statuses,
                                                     @Param("cutoff") LocalDateTime cutoff);
}
