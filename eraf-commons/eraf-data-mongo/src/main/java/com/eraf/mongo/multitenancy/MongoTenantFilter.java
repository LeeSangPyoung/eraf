package com.eraf.mongo.multitenancy;

import com.eraf.mongo.domain.TenantDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import java.lang.reflect.Method;

/**
 * MongoDB 멀티테넌시 필터
 * TenantDocument를 상속한 Document에 자동으로 tenantId를 설정합니다.
 *
 * tenantId는 다음 순서로 조회됩니다:
 * 1. ErafContext.getTenantId() (eraf-core 모듈)
 * 2. 이미 설정된 tenantId 유지
 */
public class MongoTenantFilter extends AbstractMongoEventListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(MongoTenantFilter.class);
    private static final String ERAF_CONTEXT_CLASS = "com.eraf.core.context.ErafContext";

    private final Method getTenantIdMethod;

    public MongoTenantFilter() {
        this.getTenantIdMethod = resolveGetTenantId();
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof TenantDocument tenantDocument) {
            applyTenantId(tenantDocument);
        }
    }

    private void applyTenantId(TenantDocument document) {
        // tenantId가 이미 설정되어 있으면 유지
        if (document.getTenantId() != null && !document.getTenantId().isEmpty()) {
            return;
        }

        // ErafContext에서 tenantId 조회
        String tenantId = resolveCurrentTenantId();
        if (tenantId != null) {
            document.setTenantId(tenantId);
            log.trace("Applied tenantId '{}' to document {}", tenantId, document.getClass().getSimpleName());
        }
    }

    private String resolveCurrentTenantId() {
        if (getTenantIdMethod != null) {
            try {
                Object result = getTenantIdMethod.invoke(null);
                return result != null ? result.toString() : null;
            } catch (Exception e) {
                log.trace("Failed to resolve tenantId from ErafContext", e);
            }
        }
        return null;
    }

    private static Method resolveGetTenantId() {
        try {
            Class<?> clazz = Class.forName(ERAF_CONTEXT_CLASS);
            return clazz.getMethod("getTenantId");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return null;
        }
    }
}
