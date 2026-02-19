package com.eraf.outbox;

import com.eraf.messaging.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Outbox Pattern Auto Configuration
 *
 * <p>{@link MessagePublisher}가 빈으로 존재하면 자동으로 Outbox 폴러와 연동합니다.</p>
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(ErafOutboxProperties.class)
@ConditionalOnBean(OutboxRepository.class)
public class OutboxAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OutboxAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.outbox.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxScheduler outboxScheduler(
            OutboxRepository repository,
            ErafOutboxProperties properties,
            @Autowired(required = false) MessagePublisher messagePublisher) {

        OutboxScheduler scheduler = new OutboxScheduler(repository, properties);

        if (messagePublisher != null) {
            scheduler.setMessagePublisher(message -> {
                String destination = message.getDestination();
                if (destination == null || destination.isEmpty()) {
                    destination = message.getAggregateType();
                }
                messagePublisher.publish(destination, message.getPayload());
            });
            log.info("OutboxScheduler auto-wired with MessagePublisher");
        } else {
            log.warn("OutboxScheduler created without MessagePublisher - set it manually via setMessagePublisher()");
        }

        return scheduler;
    }
}
