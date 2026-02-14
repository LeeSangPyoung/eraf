package com.eraf.mongo.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 멀티테넌시를 지원하는 MongoDB Document 기본 클래스
 * tenantId 필드를 포함하며, MongoTenantFilter와 함께 사용하여
 * 자동 테넌트 필터링을 지원합니다.
 */
public abstract class TenantDocument extends BaseIdDocument {

    @Indexed
    @Field("tenant_id")
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
