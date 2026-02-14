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

    // ===== 고급 기능: 만료 관리 =====

    /**
     * API Key 만료 일시
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 만료 알림 전송 여부
     */
    @Column(name = "expiry_notification_sent")
    @Builder.Default
    private Boolean expiryNotificationSent = false;

    // ===== 고급 기능: IP 화이트리스트 =====

    /**
     * 허용된 IP 범위 목록 (CIDR 표기법)
     * 예: ["192.168.1.0/24", "10.0.0.1"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_ip_ranges", columnDefinition = "jsonb")
    @Builder.Default
    private Set<String> allowedIpRanges = new HashSet<>();

    // ===== 고급 기능: 사용 통계 =====

    /**
     * 마지막 사용 일시
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * 총 사용 횟수
     */
    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * 마지막 사용 IP
     */
    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    // ===== 고급 기능: 스코프/권한 =====

    /**
     * 허용된 스코프 목록
     * 예: ["read:api", "write:api", "admin:*"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scopes", columnDefinition = "jsonb")
    @Builder.Default
    private Set<String> scopes = new HashSet<>();

    // ===== 고급 기능: 키 버전 관리 =====

    /**
     * API Key 버전 (키 회전 추적용)
     */
    @Column(name = "key_version")
    @Builder.Default
    private Integer keyVersion = 1;

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
        this.keyVersion = (this.keyVersion == null ? 1 : this.keyVersion + 1);
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 고급 기능: 헬퍼 메서드 =====

    /**
     * API Key가 만료되었는지 확인
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false; // 만료 기한 없음 (무제한)
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * API Key가 곧 만료되는지 확인 (N일 이내)
     */
    public boolean isExpiringSoon(int days) {
        if (expiresAt == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return expiresAt.isBefore(threshold) && !isExpired();
    }

    /**
     * IP 주소가 화이트리스트에 포함되는지 확인
     */
    public boolean isIpAllowed(String ipAddress) {
        if (allowedIpRanges == null || allowedIpRanges.isEmpty()) {
            return true; // 화이트리스트 없음 (모든 IP 허용)
        }

        for (String range : allowedIpRanges) {
            if (isIpInRange(ipAddress, range)) {
                return true;
            }
        }
        return false;
    }

    /**
     * IP 주소가 특정 CIDR 범위에 포함되는지 확인
     */
    private boolean isIpInRange(String ipAddress, String cidr) {
        if (!cidr.contains("/")) {
            // 단일 IP 주소 비교
            return ipAddress.equals(cidr);
        }

        // CIDR 범위 비교 (간단한 구현)
        String[] parts = cidr.split("/");
        String network = parts[0];
        int prefix = Integer.parseInt(parts[1]);

        // IPv4 간단 비교 (정확한 구현은 Apache Commons IP Math 라이브러리 사용 권장)
        String[] ipParts = ipAddress.split("\\.");
        String[] networkParts = network.split("\\.");

        if (ipParts.length != 4 || networkParts.length != 4) {
            return false;
        }

        int significantOctets = prefix / 8;
        for (int i = 0; i < significantOctets; i++) {
            if (!ipParts[i].equals(networkParts[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * 스코프 권한 확인
     */
    public boolean hasScope(String scope) {
        if (scopes == null || scopes.isEmpty()) {
            return true; // 스코프 제한 없음 (모든 권한)
        }

        // 정확한 매칭
        if (scopes.contains(scope)) {
            return true;
        }

        // 와일드카드 매칭 (예: admin:* matches admin:users)
        for (String allowedScope : scopes) {
            if (allowedScope.endsWith(":*")) {
                String prefix = allowedScope.substring(0, allowedScope.length() - 1);
                if (scope.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * API Key 사용 기록
     */
    public void recordUsage(String ipAddress) {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount = (this.usageCount == null ? 0L : this.usageCount) + 1;
        this.lastUsedIp = ipAddress;
    }

    /**
     * 유효한 API Key인지 확인 (활성화 + 만료 + IP)
     */
    public boolean isValid(String requestIp) {
        if (!enabled) {
            return false; // 비활성화됨
        }

        if (isExpired()) {
            return false; // 만료됨
        }

        if (!isIpAllowed(requestIp)) {
            return false; // IP 화이트리스트 제한
        }

        return true;
    }
}
