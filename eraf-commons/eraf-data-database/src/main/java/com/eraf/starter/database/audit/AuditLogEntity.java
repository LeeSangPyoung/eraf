package com.eraf.starter.database.audit;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 감사 로그 JPA 엔티티
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_log_resource", columnList = "resource, resource_id"),
        @Index(name = "idx_audit_log_action", columnList = "action"),
        @Index(name = "idx_audit_log_timestamp", columnList = "timestamp")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource", length = 200)
    private String resource;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "result", length = 20)
    private String result;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

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
}
