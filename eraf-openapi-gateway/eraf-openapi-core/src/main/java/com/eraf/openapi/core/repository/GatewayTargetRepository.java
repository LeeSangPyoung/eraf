package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gateway Target Repository
 */
@Repository
public interface GatewayTargetRepository extends JpaRepository<GatewayTarget, Long> {

    /**
     * Service ID로 활성화된 Target 목록 조회
     */
    @Query("SELECT t FROM GatewayTarget t " +
           "WHERE t.service.id = :serviceId AND t.enabled = true " +
           "AND t.healthStatus = 'HEALTHY'")
    List<GatewayTarget> findHealthyTargetsByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Service ID로 모든 Target 조회
     */
    @Query("SELECT t FROM GatewayTarget t WHERE t.service.id = :serviceId")
    List<GatewayTarget> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 활성화된 모든 Target 조회
     */
    List<GatewayTarget> findByEnabledTrue();

    /**
     * Health Status로 Target 조회
     */
    List<GatewayTarget> findByHealthStatus(String healthStatus);

    long countByEnabledTrue();
}
