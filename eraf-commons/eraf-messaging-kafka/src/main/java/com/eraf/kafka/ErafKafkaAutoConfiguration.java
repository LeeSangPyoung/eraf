package com.eraf.kafka;

import com.eraf.kafka.delayed.KafkaDelayedMessageDispatcher;
import com.eraf.kafka.dlq.DeadLetterQueuePublisher;
import com.eraf.messaging.MessagePublisher;
import com.eraf.messaging.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

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
     * Kafka 지연 메시지 디스패처 빈 등록 (선택적)
     * eraf.kafka.delayed-messaging.enabled=true 일 때만 활성화
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(KafkaDelayedMessageDispatcher.class)
    @ConditionalOnProperty(name = "eraf.kafka.delayed-messaging.enabled", havingValue = "true")
    public KafkaDelayedMessageDispatcher kafkaDelayedMessageDispatcher(
            KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaDelayedMessageDispatcher(kafkaTemplate);
    }

    /**
     * Kafka 메시지 퍼블리셔 빈 등록
     * KafkaDelayedMessageDispatcher가 있으면 지연 발행 지원
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(MessagePublisher.class)
    @ConditionalOnProperty(name = "eraf.messaging.type", havingValue = "kafka", matchIfMissing = true)
    public MessagePublisher kafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                                   MessagingProperties properties,
                                                   @org.springframework.beans.factory.annotation.Autowired(required = false)
                                                   KafkaDelayedMessageDispatcher delayedDispatcher) {
        return new KafkaMessagePublisher(kafkaTemplate, properties, delayedDispatcher);
    }

    /**
     * ERAF Kafka 프로듀서 팩토리 커스터마이저
     * ErafKafkaProperties의 producer 설정을 ProducerFactory에 적용
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafKafkaProducerFactoryCustomizer")
    public DefaultKafkaProducerFactoryCustomizer erafKafkaProducerFactoryCustomizer(ErafKafkaProperties properties) {
        return producerFactory -> {
            ErafKafkaProperties.ProducerProperties producerProps = properties.getProducer();
            Map<String, Object> configs = new HashMap<>();
            configs.put(ProducerConfig.ACKS_CONFIG, producerProps.getAcks());
            configs.put(ProducerConfig.BATCH_SIZE_CONFIG, producerProps.getBatchSize());
            configs.put(ProducerConfig.LINGER_MS_CONFIG, producerProps.getLingerMs());
            configs.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerProps.getBufferMemory());
            configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, producerProps.getCompressionType());
            configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, producerProps.isIdempotence());
            producerFactory.updateConfigs(configs);
        };
    }

    /**
     * ERAF Kafka 컨슈머 팩토리 커스터마이저
     * ErafKafkaProperties의 consumer 설정을 ConsumerFactory에 적용
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafKafkaConsumerFactoryCustomizer")
    public DefaultKafkaConsumerFactoryCustomizer erafKafkaConsumerFactoryCustomizer(ErafKafkaProperties properties) {
        return consumerFactory -> {
            Map<String, Object> configs = new HashMap<>();
            configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
            configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
            configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());
            consumerFactory.updateConfigs(configs);
        };
    }

    /**
     * Dead Letter Queue Publisher 빈 등록
     * DLQ 활성화 시 실패 메시지를 DLQ 토픽으로 발행
     */
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(DeadLetterQueuePublisher.class)
    @ConditionalOnProperty(name = "eraf.kafka.dlq.enabled", havingValue = "true", matchIfMissing = true)
    public DeadLetterQueuePublisher deadLetterQueuePublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            ErafKafkaProperties properties) {
        return new DeadLetterQueuePublisher(kafkaTemplate, objectMapper, properties.getDlq().getTopicSuffix());
    }

    /**
     * ERAF Kafka 에러 핸들러 빈 등록
     * DLQ 활성화 시 실패 메시지를 자동으로 DLQ로 라우팅
     */
    @Bean
    @ConditionalOnBean(ErafKafkaProducer.class)
    @ConditionalOnMissingBean(ErafKafkaErrorHandler.class)
    @ConditionalOnProperty(name = "eraf.kafka.dlq.enabled", havingValue = "true", matchIfMissing = true)
    public ErafKafkaErrorHandler erafKafkaErrorHandler(
            ErafKafkaProducer producer,
            ErafKafkaProperties properties) {
        return new ErafKafkaErrorHandler(producer, properties);
    }

    /**
     * Kafka 트랜잭션 매니저 빈 등록
     */
    @Bean
    @ConditionalOnBean(org.springframework.kafka.core.ProducerFactory.class)
    @ConditionalOnMissingBean(org.springframework.kafka.transaction.KafkaTransactionManager.class)
    @ConditionalOnProperty(name = "eraf.kafka.transaction.enabled", havingValue = "true")
    public org.springframework.kafka.transaction.KafkaTransactionManager<String, Object> kafkaTransactionManager(
            org.springframework.kafka.core.ProducerFactory<String, Object> producerFactory,
            ErafKafkaProperties properties) {
        producerFactory.transactionCapable();
        org.springframework.kafka.transaction.KafkaTransactionManager<String, Object> manager =
                new org.springframework.kafka.transaction.KafkaTransactionManager<>(producerFactory);
        manager.setTransactionIdPrefix(properties.getTransaction().getIdPrefix());
        return manager;
    }
}
