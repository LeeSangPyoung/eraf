package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gateway Plugin Repository
 */
@Repository
public interface GatewayPluginRepository extends JpaRepository<GatewayPlugin, Long> {

    /**
     * Route ID로 활성화된 플러그인 조회 (우선순위 순)
     */
    @Query("SELECT p FROM GatewayPlugin p " +
           "WHERE p.route.id = :routeId AND p.enabled = true " +
           "ORDER BY p.priority ASC")
    List<GatewayPlugin> findByRouteIdAndEnabledTrueOrderByPriority(Long routeId);

    /**
     * Service ID로 활성화된 플러그인 조회
     */
    @Query("SELECT p FROM GatewayPlugin p " +
           "WHERE p.service.id = :serviceId AND p.enabled = true " +
           "ORDER BY p.priority ASC")
    List<GatewayPlugin> findByServiceIdAndEnabledTrueOrderByPriority(Long serviceId);

    /**
     * Global 플러그인 조회
     */
    @Query("SELECT p FROM GatewayPlugin p " +
           "WHERE p.scope = 'GLOBAL' AND p.enabled = true " +
           "ORDER BY p.priority ASC")
    List<GatewayPlugin> findGlobalPluginsOrderByPriority();

    /**
     * 플러그인 이름으로 조회
     */
    List<GatewayPlugin> findByNameAndEnabledTrue(String name);

    List<GatewayPlugin> findByEnabledTrue();

    long countByEnabledTrue();

    /**
     * 동일 scope+entity 내 플러그인 이름 중복 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM GatewayPlugin p " +
           "WHERE p.name = :name AND p.scope = :scope " +
           "AND (:routeId IS NULL AND p.route IS NULL OR p.route.id = :routeId) " +
           "AND (:serviceId IS NULL AND p.service IS NULL OR p.service.id = :serviceId) " +
           "AND (:consumerGroupId IS NULL AND p.consumerGroup IS NULL OR p.consumerGroup.id = :consumerGroupId)")
    boolean existsDuplicatePlugin(
        @Param("name") String name,
        @Param("scope") String scope,
        @Param("routeId") Long routeId,
        @Param("serviceId") Long serviceId,
        @Param("consumerGroupId") Long consumerGroupId
    );
}
