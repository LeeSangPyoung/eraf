package com.eraf.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ERAF Config 자동 구성
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafConfigProperties.class)
public class ErafConfigAutoConfiguration {

    /**
     * Config Server 자동 구성
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.cloud.config.server.ConfigServerApplication")
    static class ConfigServerConfiguration {
        // Spring Cloud Config Server 활성화 - @EnableConfigServer는 메인 앱에서 선언
    }

    /**
     * 동적 설정 갱신 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicConfigRefreshService dynamicConfigRefreshService(ApplicationContext applicationContext) {
        return new DynamicConfigRefreshService(applicationContext);
    }

    /**
     * Config Refresh Actuator 엔드포인트
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
    public ConfigRefreshEndpoint configRefreshEndpoint(DynamicConfigRefreshService refreshService) {
        return new ConfigRefreshEndpoint(refreshService);
    }
}
