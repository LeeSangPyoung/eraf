package com.eraf.jpa.softdelete;

import com.eraf.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

/**
 * Soft Delete 지원 엔티티 기본 클래스
 */
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

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

    protected void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    protected void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    protected void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
}
