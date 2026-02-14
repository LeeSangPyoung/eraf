package com.eraf.jpa.audit;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 감사 로그 JPA 엔티티
 * 로그 테이블이므로 BaseEntity를 상속받지 않음 (생성/수정 감사 불필요)
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_log_resource", columnList = "resource, resource_id"),
        @Index(name = "idx_audit_log_action", columnList = "action"),
        @Index(name = "idx_audit_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_log_trace_id", columnList = "trace_id"),
        @Index(name = "idx_audit_log_deleted", columnList = "deleted"),
        @Index(name = "idx_audit_log_tenant_id", columnList = "tenant_id")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource", nullable = false, length = 255)
    private String resource;

    @Column(name = "resource_id", length = 255)
    private String resourceId;

    @Column(name = "result", nullable = false, length = 50)
    private String result;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    public AuditLogEntity() {
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
