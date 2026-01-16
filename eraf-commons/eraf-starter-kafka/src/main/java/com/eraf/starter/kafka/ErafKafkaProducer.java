package com.eraf.starter.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * ERAF Kafka 프로듀서
 * 표준 이벤트 포맷으로 메시지 발행
 */
public class ErafKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ErafKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ErafKafkaProperties properties;

    public ErafKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper,
                             ErafKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 동기 메시지 발행
     */
    public <T> void send(String topic, ErafKafkaEvent<T> event) {
        sendAsync(topic, event).join();
    }

    /**
     * 동기 메시지 발행 (키 지정)
     */
    public <T> void send(String topic, String key, ErafKafkaEvent<T> event) {
        sendAsync(topic, key, event).join();
    }

    /**
     * 비동기 메시지 발행
     */
    public <T> CompletableFuture<SendResult<String, String>> sendAsync(String topic, ErafKafkaEvent<T> event) {
        return sendAsync(topic, null, event);
    }

    /**
     * 비동기 메시지 발행 (키 지정)
     */
    public <T> CompletableFuture<SendResult<String, String>> sendAsync(String topic, String key, ErafKafkaEvent<T> event) {
        String fullTopic = getFullTopicName(topic);
        try {
            String message = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(fullTopic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send message to topic {}: eventId={}, error={}",
                            fullTopic, event.getEventId(), ex.getMessage());
                } else {
                    log.debug("Message sent to topic {}: eventId={}, partition={}, offset={}",
                            fullTopic, event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

            return future;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 간단한 메시지 발행 (이벤트 자동 래핑)
     */
    public <T> void send(String topic, String eventType, T payload) {
        ErafKafkaEvent<T> event = ErafKafkaEvent.of(eventType, payload);
        send(topic, event);
    }

    /**
     * 간단한 메시지 발행 (키 지정)
     */
    public <T> void send(String topic, String key, String eventType, T payload) {
        ErafKafkaEvent<T> event = ErafKafkaEvent.of(eventType, payload);
        send(topic, key, event);
    }

    private String getFullTopicName(String topic) {
        String prefix = properties.getTopicPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + topic;
        }
        return topic;
    }
}
