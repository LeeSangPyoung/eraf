package com.eraf.jpa.multitenancy;

import com.eraf.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * 멀티테넌시 지원 엔티티 기본 클래스
 */
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 50)
    private String tenantId;

    @PrePersist
    protected void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.getTenantId();
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
