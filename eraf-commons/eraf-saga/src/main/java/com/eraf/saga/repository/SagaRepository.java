package com.eraf.saga.repository;

import com.eraf.saga.core.SagaStatus;
import com.eraf.saga.execution.SagaExecution;

import java.util.List;
import java.util.Optional;

/**
 * Saga 실행 상태 저장소 인터페이스
 */
public interface SagaRepository {

    /**
     * Saga 실행 저장
     */
    void save(SagaExecution execution);

    /**
     * ID로 조회
     */
    Optional<SagaExecution> findById(String id);

    /**
     * TraceId로 조회
     */
    List<SagaExecution> findByTraceId(String traceId);

    /**
     * Saga 이름과 상태로 조회
     */
    List<SagaExecution> findBySagaNameAndStatus(String sagaName, SagaStatus status);

    /**
     * 상태로 조회
     */
    List<SagaExecution> findByStatus(SagaStatus status);

    /**
     * 삭제
     */
    void delete(String id);

    /**
     * 완료/실패 후 일정 기간 지난 항목 정리
     */
    void cleanupOlderThan(long ageMillis);
}
