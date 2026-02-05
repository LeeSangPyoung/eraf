package com.eraf.saga.repository.jpa;

import com.eraf.saga.core.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Saga 실행 JPA Repository
 */
public interface SagaExecutionJpaRepository extends JpaRepository<SagaExecutionEntity, String> {

    List<SagaExecutionEntity> findByTraceId(String traceId);

    List<SagaExecutionEntity> findBySagaNameAndStatus(String sagaName, SagaStatus status);

    List<SagaExecutionEntity> findByStatus(SagaStatus status);

    /**
     * 실행 중인 상태로 오래된 Saga 조회 (타임아웃 처리용)
     */
    @Query("SELECT s FROM SagaExecutionEntity s WHERE s.status IN :statuses AND s.startedAt < :cutoff")
    List<SagaExecutionEntity> findStuckSagas(
            @Param("statuses") List<SagaStatus> statuses,
            @Param("cutoff") Instant cutoff
    );

    /**
     * 완료된 오래된 Saga 삭제
     */
    @Modifying
    @Query("DELETE FROM SagaExecutionEntity s WHERE s.completedAt < :cutoff AND s.status IN :statuses")
    int deleteCompletedOlderThan(
            @Param("cutoff") Instant cutoff,
            @Param("statuses") List<SagaStatus> statuses
    );
}
