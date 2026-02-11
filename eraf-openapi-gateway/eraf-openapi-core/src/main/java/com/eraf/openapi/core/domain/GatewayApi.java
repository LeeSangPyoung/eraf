package com.eraf.openapi.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Gateway API Entity
 * 구체적인 API 엔드포인트 등록 및 관리
 * Routes는 경로 패턴 기반 라우팅, APIs는 구체적인 엔드포인트 검증
 */
@Entity
@Table(name = "gateway_apis",
    indexes = {
        @Index(name = "idx_api_name", columnList = "name"),
        @Index(name = "idx_api_path", columnList = "path"),
        @Index(name = "idx_api_method", columnList = "method"),
        @Index(name = "idx_api_path_method", columnList = "path, method"),
        @Index(name = "idx_api_service", columnList = "service_id"),
        @Index(name = "idx_api_route", columnList = "route_id"),
        @Index(name = "idx_api_enabled", columnList = "enabled"),
        @Index(name = "idx_api_priority", columnList = "priority")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_api_path_method_service",
            columnNames = {"path", "method", "service_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API 이름 (고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * API 경로
     * 예: /api/users, /api/users/*, /api/users/**
     */
    @Column(nullable = false, length = 500)
    private String path;

    /**
     * HTTP 메서드
     */
    @Column(nullable = false, length = 10)
    private String method;

    /**
     * 연결된 Service (필수)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private GatewayService service;

    /**
     * 연결된 Route (선택)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private GatewayRoute route;

    /**
     * 인증 필요 여부
     */
    @Column(name = "auth_required", nullable = false)
    @Builder.Default
    private Boolean authRequired = true;

    /**
     * 인증 타입 (JWT, API_KEY, OAUTH2, BASIC, NONE)
     */
    @Column(name = "auth_type", length = 50)
    private String authType;

    /**
     * 필수 Role 목록 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_roles", columnDefinition = "jsonb")
    private List<String> requiredRoles;

    /**
     * Rate Limit 활성화
     */
    @Column(name = "rate_limit_enabled")
    @Builder.Default
    private Boolean rateLimitEnabled = false;

    /**
     * Rate Limit 요청 수
     */
    @Column(name = "rate_limit_requests")
    private Integer rateLimitRequests;

    /**
     * Rate Limit 윈도우 (초)
     */
    @Column(name = "rate_limit_window_seconds")
    private Integer rateLimitWindowSeconds;

    /**
     * Rate Limit Key (ip, user, api_key, etc.)
     */
    @Column(name = "rate_limit_key", length = 50)
    private String rateLimitKey;

    /**
     * Validation 활성화
     */
    @Column(name = "validation_enabled", nullable = false)
    @Builder.Default
    private Boolean validationEnabled = true;

    /**
     * 헤더 검증 활성화
     */
    @Column(name = "validate_headers")
    @Builder.Default
    private Boolean validateHeaders = true;

    /**
     * 필수 헤더 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_headers", columnDefinition = "jsonb")
    private Map<String, String> requiredHeaders;

    /**
     * 쿼리 파라미터 검증 활성화
     */
    @Column(name = "validate_query_params")
    @Builder.Default
    private Boolean validateQueryParams = true;

    /**
     * 필수 쿼리 파라미터 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_query_params", columnDefinition = "jsonb")
    private List<String> requiredQueryParams;

    /**
     * 바디 검증 활성화
     */
    @Column(name = "validate_body")
    @Builder.Default
    private Boolean validateBody = true;

    /**
     * JSON Schema (바디 검증용)
     */
    @Column(name = "json_schema", columnDefinition = "TEXT")
    private String jsonSchema;

    /**
     * OpenAPI Operation ID
     */
    @Column(name = "openapi_operation_id", length = 200)
    private String openApiOperationId;

    /**
     * 최대 바디 크기 (bytes)
     */
    @Column(name = "max_body_size")
    private Long maxBodySize;

    /**
     * 허용 Content-Type (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_content_types", columnDefinition = "jsonb")
    private List<String> allowedContentTypes;

    /**
     * 필수 필드 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_fields", columnDefinition = "jsonb")
    private List<String> requiredFields;

    /**
     * Response 검증 활성화
     */
    @Column(name = "validate_response")
    @Builder.Default
    private Boolean validateResponse = false;

    /**
     * Response JSON Schema
     */
    @Column(name = "response_schema", columnDefinition = "TEXT")
    private String responseSchema;

    /**
     * IP 제한 활성화
     */
    @Column(name = "ip_restriction_enabled")
    @Builder.Default
    private Boolean ipRestrictionEnabled = false;

    /**
     * 허용 IP 목록 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_ips", columnDefinition = "jsonb")
    private List<String> allowedIps;

    /**
     * 차단 IP 목록 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocked_ips", columnDefinition = "jsonb")
    private List<String> blockedIps;

    /**
     * Cache 활성화
     */
    @Column(name = "cache_enabled")
    @Builder.Default
    private Boolean cacheEnabled = false;

    /**
     * Cache TTL (초)
     */
    @Column(name = "cache_ttl_seconds")
    private Integer cacheTtlSeconds;

    /**
     * Cache Key Pattern
     */
    @Column(name = "cache_key", length = 200)
    private String cacheKey;

    /**
     * Timeout (ms)
     */
    @Column(name = "timeout_ms")
    private Long timeoutMs;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 우선순위 (낮을수록 먼저 매칭)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    /**
     * 메타데이터 (JSON)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 태그 (JSON 배열)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

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
     * 경로 매칭 여부 확인 (Ant-style pattern)
     *
     * @param requestPath 요청 경로
     * @return 매칭 여부
     */
    public boolean matchesPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }

        // Exact match
        if (this.path.equals(requestPath)) {
            return true;
        }

        // Single wildcard: /api/users/* matches /api/users/123
        if (this.path.endsWith("/*")) {
            String prefix = this.path.substring(0, this.path.length() - 2);
            if (requestPath.startsWith(prefix + "/")) {
                String remaining = requestPath.substring(prefix.length() + 1);
                return !remaining.contains("/");
            }
        }

        // Double wildcard: /api/users/** matches /api/users/123/orders/456
        if (this.path.endsWith("/**")) {
            String prefix = this.path.substring(0, this.path.length() - 3);
            return requestPath.startsWith(prefix + "/") || requestPath.equals(prefix);
        }

        return false;
    }

    /**
     * 메서드 매칭 여부 확인
     *
     * @param requestMethod 요청 메서드
     * @return 매칭 여부
     */
    public boolean matchesMethod(String requestMethod) {
        return requestMethod != null && this.method.equalsIgnoreCase(requestMethod);
    }

    /**
     * 전체 매칭 여부 확인
     *
     * @param requestPath   요청 경로
     * @param requestMethod 요청 메서드
     * @return 매칭 여부
     */
    public boolean matches(String requestPath, String requestMethod) {
        return matchesPath(requestPath) && matchesMethod(requestMethod);
    }
}
