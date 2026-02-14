package com.eraf.mongo.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Soft Delete를 지원하는 MongoDB Document 기본 클래스
 *
 * BaseIdDocument를 확장하여 ID, 감사 필드와 함께
 * 논리적 삭제(soft delete) 기능을 제공합니다.
 *
 * 사용 예:
 * <pre>
 * {@literal @}Document(collection = "orders")
 * public class OrderDocument extends SoftDeleteDocument {
 *     private String orderNo;
 *     // ...
 * }
 * </pre>
 */
public abstract class SoftDeleteDocument extends BaseIdDocument {

    @Indexed
    @Field("deleted")
    private boolean deleted = false;

    @Field("deleted_at")
    private LocalDateTime deletedAt;

    @Field("deleted_by")
    private String deletedBy;

    /**
     * 논리적 삭제 수행
     *
     * @param deletedBy 삭제 수행자 ID
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
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

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
}
