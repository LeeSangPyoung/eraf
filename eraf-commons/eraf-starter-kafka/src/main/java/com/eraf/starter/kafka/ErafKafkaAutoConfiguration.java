package com.eraf.starter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * ERAF Kafka Auto Configuration
 * Kafka 프로듀서/컨슈머 자동 구성
 */
@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(ErafKafkaProperties.class)
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
     * ObjectMapper 기본 빈 (없을 경우)
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
