package com.eraf.mongo.config;

import com.eraf.mongo.multitenancy.MongoTenantFilter;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.lang.NonNull;

/**
 * ERAF MongoDB 자동 구성
 */
@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
@EnableConfigurationProperties(ErafMongoProperties.class)
public class ErafMongoAutoConfiguration {

    /**
     * MongoMappingContext에 eraf.mongo.auto-index-creation 프로퍼티 적용
     */
    @Bean
    public static BeanPostProcessor mongoAutoIndexBeanPostProcessor(ErafMongoProperties properties) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
                if (bean instanceof MongoMappingContext mappingContext) {
                    mappingContext.setAutoIndexCreation(properties.isAutoIndexCreation());
                }
                return bean;
            }
        };
    }

    /**
     * MongoDB 멀티테넌시 필터
     * TenantDocument를 상속한 Document에 자동으로 tenantId 설정
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.mongo.tenant-filter-enabled", havingValue = "true", matchIfMissing = true)
    public MongoTenantFilter mongoTenantFilter() {
        return new MongoTenantFilter();
    }

    @Import(MongoAuditingConfig.class)
    @ConditionalOnProperty(name = "eraf.mongo.auditing-enabled", havingValue = "true", matchIfMissing = true)
    static class MongoAuditingConfiguration {
    }
}
