package com.eraf.starter.messaging;

import com.eraf.starter.messaging.kafka.KafkaMessagePublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * ERAF 메시징 Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafMessagingProperties.class)
public class ErafMessagingAutoConfiguration {

    /**
     * Kafka 설정
     */
    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(name = "eraf.messaging.type", havingValue = "kafka", matchIfMissing = true)
    public static class KafkaMessagingConfiguration {

        @Bean
        @ConditionalOnMissingBean(MessagePublisher.class)
        public MessagePublisher kafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                                       ErafMessagingProperties properties) {
            return new KafkaMessagePublisher(kafkaTemplate, properties);
        }
    }
}
