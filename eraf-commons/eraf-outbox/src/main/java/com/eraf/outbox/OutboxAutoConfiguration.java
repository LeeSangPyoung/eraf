package com.eraf.outbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Outbox Pattern Auto Configuration
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(ErafOutboxProperties.class)
@ConditionalOnBean(OutboxRepository.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.outbox.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxScheduler outboxScheduler(OutboxRepository repository, ErafOutboxProperties properties) {
        return new OutboxScheduler(repository, properties);
    }
}
