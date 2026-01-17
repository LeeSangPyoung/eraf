package com.eraf.gateway.iprestriction.repository;

import com.eraf.gateway.iprestriction.domain.IpRestriction;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 IP Restriction Repository 구현체
 */
@Slf4j
public class InMemoryIpRestrictionRepository implements IpRestrictionRepository {

    private final Map<String, IpRestriction> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<IpRestriction> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<IpRestriction> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<IpRestriction> findAllEnabled() {
        return storage.values().stream()
                .filter(IpRestriction::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public List<IpRestriction> findByType(IpRestriction.RestrictionType type) {
        return storage.values().stream()
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<IpRestriction> findEnabledByType(IpRestriction.RestrictionType type) {
        return storage.values().stream()
                .filter(IpRestriction::isEnabled)
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IpRestriction> findByIpAddress(String ipAddress) {
        return storage.values().stream()
                .filter(r -> r.getIpAddress().equals(ipAddress))
                .findFirst();
    }

    @Override
    public IpRestriction save(IpRestriction ipRestriction) {
        String id = ipRestriction.getId();
        if (id == null) {
            id = UUID.randomUUID().toString();
            ipRestriction = IpRestriction.builder()
                    .id(id)
                    .ipAddress(ipRestriction.getIpAddress())
                    .type(ipRestriction.getType())
                    .pathPattern(ipRestriction.getPathPattern())
                    .description(ipRestriction.getDescription())
                    .enabled(ipRestriction.isEnabled())
                    .expiresAt(ipRestriction.getExpiresAt())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        storage.put(id, ipRestriction);
        log.debug("Saved IP restriction: {} - {} ({})", ipRestriction.getIpAddress(), ipRestriction.getType(), id);
        return ipRestriction;
    }

    @Override
    public void deleteById(String id) {
        IpRestriction removed = storage.remove(id);
        if (removed != null) {
            log.debug("Deleted IP restriction: {} (id: {})", removed.getIpAddress(), id);
        }
    }

    @Override
    public boolean existsByIpAddress(String ipAddress) {
        return storage.values().stream()
                .anyMatch(r -> r.getIpAddress().equals(ipAddress));
    }
}
