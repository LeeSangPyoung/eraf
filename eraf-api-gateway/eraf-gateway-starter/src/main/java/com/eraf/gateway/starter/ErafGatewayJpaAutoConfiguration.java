package com.eraf.gateway.starter;

import com.eraf.gateway.repository.*;
import com.eraf.gateway.store.jpa.*;
import com.eraf.gateway.store.jpa.repository.*;
import com.eraf.gateway.store.memory.InMemoryRateLimitRecordRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

/**
 * ERAF Gateway JPA 자동 설정
 */
@AutoConfiguration
@ConditionalOnProperty(name = "eraf.gateway.store-type", havingValue = "jpa")
@ConditionalOnClass({DataSource.class})
@ConditionalOnBean(DataSource.class)
@EnableJpaRepositories(basePackages = "com.eraf.gateway.store.jpa.repository")
@EntityScan(basePackages = "com.eraf.gateway.store.jpa.entity")
public class ErafGatewayJpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiKeyRepository.class)
    public ApiKeyRepository apiKeyRepository(ApiKeyJpaRepository jpaRepository) {
        return new JpaApiKeyRepository(jpaRepository);
    }

    @Bean
    @ConditionalOnMissingBean(IpRestrictionRepository.class)
    public IpRestrictionRepository ipRestrictionRepository(IpRestrictionJpaRepository jpaRepository) {
        return new JpaIpRestrictionRepository(jpaRepository);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitRuleRepository.class)
    public RateLimitRuleRepository rateLimitRuleRepository(RateLimitRuleJpaRepository jpaRepository) {
        return new JpaRateLimitRuleRepository(jpaRepository);
    }

    /**
     * Rate Limit Record는 성능을 위해 메모리 저장소 사용 (JPA 모드에서도)
     * Redis가 있으면 Redis 사용 권장
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitRecordRepository.class)
    public RateLimitRecordRepository rateLimitRecordRepository() {
        return new InMemoryRateLimitRecordRepository();
    }
}
