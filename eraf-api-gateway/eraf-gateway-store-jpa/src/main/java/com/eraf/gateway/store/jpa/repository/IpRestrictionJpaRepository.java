package com.eraf.gateway.store.jpa.repository;

import com.eraf.gateway.domain.IpRestriction;
import com.eraf.gateway.store.jpa.entity.IpRestrictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * IP Restriction Spring Data JPA Repository
 */
@Repository
public interface IpRestrictionJpaRepository extends JpaRepository<IpRestrictionEntity, String> {

    List<IpRestrictionEntity> findByEnabledTrue();

    List<IpRestrictionEntity> findByType(IpRestriction.RestrictionType type);

    List<IpRestrictionEntity> findByEnabledTrueAndType(IpRestriction.RestrictionType type);

    Optional<IpRestrictionEntity> findByIpAddress(String ipAddress);

    boolean existsByIpAddress(String ipAddress);
}
