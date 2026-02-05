package com.eraf.jpa;

import com.eraf.core.code.CodeRepository;
import com.eraf.core.lock.OptimisticRetryAspect;
import com.eraf.core.logging.AuditLogStore;
import com.eraf.core.logging.AuditLogger;
import com.eraf.jpa.audit.AuditLogJpaRepository;
import com.eraf.jpa.audit.ErafAuditorAware;
import com.eraf.jpa.audit.JpaAuditLogStore;
import com.eraf.jpa.code.CommonCodeJpaRepository;
import com.eraf.jpa.code.JpaCodeRepository;
import com.eraf.jpa.multitenancy.TenantAspect;
import com.eraf.jpa.multitenancy.TenantFilter;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ERAF JPA Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(JpaRepository.class)
@EnableConfigurationProperties(ErafJpaProperties.class)
@EnableJpaAuditing(auditorAwareRef = "erafAuditorAware")
public class ErafJpaAutoConfiguration {

    /**
     * 감사 정보 제공자 (ErafContext 기반)
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnProperty(name = "eraf.jpa.auditing-enabled", havingValue = "true", matchIfMissing = true)
    public AuditorAware<String> erafAuditorAware() {
        return new ErafAuditorAware();
    }

    /**
     * JPA 기반 공통코드 저장소
     */
    @Bean
    @ConditionalOnMissingBean(CodeRepository.class)
    @ConditionalOnBean(CommonCodeJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.jpa.code-repository-enabled", havingValue = "true", matchIfMissing = true)
    public CodeRepository jpaCodeRepository(CommonCodeJpaRepository commonCodeJpaRepository) {
        return new JpaCodeRepository(commonCodeJpaRepository);
    }

    /**
     * JPA 기반 감사 로그 저장소
     */
    @Bean
    @ConditionalOnMissingBean(AuditLogStore.class)
    @ConditionalOnBean(AuditLogJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.jpa.audit-log-enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogStore jpaAuditLogStore(AuditLogJpaRepository auditLogJpaRepository) {
        return new JpaAuditLogStore(auditLogJpaRepository);
    }

    /**
     * AuditLogger에 AuditLogStore 설정
     */
    @Bean
    @ConditionalOnBean(AuditLogStore.class)
    public AuditLogStoreInitializer auditLogStoreInitializer(AuditLogStore auditLogStore) {
        return new AuditLogStoreInitializer(auditLogStore);
    }

    /**
     * AuditLogger 초기화 헬퍼
     */
    public static class AuditLogStoreInitializer {
        public AuditLogStoreInitializer(AuditLogStore auditLogStore) {
            AuditLogger.setAuditLogStore(auditLogStore);
        }
    }

    /**
     * 낙관적 락 재시도 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.jpa.optimistic-retry-enabled", havingValue = "true", matchIfMissing = true)
    public OptimisticRetryAspect optimisticRetryAspect() {
        return new OptimisticRetryAspect();
    }

    /**
     * Multi-tenancy 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "eraf.jpa.multi-tenancy.enabled", havingValue = "true")
    static class MultiTenancyConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        public TenantFilter tenantFilter(ErafJpaProperties properties) {
            ErafJpaProperties.MultiTenancy config = properties.getMultiTenancy();
            return new TenantFilter(
                    config.getHeaderName(),
                    config.getDefaultTenantId(),
                    config.isRequired()
            );
        }

        @Bean
        @ConditionalOnMissingBean(name = "tenantFilterRegistration")
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter tenantFilter) {
            FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>(tenantFilter);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
            registration.addUrlPatterns("/*");
            registration.setName("tenantFilter");
            return registration;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(EntityManager.class)
        public TenantAspect tenantAspect(EntityManager entityManager) {
            return new TenantAspect(entityManager);
        }
    }
}
