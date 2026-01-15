package com.eraf.starter.database;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF 데이터베이스 설정
 */
@ConfigurationProperties(prefix = "eraf.database")
public class ErafDatabaseProperties {

    /**
     * Auditing 활성화
     */
    private boolean auditingEnabled = true;

    /**
     * Soft Delete 활성화
     */
    private boolean softDeleteEnabled = true;

    /**
     * 삭제 컬럼 이름
     */
    private String deletedColumn = "deleted";

    /**
     * 삭제 시간 컬럼 이름
     */
    private String deletedAtColumn = "deleted_at";

    public boolean isAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public boolean isSoftDeleteEnabled() {
        return softDeleteEnabled;
    }

    public void setSoftDeleteEnabled(boolean softDeleteEnabled) {
        this.softDeleteEnabled = softDeleteEnabled;
    }

    public String getDeletedColumn() {
        return deletedColumn;
    }

    public void setDeletedColumn(String deletedColumn) {
        this.deletedColumn = deletedColumn;
    }

    public String getDeletedAtColumn() {
        return deletedAtColumn;
    }

    public void setDeletedAtColumn(String deletedAtColumn) {
        this.deletedAtColumn = deletedAtColumn;
    }
}
