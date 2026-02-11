package com.eraf.openapi.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gateway Consumer Entity
 * API를 사용하는 클라이언트 정의 및 API Key 관리
 */
@Entity
@Table(name = "gateway_consumers", indexes = {
        @Index(name = "idx_consumer_username", columnList = "username"),
        @Index(name = "idx_consumer_api_key", columnList = "api_key"),
        @Index(name = "idx_consumer_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayConsumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Consumer 사용자명 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * API Key (UUID 기반, 고유)
     */
    @Column(name = "api_key", nullable = false, unique = true, length = 255)
    private String apiKey;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * Rate Limit - 요청 수 제한
     */
    @Column(name = "rate_limit")
    private Integer rateLimit;

    /**
     * Rate Limit Window (초 단위)
     */
    @Column(name = "rate_limit_window_seconds")
    @Builder.Default
    private Integer rateLimitWindowSeconds = 60;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 태그 (검색 및 분류용)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    /**
     * Custom ID (외부 시스템 연동용)
     */
    @Column(name = "custom_id", length = 100)
    private String customId;

    /**
     * 메타데이터 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 소속 Consumer Groups (역방향 ManyToMany)
     */
    @ManyToMany(mappedBy = "consumers")
    @Builder.Default
    private Set<GatewayConsumerGroup> groups = new HashSet<>();

    /**
     * 생성일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = generateApiKey();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * API Key 생성
     */
    public static String generateApiKey() {
        return "ck_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * API Key 재생성
     */
    public void regenerateApiKey() {
        this.apiKey = generateApiKey();
        this.updatedAt = LocalDateTime.now();
    }
}
