package com.eraf.mongo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF MongoDB 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.mongo")
public class ErafMongoProperties {

    /**
     * MongoDB Auditing 활성화 여부
     */
    private boolean auditingEnabled = true;

    /**
     * 자동 인덱스 생성 여부
     */
    private boolean autoIndexCreation = true;

    public boolean isAuditingEnabled() {
        return auditingEnabled;
    }

    public void setAuditingEnabled(boolean auditingEnabled) {
        this.auditingEnabled = auditingEnabled;
    }

    public boolean isAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void setAutoIndexCreation(boolean autoIndexCreation) {
        this.autoIndexCreation = autoIndexCreation;
    }
}
