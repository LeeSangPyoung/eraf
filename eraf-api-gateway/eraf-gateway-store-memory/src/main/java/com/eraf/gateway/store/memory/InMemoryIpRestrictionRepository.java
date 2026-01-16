package com.eraf.gateway.store.memory;

import com.eraf.gateway.domain.IpRestriction;
import com.eraf.gateway.repository.IpRestrictionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory IP Restriction Repository 구현체
 */
public class InMemoryIpRestrictionRepository implements IpRestrictionRepository {

    private final Map<String, IpRestriction> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<IpRestriction> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<IpRestriction> findAll() {
        return storage.values().stream().collect(Collectors.toList());
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
                .filter(r -> r.isEnabled() && r.getType() == type)
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
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        IpRestriction toSave = IpRestriction.builder()
                .id(id)
                .ipAddress(ipRestriction.getIpAddress())
                .type(ipRestriction.getType())
                .pathPattern(ipRestriction.getPathPattern())
                .description(ipRestriction.getDescription())
                .enabled(ipRestriction.isEnabled())
                .expiresAt(ipRestriction.getExpiresAt())
                .createdAt(ipRestriction.getCreatedAt() != null ? ipRestriction.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.put(id, toSave);
        return toSave;
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByIpAddress(String ipAddress) {
        return storage.values().stream()
                .anyMatch(r -> r.getIpAddress().equals(ipAddress));
    }
}
