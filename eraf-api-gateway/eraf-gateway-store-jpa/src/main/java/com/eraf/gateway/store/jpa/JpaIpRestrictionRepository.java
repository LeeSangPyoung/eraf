package com.eraf.gateway.store.jpa;

import com.eraf.gateway.domain.IpRestriction;
import com.eraf.gateway.repository.IpRestrictionRepository;
import com.eraf.gateway.store.jpa.entity.IpRestrictionEntity;
import com.eraf.gateway.store.jpa.repository.IpRestrictionJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA IP Restriction Repository 어댑터
 */
@RequiredArgsConstructor
public class JpaIpRestrictionRepository implements IpRestrictionRepository {

    private final IpRestrictionJpaRepository jpaRepository;

    @Override
    public Optional<IpRestriction> findById(String id) {
        return jpaRepository.findById(id)
                .map(IpRestrictionEntity::toDomain);
    }

    @Override
    public List<IpRestriction> findAll() {
        return jpaRepository.findAll().stream()
                .map(IpRestrictionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IpRestriction> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(IpRestrictionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IpRestriction> findByType(IpRestriction.RestrictionType type) {
        return jpaRepository.findByType(type).stream()
                .map(IpRestrictionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IpRestriction> findEnabledByType(IpRestriction.RestrictionType type) {
        return jpaRepository.findByEnabledTrueAndType(type).stream()
                .map(IpRestrictionEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IpRestriction> findByIpAddress(String ipAddress) {
        return jpaRepository.findByIpAddress(ipAddress)
                .map(IpRestrictionEntity::toDomain);
    }

    @Override
    public IpRestriction save(IpRestriction ipRestriction) {
        String id = ipRestriction.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            ipRestriction = IpRestriction.builder()
                    .id(id)
                    .ipAddress(ipRestriction.getIpAddress())
                    .type(ipRestriction.getType())
                    .pathPattern(ipRestriction.getPathPattern())
                    .description(ipRestriction.getDescription())
                    .enabled(ipRestriction.isEnabled())
                    .expiresAt(ipRestriction.getExpiresAt())
                    .createdAt(ipRestriction.getCreatedAt())
                    .updatedAt(ipRestriction.getUpdatedAt())
                    .build();
        }

        IpRestrictionEntity entity = IpRestrictionEntity.fromDomain(ipRestriction);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIpAddress(String ipAddress) {
        return jpaRepository.existsByIpAddress(ipAddress);
    }
}
