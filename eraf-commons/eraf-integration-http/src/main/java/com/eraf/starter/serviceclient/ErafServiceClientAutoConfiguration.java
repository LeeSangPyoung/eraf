package com.eraf.starter.serviceclient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Service Client Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(FeignClient.class)
@EnableConfigurationProperties(ErafServiceClientProperties.class)
@EnableFeignClients
public class ErafServiceClientAutoConfiguration {

    @Bean
    public ErafClientRequestInterceptor erafClientRequestInterceptor() {
        return new ErafClientRequestInterceptor();
    }
}
