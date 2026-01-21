package com.eraf.starter.database;

import com.eraf.core.code.CodeRepository;
import com.eraf.core.lock.OptimisticRetryAspect;
import com.eraf.core.logging.AuditLogStore;
import com.eraf.core.logging.AuditLogger;
import com.eraf.starter.database.audit.AuditLogJpaRepository;
import com.eraf.starter.database.audit.ErafAuditorAware;
import com.eraf.starter.database.audit.JpaAuditLogStore;
import com.eraf.starter.database.code.CommonCodeJpaRepository;
import com.eraf.starter.database.code.JpaCodeRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * ERAF Database Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(JpaRepository.class)
@EnableConfigurationProperties(ErafDatabaseProperties.class)
@EnableJpaAuditing(auditorAwareRef = "erafAuditorAware")
@EnableJpaRepositories(basePackages = "com.eraf.starter.database")
@EntityScan(basePackages = "com.eraf.starter.database")
public class ErafDatabaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnProperty(name = "eraf.database.auditing-enabled", havingValue = "true", matchIfMissing = true)
    public AuditorAware<String> erafAuditorAware() {
        return new ErafAuditorAware();
    }

    /**
     * JPA 기반 공통코드 저장소
     */
    @Bean
    @ConditionalOnMissingBean(CodeRepository.class)
    @ConditionalOnBean(CommonCodeJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.database.code-repository-enabled", havingValue = "true", matchIfMissing = true)
    public CodeRepository jpaCodeRepository(CommonCodeJpaRepository commonCodeJpaRepository) {
        return new JpaCodeRepository(commonCodeJpaRepository);
    }

    /**
     * JPA 기반 감사 로그 저장소
     */
    @Bean
    @ConditionalOnMissingBean(AuditLogStore.class)
    @ConditionalOnBean(AuditLogJpaRepository.class)
    @ConditionalOnProperty(name = "eraf.database.audit-log-enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(name = "eraf.database.optimistic-retry-enabled", havingValue = "true", matchIfMissing = true)
    public OptimisticRetryAspect optimisticRetryAspect() {
        return new OptimisticRetryAspect();
    }
}
