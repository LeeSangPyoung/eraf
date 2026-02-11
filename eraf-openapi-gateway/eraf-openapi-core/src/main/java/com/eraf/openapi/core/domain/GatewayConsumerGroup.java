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

/**
 * Gateway Consumer Group Entity
 * Kong의 Consumer Group과 유사 - Consumer를 그룹화하여 공통 정책 적용
 * Rate Limit, Plugin 등을 그룹 단위로 설정 가능
 */
@Entity
@Table(name = "gateway_consumer_groups", indexes = {
        @Index(name = "idx_consumer_group_name", columnList = "name"),
        @Index(name = "idx_consumer_group_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayConsumerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 그룹 이름 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 그룹 Rate Limit (null이면 제한 없음)
     */
    @Column(name = "rate_limit")
    private Integer rateLimit;

    /**
     * Rate Limit Window (초)
     */
    @Column(name = "rate_limit_window_seconds")
    @Builder.Default
    private Integer rateLimitWindowSeconds = 60;

    /**
     * 그룹에 속한 Consumers (Many-to-Many)
     */
    @ManyToMany
    @JoinTable(
            name = "gateway_consumer_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "consumer_id")
    )
    @Builder.Default
    private Set<GatewayConsumer> consumers = new HashSet<>();

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
     * 메타데이터 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

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
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Consumer 추가
     */
    public void addConsumer(GatewayConsumer consumer) {
        consumers.add(consumer);
    }

    /**
     * Consumer 제거
     */
    public void removeConsumer(GatewayConsumer consumer) {
        consumers.remove(consumer);
    }
}
