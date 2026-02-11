package com.eraf.openapi.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway Upstream Entity
 * Kong의 Upstream과 유사 - 로드 밸런싱 및 헬스체크 정책 정의
 * Service/Target과 연결하여 고급 트래픽 관리 제공
 */
@Entity
@Table(name = "gateway_upstreams", indexes = {
        @Index(name = "idx_upstream_name", columnList = "name"),
        @Index(name = "idx_upstream_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayUpstream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Upstream 이름 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 로드 밸런싱 알고리즘
     * ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH, CONSISTENT_HASH, WEIGHTED
     */
    @Column(name = "algorithm", nullable = false, length = 30)
    @Builder.Default
    private String algorithm = "ROUND_ROBIN";

    /**
     * Hash On (consistent hash 사용 시)
     * NONE, CONSUMER, IP, HEADER, COOKIE
     */
    @Column(name = "hash_on", length = 20)
    @Builder.Default
    private String hashOn = "NONE";

    /**
     * Hash On Header (hash_on=HEADER 시 사용할 헤더명)
     */
    @Column(name = "hash_on_header", length = 100)
    private String hashOnHeader;

    /**
     * Hash Fallback (해시 실패 시 대체 방식)
     */
    @Column(name = "hash_fallback", length = 20)
    @Builder.Default
    private String hashFallback = "NONE";

    /**
     * Slots (ring-balancer 슬롯 수)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer slots = 10000;

    // ===== Health Check - Active =====

    /**
     * Active Health Check 활성화
     */
    @Column(name = "healthcheck_active_enabled")
    @Builder.Default
    private Boolean healthcheckActiveEnabled = true;

    /**
     * Health Check 경로
     */
    @Column(name = "healthcheck_active_path", length = 200)
    @Builder.Default
    private String healthcheckActivePath = "/";

    /**
     * Health Check 간격 (초)
     */
    @Column(name = "healthcheck_active_interval")
    @Builder.Default
    private Integer healthcheckActiveInterval = 30;

    /**
     * Health Check 타임아웃 (초)
     */
    @Column(name = "healthcheck_active_timeout")
    @Builder.Default
    private Integer healthcheckActiveTimeout = 5;

    /**
     * Healthy 판정 성공 횟수
     */
    @Column(name = "healthcheck_active_healthy_successes")
    @Builder.Default
    private Integer healthcheckActiveHealthySuccesses = 2;

    /**
     * Unhealthy 판정 실패 횟수
     */
    @Column(name = "healthcheck_active_unhealthy_failures")
    @Builder.Default
    private Integer healthcheckActiveUnhealthyFailures = 3;

    /**
     * Healthy로 간주할 HTTP 상태 코드 목록
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "healthcheck_active_healthy_statuses", columnDefinition = "jsonb")
    @Builder.Default
    private List<Integer> healthcheckActiveHealthyStatuses = new ArrayList<>(List.of(200, 302));

    /**
     * Unhealthy로 간주할 HTTP 상태 코드 목록
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "healthcheck_active_unhealthy_statuses", columnDefinition = "jsonb")
    @Builder.Default
    private List<Integer> healthcheckActiveUnhealthyStatuses = new ArrayList<>(List.of(429, 500, 503));

    // ===== Health Check - Passive =====

    /**
     * Passive Health Check 활성화
     */
    @Column(name = "healthcheck_passive_enabled")
    @Builder.Default
    private Boolean healthcheckPassiveEnabled = true;

    /**
     * Passive - Unhealthy 판정 실패 횟수
     */
    @Column(name = "healthcheck_passive_unhealthy_failures")
    @Builder.Default
    private Integer healthcheckPassiveUnhealthyFailures = 5;

    /**
     * Passive - Unhealthy 타임아웃 횟수
     */
    @Column(name = "healthcheck_passive_unhealthy_timeouts")
    @Builder.Default
    private Integer healthcheckPassiveUnhealthyTimeouts = 3;

    /**
     * 연결된 Targets
     */
    @OneToMany(mappedBy = "upstream", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GatewayTarget> targets = new ArrayList<>();

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
     * Load Balancing Algorithm Enum
     */
    public enum Algorithm {
        ROUND_ROBIN,
        LEAST_CONNECTIONS,
        IP_HASH,
        CONSISTENT_HASH,
        WEIGHTED
    }

    /**
     * Hash On Type
     */
    public enum HashOn {
        NONE,
        CONSUMER,
        IP,
        HEADER,
        COOKIE
    }
}
