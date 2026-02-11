package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayConsumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Consumer Repository
 */
@Repository
public interface GatewayConsumerRepository extends JpaRepository<GatewayConsumer, Long> {

    /**
     * Username으로 Consumer 조회
     */
    Optional<GatewayConsumer> findByUsername(String username);

    /**
     * API Key로 Consumer 조회
     */
    Optional<GatewayConsumer> findByApiKey(String apiKey);

    /**
     * Username 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * API Key 존재 여부
     */
    boolean existsByApiKey(String apiKey);

    /**
     * 활성화된 Consumer 목록 조회
     */
    List<GatewayConsumer> findByEnabledTrue();

    /**
     * Custom ID로 Consumer 조회
     */
    Optional<GatewayConsumer> findByCustomId(String customId);

    long countByEnabledTrue();
}
