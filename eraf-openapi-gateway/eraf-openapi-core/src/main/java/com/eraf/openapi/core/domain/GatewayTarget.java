package com.eraf.openapi.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Gateway Target Entity
 * 로드 밸런싱을 위한 업스트림 서버 인스턴스
 */
@Entity
@Table(name = "gateway_targets", indexes = {
        @Index(name = "idx_target_service", columnList = "service_id"),
        @Index(name = "idx_target_health", columnList = "health_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 Service
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private GatewayService service;

    /**
     * 연결된 Upstream (선택)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upstream_id")
    private GatewayUpstream upstream;

    /**
     * Target 호스트
     */
    @Column(nullable = false, length = 255)
    private String host;

    /**
     * Target 포트
     */
    @Column(nullable = false)
    private Integer port;

    /**
     * 가중치 (로드 밸런싱)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 100;

    /**
     * Health Status
     */
    @Column(name = "health_status", length = 20)
    @Builder.Default
    private String healthStatus = "UNKNOWN";

    /**
     * 마지막 Health Check 시간
     */
    @Column(name = "last_health_check_at")
    private LocalDateTime lastHealthCheckAt;

    /**
     * 연속 실패 횟수
     */
    @Column(name = "consecutive_failures")
    @Builder.Default
    private Integer consecutiveFailures = 0;

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
     * Target URL 반환
     */
    public String getTargetUrl() {
        return String.format("%s:%d", host, port);
    }

    /**
     * Health Status Enum
     */
    public enum HealthStatus {
        HEALTHY,
        UNHEALTHY,
        UNKNOWN
    }
}
