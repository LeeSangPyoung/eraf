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
 * Gateway Certificate Entity
 * Kong의 Certificate와 유사 - TLS/SSL 인증서 관리
 * SNI(Server Name Indication)와 연결하여 도메인별 인증서 매핑
 */
@Entity
@Table(name = "gateway_certificates", indexes = {
        @Index(name = "idx_certificate_name", columnList = "name"),
        @Index(name = "idx_certificate_enabled", columnList = "enabled"),
        @Index(name = "idx_certificate_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 인증서 이름 (식별용, 고유)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * PEM 형식의 인증서 (공개키)
     */
    @Column(name = "cert", columnDefinition = "TEXT", nullable = false)
    private String cert;

    /**
     * PEM 형식의 개인키
     */
    @Column(name = "private_key", columnDefinition = "TEXT", nullable = false)
    private String privateKey;

    /**
     * 인증서 체인 (중간 CA 인증서)
     */
    @Column(name = "cert_chain", columnDefinition = "TEXT")
    private String certChain;

    /**
     * SNI 목록 (Server Name Indication)
     * 이 인증서가 사용될 도메인 목록
     * 예: ["api.example.com", "*.example.com"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> snis = new ArrayList<>();

    /**
     * 인증서 발급자
     */
    @Column(name = "issuer", length = 255)
    private String issuer;

    /**
     * 인증서 주체 (Subject)
     */
    @Column(name = "subject", length = 255)
    private String subject;

    /**
     * 인증서 유효 시작일
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * 인증서 만료일
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 인증서 유형
     * STANDARD, WILDCARD, SAN, SELF_SIGNED
     */
    @Column(name = "cert_type", length = 20)
    @Builder.Default
    private String certType = "STANDARD";

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
     * 인증서 만료 여부 확인
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 인증서 만료 임박 여부 (30일 이내)
     */
    public boolean isExpiringSoon() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().plusDays(30).isAfter(expiresAt) && !isExpired();
    }

    /**
     * Certificate Type Enum
     */
    public enum CertType {
        STANDARD,
        WILDCARD,
        SAN,
        SELF_SIGNED
    }
}
