package com.eraf.jpa;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF JPA Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.jpa")
public class ErafJpaProperties {

    /**
     * Enable JPA auditing
     */
    private boolean auditingEnabled = true;

    /**
     * Enable open-in-view
     */
    private boolean openInView = false;

    /**
     * Show SQL in logs
     */
    private boolean showSql = false;

    /**
     * Format SQL in logs
     */
    private boolean formatSql = true;

    /**
     * Enable code repository
     */
    private boolean codeRepositoryEnabled = true;

    /**
     * Enable audit log
     */
    private boolean auditLogEnabled = true;

    /**
     * Enable optimistic retry aspect
     */
    private boolean optimisticRetryEnabled = true;

    /**
     * Multi-tenancy 설정
     */
    private MultiTenancy multiTenancy = new MultiTenancy();

    public boolean isAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public boolean isOpenInView() {
        return openInView;
    }

    public void setOpenInView(boolean openInView) {
        this.openInView = openInView;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public boolean isFormatSql() {
        return formatSql;
    }

    public void setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
    }

    public boolean isCodeRepositoryEnabled() {
        return codeRepositoryEnabled;
    }

    public void setCodeRepositoryEnabled(boolean codeRepositoryEnabled) {
        this.codeRepositoryEnabled = codeRepositoryEnabled;
    }

    public boolean isAuditLogEnabled() {
        return auditLogEnabled;
    }

    public void setAuditLogEnabled(boolean auditLogEnabled) {
        this.auditLogEnabled = auditLogEnabled;
    }

    public boolean isOptimisticRetryEnabled() {
        return optimisticRetryEnabled;
    }

    public void setOptimisticRetryEnabled(boolean optimisticRetryEnabled) {
        this.optimisticRetryEnabled = optimisticRetryEnabled;
    }

    public MultiTenancy getMultiTenancy() {
        return multiTenancy;
    }

    public void setMultiTenancy(MultiTenancy multiTenancy) {
        this.multiTenancy = multiTenancy;
    }

    /**
     * Multi-tenancy 설정
     */
    public static class MultiTenancy {

        /**
         * Multi-tenancy 활성화
         */
        private boolean enabled = false;

        /**
         * 테넌트 ID 헤더 이름
         */
        private String headerName = "X-Tenant-ID";

        /**
         * 기본 테넌트 ID
         */
        private String defaultTenantId;

        /**
         * 테넌트 ID 필수 여부
         */
        private boolean required = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getDefaultTenantId() {
            return defaultTenantId;
        }

        public void setDefaultTenantId(String defaultTenantId) {
            this.defaultTenantId = defaultTenantId;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
