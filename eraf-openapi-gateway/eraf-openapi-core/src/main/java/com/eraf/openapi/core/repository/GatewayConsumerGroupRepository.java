package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayConsumerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Consumer Group Repository
 */
@Repository
public interface GatewayConsumerGroupRepository extends JpaRepository<GatewayConsumerGroup, Long> {

    Optional<GatewayConsumerGroup> findByName(String name);

    boolean existsByName(String name);

    List<GatewayConsumerGroup> findByEnabledTrue();

    /**
     * Consumer가 속한 그룹 조회
     */
    @Query("SELECT g FROM GatewayConsumerGroup g JOIN g.consumers c WHERE c.id = :consumerId")
    List<GatewayConsumerGroup> findByConsumerId(@Param("consumerId") Long consumerId);

    long countByEnabledTrue();
}
