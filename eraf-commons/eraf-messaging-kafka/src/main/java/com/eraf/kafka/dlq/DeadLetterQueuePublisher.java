package com.eraf.kafka.dlq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * DLQ 메시지 발행기
 */
public class DeadLetterQueuePublisher {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueuePublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String defaultDlqSuffix;

    public DeadLetterQueuePublisher(KafkaTemplate<String, String> kafkaTemplate,
                                    ObjectMapper objectMapper,
                                    String defaultDlqSuffix) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.defaultDlqSuffix = defaultDlqSuffix;
    }

    /**
     * DLQ로 메시지 발행
     */
    public void publish(DeadLetterMessage message) {
        publish(message.getOriginalTopic() + defaultDlqSuffix, message);
    }

    /**
     * 특정 DLQ 토픽으로 메시지 발행
     */
    public void publish(String dlqTopic, DeadLetterMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(dlqTopic, message.getOriginalKey(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send message to DLQ {}: {}", dlqTopic, ex.getMessage());
                        } else {
                            log.info("Message sent to DLQ {}: partition={}, offset={}",
                                    dlqTopic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DLQ message: {}", e.getMessage());
        }
    }

    /**
     * DLQ 메시지를 원본 토픽으로 재발행 (재처리)
     */
    public void reprocess(DeadLetterMessage message) {
        kafkaTemplate.send(message.getOriginalTopic(), message.getOriginalKey(), message.getOriginalMessage())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to reprocess message to {}: {}", message.getOriginalTopic(), ex.getMessage());
                    } else {
                        log.info("Message reprocessed to {}: partition={}, offset={}",
                                message.getOriginalTopic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
