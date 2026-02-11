package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Service Repository
 */
@Repository
public interface GatewayServiceRepository extends JpaRepository<GatewayService, Long> {

    /**
     * 이름으로 Service 조회
     */
    Optional<GatewayService> findByName(String name);

    /**
     * 활성화된 Service 목록
     */
    List<GatewayService> findByEnabledTrue();

    /**
     * 이름 존재 여부
     */
    boolean existsByName(String name);

    long countByEnabledTrue();
}
