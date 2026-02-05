package com.eraf.kafka;

import com.eraf.core.messaging.MessagePublisher;
import com.eraf.core.messaging.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * ERAF Kafka Auto Configuration
 * Kafka 프로듀서/컨슈머 자동 구성
 */
@AutoConfiguration
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties({ErafKafkaProperties.class, MessagingProperties.class})
public class ErafKafkaAutoConfiguration {

    /**
     * ERAF Kafka 프로듀서 빈 등록
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(ErafKafkaProducer.class)
    public ErafKafkaProducer erafKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            ErafKafkaProperties properties) {
        return new ErafKafkaProducer(kafkaTemplate, objectMapper, properties);
    }

    /**
     * ERAF Kafka 컨슈머 헬퍼 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean(ErafKafkaConsumer.class)
    public ErafKafkaConsumer erafKafkaConsumer(
            ObjectMapper objectMapper,
            ErafKafkaProperties properties) {
        return new ErafKafkaConsumer(objectMapper, properties);
    }

    /**
     * Kafka 메시지 퍼블리셔 빈 등록
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(MessagePublisher.class)
    @ConditionalOnProperty(name = "eraf.messaging.type", havingValue = "kafka", matchIfMissing = true)
    public MessagePublisher kafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                                   MessagingProperties properties) {
        return new KafkaMessagePublisher(kafkaTemplate, properties);
    }
}
