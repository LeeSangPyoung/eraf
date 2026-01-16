package com.eraf.gateway.store.jpa.entity;

import com.eraf.gateway.domain.IpRestriction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IP Restriction JPA Entity
 */
@Entity
@Table(name = "gateway_ip_restriction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpRestrictionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IpRestriction.RestrictionType type;

    @Column(name = "path_pattern", length = 200)
    private String pathPattern;

    @Column(length = 500)
    private String description;

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
    public IpRestriction toDomain() {
        return IpRestriction.builder()
                .id(id)
                .ipAddress(ipAddress)
                .type(type)
                .pathPattern(pathPattern)
                .description(description)
                .enabled(enabled)
                .expiresAt(expiresAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Domain 객체로부터 Entity 생성
     */
    public static IpRestrictionEntity fromDomain(IpRestriction ipRestriction) {
        return IpRestrictionEntity.builder()
                .id(ipRestriction.getId())
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
}
