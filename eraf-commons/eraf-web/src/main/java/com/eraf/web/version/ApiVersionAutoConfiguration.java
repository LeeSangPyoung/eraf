package com.eraf.web.version;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 버전 관리 Auto Configuration
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "eraf.web.api-version.enabled", havingValue = "true")
@EnableConfigurationProperties(ApiVersionProperties.class)
public class ApiVersionAutoConfiguration implements WebMvcConfigurer {

    private final ApiVersionProperties properties;

    public ApiVersionAutoConfiguration(ApiVersionProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ApiVersionInterceptor apiVersionInterceptor() {
        return new ApiVersionInterceptor(properties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor());
    }
}
