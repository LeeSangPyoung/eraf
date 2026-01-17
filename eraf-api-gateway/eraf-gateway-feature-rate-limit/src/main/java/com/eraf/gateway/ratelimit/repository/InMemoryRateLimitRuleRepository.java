package com.eraf.gateway.ratelimit.repository;

import com.eraf.gateway.ratelimit.domain.RateLimitRule;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 Rate Limit Rule Repository 구현체
 */
@Slf4j
public class InMemoryRateLimitRuleRepository implements RateLimitRuleRepository {

    private final Map<String, RateLimitRule> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<RateLimitRule> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RateLimitRule> findAll() {
        return new ArrayList<>(storage.values());
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
                .filter(RateLimitRule::isEnabled)
                .filter(r -> r.getType() == type)
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
    public RateLimitRule save(RateLimitRule rule) {
        RateLimitRule savedRule = rule;
        if (rule.getId() == null) {
            savedRule = RateLimitRule.builder()
                    .id(UUID.randomUUID().toString())
                    .name(rule.getName())
                    .pathPattern(rule.getPathPattern())
                    .type(rule.getType())
                    .windowSeconds(rule.getWindowSeconds())
                    .maxRequests(rule.getMaxRequests())
                    .burstAllowed(rule.isBurstAllowed())
                    .burstMaxRequests(rule.getBurstMaxRequests())
                    .enabled(rule.isEnabled())
                    .priority(rule.getPriority())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        storage.put(savedRule.getId(), savedRule);
        log.debug("Saved rate limit rule: {} ({})", savedRule.getName(), savedRule.getId());
        return savedRule;
    }

    @Override
    public void deleteById(String id) {
        RateLimitRule removed = storage.remove(id);
        if (removed != null) {
            log.debug("Deleted rate limit rule: {} (id: {})", removed.getName(), id);
        }
    }
}
