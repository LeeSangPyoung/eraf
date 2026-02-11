package com.eraf.openapi.features.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Circuit Breaker Configuration
 * Circular dependency 방지를 위해 별도 설정
 */
@Slf4j
@Configuration
public class CircuitBreakerConfiguration {

    /**
     * Circuit Breaker Registry Bean
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.info("Creating CircuitBreakerRegistry");
        return CircuitBreakerRegistry.ofDefaults();
    }
}
