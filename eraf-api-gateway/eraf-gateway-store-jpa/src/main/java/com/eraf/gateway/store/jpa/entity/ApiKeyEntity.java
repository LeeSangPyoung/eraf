package com.eraf.gateway.store.jpa.entity;

import com.eraf.gateway.domain.ApiKey;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * API Key JPA Entity
 */
@Entity
@Table(name = "gateway_api_key")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "api_key", unique = true, nullable = false, length = 128)
    private String apiKey;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gateway_api_key_paths", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "path_pattern")
    private Set<String> allowedPaths = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gateway_api_key_ips", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "ip_address")
    private Set<String> allowedIps = new HashSet<>();

    @Column(name = "rate_limit_per_second")
    private Integer rateLimitPerSecond;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
    public ApiKey toDomain() {
        return ApiKey.builder()
                .id(id)
                .apiKey(apiKey)
                .name(name)
                .description(description)
                .allowedPaths(allowedPaths != null ? new HashSet<>(allowedPaths) : null)
                .allowedIps(allowedIps != null ? new HashSet<>(allowedIps) : null)
                .rateLimitPerSecond(rateLimitPerSecond)
                .enabled(enabled)
                .expiresAt(expiresAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Domain 객체로부터 Entity 생성
     */
    public static ApiKeyEntity fromDomain(ApiKey apiKey) {
        return ApiKeyEntity.builder()
                .id(apiKey.getId())
                .apiKey(apiKey.getApiKey())
                .name(apiKey.getName())
                .description(apiKey.getDescription())
                .allowedPaths(apiKey.getAllowedPaths() != null ? new HashSet<>(apiKey.getAllowedPaths()) : new HashSet<>())
                .allowedIps(apiKey.getAllowedIps() != null ? new HashSet<>(apiKey.getAllowedIps()) : new HashSet<>())
                .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                .enabled(apiKey.isEnabled())
                .expiresAt(apiKey.getExpiresAt())
                .createdAt(apiKey.getCreatedAt())
                .updatedAt(apiKey.getUpdatedAt())
                .build();
    }
}
