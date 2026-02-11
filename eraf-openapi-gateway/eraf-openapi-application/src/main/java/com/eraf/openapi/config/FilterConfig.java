package com.eraf.openapi.config;

import com.eraf.openapi.core.repository.GatewayApiRepository;
import com.eraf.openapi.filter.ApiValidationFilter;
import com.eraf.openapi.filter.JwtAuthFilter;
import com.eraf.openapi.filter.RequestLoggingFilter;
import com.eraf.openapi.validation.ApiValidationService;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Gateway Filters
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class FilterConfig {

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    public ApiValidationFilter apiValidationFilter(
            GatewayApiRepository apiRepository,
            ApiValidationService validationService) {
        return new ApiValidationFilter(apiRepository, validationService);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(
            JwtTokenProvider tokenProvider,
            JwtProperties properties) {
        return new JwtAuthFilter(tokenProvider, properties);
    }
}

