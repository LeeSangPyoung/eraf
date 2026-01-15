package com.eraf.starter.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Actuator Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
@EnableConfigurationProperties(ErafActuatorProperties.class)
public class ErafActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "erafHealthIndicator")
    public HealthIndicator erafHealthIndicator() {
        return () -> Health.up()
                .withDetail("module", "eraf-commons")
                .withDetail("version", "1.0.0-SNAPSHOT")
                .build();
    }
}
