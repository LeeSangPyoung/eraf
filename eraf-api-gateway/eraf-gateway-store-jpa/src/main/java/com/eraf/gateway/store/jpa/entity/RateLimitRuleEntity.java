package com.eraf.gateway.store.jpa.entity;

import com.eraf.gateway.domain.RateLimitRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Rate Limit Rule JPA Entity
 */
@Entity
@Table(name = "gateway_rate_limit_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRuleEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "path_pattern", length = 200)
    private String pathPattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RateLimitRule.RateLimitType type;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;

    @Column(name = "max_requests", nullable = false)
    private int maxRequests;

    @Column(name = "burst_allowed", nullable = false)
    private boolean burstAllowed = false;

    @Column(name = "burst_max_requests")
    private int burstMaxRequests;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int priority = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Domain 객체로 변환
     */
    public RateLimitRule toDomain() {
        return RateLimitRule.builder()
                .id(id)
                .name(name)
                .pathPattern(pathPattern)
                .type(type)
                .windowSeconds(windowSeconds)
                .maxRequests(maxRequests)
                .burstAllowed(burstAllowed)
                .burstMaxRequests(burstMaxRequests)
                .enabled(enabled)
                .priority(priority)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Domain 객체로부터 Entity 생성
     */
    public static RateLimitRuleEntity fromDomain(RateLimitRule rule) {
        return RateLimitRuleEntity.builder()
                .id(rule.getId())
                .name(rule.getName())
                .pathPattern(rule.getPathPattern())
                .type(rule.getType())
                .windowSeconds(rule.getWindowSeconds())
                .maxRequests(rule.getMaxRequests())
                .burstAllowed(rule.isBurstAllowed())
                .burstMaxRequests(rule.getBurstMaxRequests())
                .enabled(rule.isEnabled())
                .priority(rule.getPriority())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
