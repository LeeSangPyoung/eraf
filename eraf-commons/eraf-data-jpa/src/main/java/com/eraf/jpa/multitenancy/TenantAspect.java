package com.eraf.jpa.multitenancy;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 테넌트 필터 적용 Aspect
 * Hibernate Filter를 사용하여 자동으로 테넌트 조건 추가
 */
@Aspect
public class TenantAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantAspect.class);

    private static final String TENANT_FILTER_NAME = "tenantFilter";

    private final EntityManager entityManager;

    public TenantAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))")
    public Object applyTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        String tenantId = TenantContext.getTenantId();

        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter(TENANT_FILTER_NAME)
                    .setParameter("tenantId", tenantId);
            log.debug("Tenant filter enabled for tenant: {}", tenantId);
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (tenantId != null) {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter(TENANT_FILTER_NAME);
            }
        }
    }
}
