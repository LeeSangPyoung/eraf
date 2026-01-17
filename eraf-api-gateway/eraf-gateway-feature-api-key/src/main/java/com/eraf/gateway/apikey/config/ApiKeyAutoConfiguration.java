package com.eraf.gateway.apikey.config;

import com.eraf.gateway.apikey.filter.ApiKeyAuthFilter;
import com.eraf.gateway.apikey.repository.ApiKeyRepository;
import com.eraf.gateway.apikey.repository.InMemoryApiKeyRepository;
import com.eraf.gateway.apikey.service.ApiKeyService;
import com.eraf.gateway.common.filter.FilterOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Key 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(ApiKeyAuthFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.api-key", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ApiKeyProperties.class)
public class ApiKeyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiKeyRepository apiKeyRepository() {
        log.info("Initializing InMemoryApiKeyRepository");
        return new InMemoryApiKeyRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiKeyService apiKeyService(ApiKeyRepository apiKeyRepository) {
        log.info("Initializing ApiKeyService");
        return new ApiKeyService(apiKeyRepository);
    }

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(
            ApiKeyService apiKeyService,
            ApiKeyProperties properties) {
        log.info("Registering ApiKeyAuthFilter with order: {}", FilterOrder.API_KEY_AUTH);

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(
                apiKeyService,
                properties.getHeaderName(),
                properties.isEnabled()
        );
        filter.setExcludePatterns(properties.getExcludePatterns());

        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.API_KEY_AUTH);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
