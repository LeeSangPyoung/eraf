package com.eraf.jpa.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * 감사 로그 JPA Repository
 */
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

    /**
     * 사용자별 감사 로그 조회 (최신순)
     */
    List<AuditLogEntity> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * 리소스별 감사 로그 조회 (최신순)
     */
    List<AuditLogEntity> findByResourceAndResourceIdOrderByTimestampDesc(
            String resource, String resourceId, Pageable pageable);

    /**
     * 기간별 감사 로그 조회 (최신순)
     */
    @Query("SELECT a FROM AuditLogEntity a WHERE a.timestamp BETWEEN :from AND :to ORDER BY a.timestamp DESC")
    List<AuditLogEntity> findByPeriod(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    /**
     * 액션별 감사 로그 조회 (최신순)
     */
    List<AuditLogEntity> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * 리소스별 감사 로그 조회 (리소스만)
     */
    List<AuditLogEntity> findByResourceOrderByTimestampDesc(String resource, Pageable pageable);

    /**
     * 특정 시간 이전의 삭제되지 않은 로그 조회 (Retention Policy용)
     */
    List<AuditLogEntity> findByTimestampBeforeAndDeletedFalse(Instant timestamp);

    /**
     * 특정 시간 이전에 삭제된 로그 영구 삭제 (Hard Delete)
     */
    int deleteByDeletedTrueAndDeletedAtBefore(Instant deletedAt);

    /**
     * 기간 범위의 삭제되지 않은 로그 조회
     */
    List<AuditLogEntity> findByTimestampBetweenAndDeletedFalse(Instant from, Instant to);

    /**
     * 사용자별 삭제되지 않은 로그 조회
     */
    List<AuditLogEntity> findByUserIdAndDeletedFalse(String userId);

    /**
     * 결과별 로그 개수 조회 (통계용)
     */
    long countByResult(String result);
}
