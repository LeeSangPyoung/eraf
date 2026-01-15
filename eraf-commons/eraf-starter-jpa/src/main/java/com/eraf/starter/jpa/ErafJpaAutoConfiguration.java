package com.eraf.starter.jpa;

import com.eraf.starter.jpa.audit.ErafAuditorAware;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
}
