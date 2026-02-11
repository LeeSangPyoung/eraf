package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayUpstream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Upstream Repository
 */
@Repository
public interface GatewayUpstreamRepository extends JpaRepository<GatewayUpstream, Long> {

    Optional<GatewayUpstream> findByName(String name);

    boolean existsByName(String name);

    List<GatewayUpstream> findByEnabledTrue();

    long countByEnabledTrue();
}
