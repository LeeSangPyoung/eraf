package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Route Repository
 */
@Repository
public interface GatewayRouteRepository extends JpaRepository<GatewayRoute, Long> {

    /**
     * 이름으로 Route 조회
     */
    Optional<GatewayRoute> findByName(String name);

    /**
     * 활성화된 모든 Route 조회 (우선순위 순)
     */
    List<GatewayRoute> findByEnabledTrueOrderByPriorityAsc();

    /**
     * Service ID로 Route 목록 조회
     */
    @Query("SELECT r FROM GatewayRoute r WHERE r.service.id = :serviceId")
    List<GatewayRoute> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 활성화된 Route 중 경로 매칭
     * 실제 매칭은 애플리케이션 레벨에서 수행
     */
    @Query("SELECT r FROM GatewayRoute r " +
           "WHERE r.enabled = true " +
           "ORDER BY r.priority ASC")
    List<GatewayRoute> findAllEnabledOrderedByPriority();

    /**
     * 활성화된 Route + Service 함께 로드 (Lazy 초기화 문제 방지)
     * DynamicRouteLocator에서 사용
     */
    @Query("SELECT r FROM GatewayRoute r JOIN FETCH r.service " +
           "WHERE r.enabled = true " +
           "ORDER BY r.priority ASC")
    List<GatewayRoute> findEnabledWithServiceOrderByPriority();

    /**
     * 이름 존재 여부
     */
    boolean existsByName(String name);

    long countByEnabledTrue();

    /**
     * API 버전으로 Route 조회
     */
    List<GatewayRoute> findByApiVersion(String apiVersion);

    /**
     * API 버전 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT r.apiVersion FROM GatewayRoute r WHERE r.apiVersion IS NOT NULL ORDER BY r.apiVersion")
    List<String> findDistinctApiVersions();
}
