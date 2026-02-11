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
 * Gateway Service Entity
 * Kong의 Service와 유사 - 업스트림 서비스 정의
 */
@Entity
@Table(name = "gateway_services", indexes = {
        @Index(name = "idx_service_name", columnList = "name"),
        @Index(name = "idx_service_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Service 이름 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 프로토콜
     */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String protocol = "http";

    /**
     * 호스트
     */
    @Column(nullable = false, length = 255)
    private String host;

    /**
     * 포트
     */
    @Column(nullable = false)
    private Integer port;

    /**
     * Base Path (Service 루트 경로)
     * 예: "/api/v1"
     */
    @Column(name = "base_path", length = 200)
    private String basePath;

    /**
     * 연결 타임아웃 (ms)
     */
    @Column(name = "connect_timeout")
    @Builder.Default
    private Integer connectTimeout = 5000;

    /**
     * 읽기 타임아웃 (ms)
     */
    @Column(name = "read_timeout")
    @Builder.Default
    private Integer readTimeout = 30000;

    /**
     * 쓰기 타임아웃 (ms)
     */
    @Column(name = "write_timeout")
    @Builder.Default
    private Integer writeTimeout = 30000;

    /**
     * 재시도 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retries = 3;

    /**
     * 로드 밸런싱 알고리즘
     */
    @Column(name = "load_balancing_algorithm", length = 50)
    @Builder.Default
    private String loadBalancingAlgorithm = "ROUND_ROBIN";

    /**
     * Health Check 활성화
     */
    @Column(name = "health_check_enabled")
    @Builder.Default
    private Boolean healthCheckEnabled = true;

    /**
     * Health Check Path
     */
    @Column(name = "health_check_path", length = 200)
    private String healthCheckPath;

    /**
     * Health Check 간격 (ms)
     */
    @Column(name = "health_check_interval")
    @Builder.Default
    private Integer healthCheckInterval = 30000;

    /**
     * Health Check 타임아웃 (ms)
     */
    @Column(name = "health_check_timeout")
    @Builder.Default
    private Integer healthCheckTimeout = 5000;

    /**
     * Healthy 임계값
     */
    @Column(name = "healthy_threshold")
    @Builder.Default
    private Integer healthyThreshold = 2;

    /**
     * Unhealthy 임계값
     */
    @Column(name = "unhealthy_threshold")
    @Builder.Default
    private Integer unhealthyThreshold = 3;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 메타데이터 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 연결된 Upstream (선택, 고급 로드밸런싱 정책)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upstream_id")
    private GatewayUpstream upstream;

    /**
     * Targets (업스트림 서버 목록)
     */
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GatewayTarget> targets = new ArrayList<>();

    /**
     * Routes
     */
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GatewayRoute> routes = new ArrayList<>();

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
}
