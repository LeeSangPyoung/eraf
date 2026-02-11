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
 * Gateway Route Entity
 * Kong의 Route와 유사한 개념
 */
@Entity
@Table(name = "gateway_routes", indexes = {
        @Index(name = "idx_route_name", columnList = "name"),
        @Index(name = "idx_route_service", columnList = "service_id"),
        @Index(name = "idx_route_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Route 이름 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 매칭 경로 패턴 (JSON 배열)
     * 예: ["/api/**", "/v1/users/**"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> paths = new ArrayList<>();

    /**
     * HTTP 메서드 (JSON 배열)
     * 예: ["GET", "POST"]
     * null이면 모든 메서드 허용
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> methods;

    /**
     * 호스트 매칭 (JSON 배열)
     * 예: ["api.example.com", "*.example.com"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> hosts;

    /**
     * 헤더 매칭 조건 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> headers;

    /**
     * 연결된 Service
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private GatewayService service;

    /**
     * 우선순위 (낮을수록 우선)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    /**
     * Path Strip 여부
     * true면 첫 번째 경로 세그먼트 제거
     * 예: "/api/users" → "/users"
     */
    @Column(name = "strip_path")
    @Builder.Default
    private Boolean stripPath = true;

    /**
     * Path Prefix 추가
     * 예: "/backend" 추가시 "/users" → "/backend/users"
     */
    @Column(name = "path_prefix", length = 200)
    private String pathPrefix;

    /**
     * API 버전
     * 예: "v1", "v2", "2024-01-15"
     * 버전별 라우팅 관리 및 필터링용
     */
    @Column(name = "api_version", length = 50)
    private String apiVersion;

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
