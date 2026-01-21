package com.eraf.starter.jpa;

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
}
