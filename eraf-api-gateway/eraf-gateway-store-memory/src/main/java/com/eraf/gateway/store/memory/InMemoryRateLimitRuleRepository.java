package com.eraf.gateway.store.memory;

import com.eraf.gateway.domain.RateLimitRule;
import com.eraf.gateway.repository.RateLimitRuleRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory Rate Limit Rule Repository 구현체
 */
public class InMemoryRateLimitRuleRepository implements RateLimitRuleRepository {

    private final Map<String, RateLimitRule> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<RateLimitRule> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RateLimitRule> findAll() {
        return storage.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findAllEnabled() {
        return storage.values().stream()
                .filter(RateLimitRule::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findByType(RateLimitRule.RateLimitType type) {
        return storage.values().stream()
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findEnabledByType(RateLimitRule.RateLimitType type) {
        return storage.values().stream()
                .filter(r -> r.isEnabled() && r.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findAllEnabledOrderByPriority() {
        return storage.values().stream()
                .filter(RateLimitRule::isEnabled)
                .sorted(Comparator.comparingInt(RateLimitRule::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public RateLimitRule save(RateLimitRule rateLimitRule) {
        String id = rateLimitRule.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        RateLimitRule toSave = RateLimitRule.builder()
                .id(id)
                .name(rateLimitRule.getName())
                .pathPattern(rateLimitRule.getPathPattern())
                .type(rateLimitRule.getType())
                .windowSeconds(rateLimitRule.getWindowSeconds())
                .maxRequests(rateLimitRule.getMaxRequests())
                .burstAllowed(rateLimitRule.isBurstAllowed())
                .burstMaxRequests(rateLimitRule.getBurstMaxRequests())
                .enabled(rateLimitRule.isEnabled())
                .priority(rateLimitRule.getPriority())
                .createdAt(rateLimitRule.getCreatedAt() != null ? rateLimitRule.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.put(id, toSave);
        return toSave;
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}
