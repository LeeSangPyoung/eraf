package com.eraf.starter.database.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * 감사 로그 JPA Repository
 */
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {

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
}
