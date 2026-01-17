package com.eraf.gateway.jwt.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.jwt.DefaultJwtValidator;
import com.eraf.gateway.jwt.JwtValidationFilter;
import com.eraf.gateway.jwt.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * JWT 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(JwtValidationFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(JwtProperties properties) {
        Assert.hasText(properties.getSecretKey(), "JWT secret key must not be empty");
        log.info("Initializing JwtValidator with secret key");
        return new DefaultJwtValidator(properties.getSecretKey());
    }

    @Bean
    public FilterRegistrationBean<JwtValidationFilter> jwtValidationFilterRegistration(
            JwtValidator jwtValidator,
            JwtProperties properties) {
        log.info("Registering JwtValidationFilter with order: {}", FilterOrder.JWT_VALIDATION);

        JwtValidationFilter filter = new JwtValidationFilter(
                jwtValidator,
                properties.getExcludePatterns(),
                properties.getHeaderName()
        );

        FilterRegistrationBean<JwtValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.JWT_VALIDATION);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
