package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway API Repository
 */
@Repository
public interface GatewayApiRepository extends JpaRepository<GatewayApi, Long> {

    /**
     * 이름으로 API 조회
     */
    Optional<GatewayApi> findByName(String name);

    /**
     * 활성화된 모든 API 조회 (우선순위 순)
     */
    List<GatewayApi> findByEnabledTrueOrderByPriorityAsc();

    /**
     * Service ID로 API 목록 조회
     */
    @Query("SELECT a FROM GatewayApi a WHERE a.service.id = :serviceId")
    List<GatewayApi> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Route ID로 API 목록 조회
     */
    @Query("SELECT a FROM GatewayApi a WHERE a.route.id = :routeId")
    List<GatewayApi> findByRouteId(@Param("routeId") Long routeId);

    /**
     * 경로와 메서드로 API 조회 (정확한 매칭)
     */
    @Query("SELECT a FROM GatewayApi a " +
           "WHERE a.path = :path AND a.method = :method AND a.enabled = true")
    Optional<GatewayApi> findByPathAndMethod(
        @Param("path") String path,
        @Param("method") String method
    );

    /**
     * 활성화된 API 중 메서드 매칭 후보 조회
     * 애플리케이션 레벨에서 경로 패턴 매칭 수행
     */
    @Query("SELECT a FROM GatewayApi a " +
           "WHERE a.enabled = true AND a.method = :method " +
           "ORDER BY a.priority ASC")
    List<GatewayApi> findMatchingCandidates(@Param("method") String method);

    /**
     * 태그로 API 검색
     */
    @Query(value = "SELECT * FROM gateway_apis " +
                   "WHERE tags @> CAST(:tag AS jsonb) AND enabled = true",
           nativeQuery = true)
    List<GatewayApi> findByTag(@Param("tag") String tag);

    /**
     * 이름 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 활성화된 API 조회
     */
    List<GatewayApi> findByEnabledTrue();

    /**
     * 활성화된 API 수
     */
    long countByEnabledTrue();

    long countByAuthRequiredTrue();

    long countByValidationEnabledTrue();

    long countByRateLimitEnabledTrue();

    long countByCacheEnabledTrue();

    /**
     * 경로+메서드+서비스 중복 확인 (자신 제외)
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM GatewayApi a " +
           "WHERE a.path = :path AND a.method = :method " +
           "AND a.service.id = :serviceId " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsByPathAndMethodAndService(
        @Param("path") String path,
        @Param("method") String method,
        @Param("serviceId") Long serviceId,
        @Param("excludeId") Long excludeId
    );
}
