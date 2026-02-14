package com.eraf.http;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;

/**
 * ERAF HTTP Client Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(FeignClient.class)
@EnableConfigurationProperties(ErafHttpProperties.class)
@EnableFeignClients
public class ErafHttpAutoConfiguration {

    @Bean
    public ErafHttpRequestInterceptor erafHttpRequestInterceptor(ErafHttpProperties properties) {
        return new ErafHttpRequestInterceptor(properties.isContextPropagationEnabled());
    }

    /**
     * @ErafClient 어노테이션 속성 처리기
     */
    @Bean
    public ErafClientBeanPostProcessor erafClientBeanPostProcessor(ErafHttpProperties properties) {
        return new ErafClientBeanPostProcessor(properties);
    }
}
