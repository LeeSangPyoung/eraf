package com.eraf.openapi.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Plugin Entity
 * Route 또는 Service에 적용되는 플러그인 설정
 */
@Entity
@Table(name = "gateway_plugins", indexes = {
        @Index(name = "idx_plugin_name", columnList = "name"),
        @Index(name = "idx_plugin_route", columnList = "route_id"),
        @Index(name = "idx_plugin_service", columnList = "service_id"),
        @Index(name = "idx_plugin_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPlugin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 플러그인 이름
     * 예: "rate-limit", "jwt-auth", "api-key", "cors", "cache"
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 적용 범위: GLOBAL, SERVICE, ROUTE
     */
    @Column(name = "scope", nullable = false, length = 20)
    @Builder.Default
    private String scope = "ROUTE";

    /**
     * 연결된 Route (scope=ROUTE일 때)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private GatewayRoute route;

    /**
     * 연결된 Service (scope=SERVICE일 때)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private GatewayService service;

    /**
     * 연결된 Consumer Group (scope=CONSUMER_GROUP일 때)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_group_id")
    private GatewayConsumerGroup consumerGroup;

    /**
     * 플러그인 설정 (JSON)
     * 예:
     * {
     *   "rate_limit": 100,
     *   "window_seconds": 60
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> config = new HashMap<>();

    /**
     * 우선순위 (낮을수록 먼저 실행)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

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
     * Plugin Scope Enum
     */
    public enum PluginScope {
        GLOBAL,           // 모든 요청에 적용
        SERVICE,          // 특정 서비스에 적용
        ROUTE,            // 특정 라우트에 적용
        CONSUMER_GROUP    // 특정 Consumer Group에 적용
    }

    /**
     * 사용 가능한 플러그인 목록
     */
    public enum AvailablePlugins {
        RATE_LIMIT("rate-limit"),
        RATE_LIMIT_ADVANCED("rate-limit-advanced"),
        API_KEY("api-key"),
        JWT_AUTH("jwt-auth"),
        OAUTH2("oauth2"),
        IP_RESTRICTION("ip-restriction"),
        CIRCUIT_BREAKER("circuit-breaker"),
        RESPONSE_CACHE("response-cache"),
        BOT_DETECTION("bot-detection"),
        CORS("cors"),
        REQUEST_TRANSFORMER("request-transformer"),
        RESPONSE_TRANSFORMER("response-transformer"),
        ANALYTICS("analytics"),
        RETRY("retry"),
        TIMEOUT("timeout");

        private final String pluginName;

        AvailablePlugins(String pluginName) {
            this.pluginName = pluginName;
        }

        public String getPluginName() {
            return pluginName;
        }
    }
}
