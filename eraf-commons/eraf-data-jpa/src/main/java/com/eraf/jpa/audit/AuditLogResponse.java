package com.eraf.jpa.audit;

import java.time.Instant;

/**
 * 감사 로그 응답 DTO
 */
public class AuditLogResponse {

    private Long id;
    private Instant timestamp;
    private String traceId;
    private String userId;
    private String username;
    private String clientIp;
    private String action;
    private String resource;
    private String resourceId;
    private String result;
    private String details;
    private Boolean deleted;
    private Instant deletedAt;
    private String deletedBy;
    private String tenantId;

    public AuditLogResponse() {
    }

    // Constructor from Entity
    public static AuditLogResponse from(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }

        AuditLogResponse response = new AuditLogResponse();
        response.setId(entity.getId());
        response.setTimestamp(entity.getTimestamp());
        response.setTraceId(entity.getTraceId());
        response.setUserId(entity.getUserId());
        response.setUsername(entity.getUsername());
        response.setClientIp(entity.getClientIp());
        response.setAction(entity.getAction());
        response.setResource(entity.getResource());
        response.setResourceId(entity.getResourceId());
        response.setResult(entity.getResult());
        response.setDetails(entity.getDetails());
        response.setDeleted(entity.getDeleted());
        response.setDeletedAt(entity.getDeletedAt());
        response.setDeletedBy(entity.getDeletedBy());
        response.setTenantId(entity.getTenantId());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
