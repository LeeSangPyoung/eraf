package com.eraf.jpa;

import com.eraf.code.CodeRepository;
import com.eraf.lock.OptimisticRetryAspect;
import com.eraf.core.logging.AuditLogStore;
import com.eraf.core.logging.AuditLogger;
import com.eraf.jpa.audit.*;
import com.eraf.jpa.envers.EntityRevisionService;
import com.eraf.jpa.code.CommonCodeJpaRepository;
import com.eraf.jpa.code.JpaCodeRepository;
import com.eraf.jpa.datasource.DataSourceRoutingAspect;
import com.eraf.jpa.multitenancy.TenantAspect;
import com.eraf.jpa.multitenancy.TenantFilter;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import javax.sql.DataSource;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ERAF JPA Auto Configuration
 */
@AutoConfiguration(afterName = "com.eraf.web.ErafWebAutoConfiguration")
@ConditionalOnClass(JpaRepository.class)
@EnableConfigurationProperties(ErafJpaProperties.class)
@EnableJpaAuditing(auditorAwareRef = "erafAuditorAware")
@EnableAsync
@EnableScheduling
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
     * 비동기 감사 로거
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AuditLogJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.jpa.audit-log-enabled", havingValue = "true", matchIfMissing = true)
    public AsyncAuditLogger asyncAuditLogger(AuditLogJpaRepository auditLogRepository) {
        return new AsyncAuditLogger(auditLogRepository);
    }

    /**
     * 감사 로그 조회 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AuditLogJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.jpa.audit-log-enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogQueryService auditLogQueryService(AuditLogJpaRepository auditLogRepository) {
        return new AuditLogQueryService(auditLogRepository);
    }

    /**
     * 감사 로그 보관 정책
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AuditLogJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.jpa.audit-retention.enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogRetentionPolicy auditLogRetentionPolicy(AuditLogJpaRepository auditLogRepository,
                                                           ErafJpaProperties properties) {
        return new AuditLogRetentionPolicy(auditLogRepository, properties.getAuditRetention());
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
     * Flyway Database Migration
     */
    @Bean(initMethod = "migrate")
    @ConditionalOnClass(Flyway.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.jpa.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource, ErafJpaProperties properties) {
        ErafJpaProperties.Flyway flywayProps = properties.getFlyway();

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayProps.getLocation())
                .baselineVersion(flywayProps.getBaselineVersion())
                .baselineOnMigrate(flywayProps.isBaselineOnMigrate())
                .validateOnMigrate(flywayProps.isValidateOnMigrate())
                .load();
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

    /**
     * DataSource 라우팅 AOP Aspect
     * @DataSourceRouting 어노테이션 기반으로 DataSource를 동적 전환
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "eraf.jpa.datasource-routing.enabled", havingValue = "true")
    static class DataSourceRoutingConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DataSourceRoutingAspect dataSourceRoutingAspect() {
            return new DataSourceRoutingAspect();
        }
    }

    /**
     * Hibernate Envers 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.hibernate.envers.AuditReader")
    @ConditionalOnProperty(name = "eraf.jpa.envers.enabled", havingValue = "true")
    static class EnversConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(EntityManager.class)
        public EntityRevisionService entityRevisionService(EntityManager entityManager) {
            return new EntityRevisionService(entityManager);
        }
    }
}
