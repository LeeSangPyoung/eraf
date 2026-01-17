package com.eraf.gateway.circuitbreaker.config;

import com.eraf.gateway.circuitbreaker.CircuitBreakerFilter;
import com.eraf.gateway.circuitbreaker.CircuitBreakerRegistry;
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
 * Circuit Breaker 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(CircuitBreakerFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.circuit-breaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CircuitBreakerProperties.class)
public class CircuitBreakerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerProperties properties) {
        log.info("Initializing CircuitBreakerRegistry with failureThreshold={}, successThreshold={}, openTimeoutMs={}",
                properties.getDefaultFailureThreshold(),
                properties.getDefaultSuccessThreshold(),
                properties.getDefaultOpenTimeoutMs());
        return new CircuitBreakerRegistry(
                properties.getDefaultFailureThreshold(),
                properties.getDefaultSuccessThreshold(),
                properties.getDefaultOpenTimeoutMs()
        );
    }

    @Bean
    public FilterRegistrationBean<CircuitBreakerFilter> circuitBreakerFilterRegistration(
            CircuitBreakerRegistry registry,
            CircuitBreakerProperties properties) {
        log.info("Registering CircuitBreakerFilter with order: {}", FilterOrder.CIRCUIT_BREAKER);

        CircuitBreakerFilter filter = new CircuitBreakerFilter(registry, properties.getExcludePatterns());

        FilterRegistrationBean<CircuitBreakerFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.CIRCUIT_BREAKER);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
