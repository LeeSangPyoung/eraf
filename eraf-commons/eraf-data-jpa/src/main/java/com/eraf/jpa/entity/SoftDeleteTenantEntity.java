package com.eraf.jpa.entity;

import com.eraf.jpa.multitenancy.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.Instant;

/**
 * Soft Delete + Multi-tenancy 통합 엔티티 기본 클래스
 * BaseEntity를 확장하여 생성/수정 감사, 소프트 삭제, 멀티테넌시를 모두 지원
 */
@MappedSuperclass
public abstract class SoftDeleteTenantEntity extends BaseEntity {

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
    private String tenantId;

    @PrePersist
    protected void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantId();
        }
    }

    /**
     * 삭제 여부
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * 소프트 삭제 실행
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    /**
     * 소프트 삭제 실행 (삭제자 지정)
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 삭제 복원
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public String getTenantId() {
        return tenantId;
    }

    protected void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    protected void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    protected void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
