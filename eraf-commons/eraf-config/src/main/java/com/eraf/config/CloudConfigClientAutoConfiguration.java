package com.eraf.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Config Client Auto-Configuration
 *
 * Spring Cloud Config Server와 통합하여 중앙 설정 관리를 지원합니다.
 *
 * Spring Cloud Config는 spring.cloud.config.* properties를 통해 자동 구성됩니다.
 * CloudConfigProperties는 ERAF 특화 설정과 통합 지원을 위한 추가 설정입니다.
 */
@AutoConfiguration
@ConditionalOnClass(ConfigServicePropertySourceLocator.class)
@ConditionalOnProperty(prefix = "eraf.config.cloud", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CloudConfigProperties.class)
public class CloudConfigClientAutoConfiguration {

    /**
     * Config Client 리프레시 지원 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "eraf.config.cloud.refresh", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class RefreshConfiguration {

        @Bean
        public ConfigRefreshListener configRefreshListener() {
            return new ConfigRefreshListener();
        }
    }
}
