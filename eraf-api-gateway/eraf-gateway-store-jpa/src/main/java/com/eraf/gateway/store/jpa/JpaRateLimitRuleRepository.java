package com.eraf.gateway.store.jpa;

import com.eraf.gateway.domain.RateLimitRule;
import com.eraf.gateway.repository.RateLimitRuleRepository;
import com.eraf.gateway.store.jpa.entity.RateLimitRuleEntity;
import com.eraf.gateway.store.jpa.repository.RateLimitRuleJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Rate Limit Rule Repository 어댑터
 */
@RequiredArgsConstructor
public class JpaRateLimitRuleRepository implements RateLimitRuleRepository {

    private final RateLimitRuleJpaRepository jpaRepository;

    @Override
    public Optional<RateLimitRule> findById(String id) {
        return jpaRepository.findById(id)
                .map(RateLimitRuleEntity::toDomain);
    }

    @Override
    public List<RateLimitRule> findAll() {
        return jpaRepository.findAll().stream()
                .map(RateLimitRuleEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(RateLimitRuleEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findByType(RateLimitRule.RateLimitType type) {
        return jpaRepository.findByType(type).stream()
                .map(RateLimitRuleEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findEnabledByType(RateLimitRule.RateLimitType type) {
        return jpaRepository.findByEnabledTrueAndType(type).stream()
                .map(RateLimitRuleEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RateLimitRule> findAllEnabledOrderByPriority() {
        return jpaRepository.findByEnabledTrueOrderByPriorityAsc().stream()
                .map(RateLimitRuleEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public RateLimitRule save(RateLimitRule rateLimitRule) {
        String id = rateLimitRule.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            rateLimitRule = RateLimitRule.builder()
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
                    .createdAt(rateLimitRule.getCreatedAt())
                    .updatedAt(rateLimitRule.getUpdatedAt())
                    .build();
        }

        RateLimitRuleEntity entity = RateLimitRuleEntity.fromDomain(rateLimitRule);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
