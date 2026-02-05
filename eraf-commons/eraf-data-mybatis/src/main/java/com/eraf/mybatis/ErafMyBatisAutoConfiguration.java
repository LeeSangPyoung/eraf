package com.eraf.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF MyBatis Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(SqlSessionFactory.class)
@EnableConfigurationProperties(ErafMyBatisProperties.class)
public class ErafMyBatisAutoConfiguration {

    /**
     * MyBatis Configuration Customizer
     * ERAF 설정 적용
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigurationCustomizer erafMyBatisConfigurationCustomizer(ErafMyBatisProperties properties) {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(properties.isMapUnderscoreToCamelCase());
            configuration.setLazyLoadingEnabled(properties.isLazyLoadingEnabled());
        };
    }
}
