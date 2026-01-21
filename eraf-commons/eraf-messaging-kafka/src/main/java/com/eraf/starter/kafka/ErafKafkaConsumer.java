package com.eraf.starter.kafka;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.Acknowledgment;

import java.util.function.Consumer;

/**
 * ERAF Kafka 컨슈머 헬퍼
 * 표준 이벤트 포맷 파싱 및 에러 처리
 */
public class ErafKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ErafKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final ErafKafkaProperties properties;

    public ErafKafkaConsumer(ObjectMapper objectMapper, ErafKafkaProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 메시지 파싱 및 처리
     */
    public <T> void process(String message, Class<T> payloadType, Consumer<ErafKafkaEvent<T>> handler) {
        process(message, payloadType, handler, null);
    }

    /**
     * 메시지 파싱 및 처리 (수동 ACK)
     */
    public <T> void process(String message, Class<T> payloadType,
                            Consumer<ErafKafkaEvent<T>> handler, Acknowledgment ack) {
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(ErafKafkaEvent.class, payloadType);
            ErafKafkaEvent<T> event = objectMapper.readValue(message, type);

            log.debug("Processing event: eventId={}, eventType={}", event.getEventId(), event.getEventType());

            handler.accept(event);

            if (ack != null) {
                ack.acknowledge();
            }

            log.debug("Event processed successfully: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage(), e);
            throw new ErafKafkaException("Failed to process Kafka message", e);
        }
    }

    /**
     * 메시지 파싱만 수행
     */
    public <T> ErafKafkaEvent<T> parse(String message, Class<T> payloadType) {
        try {
            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(ErafKafkaEvent.class, payloadType);
            return objectMapper.readValue(message, type);
        } catch (Exception e) {
            log.error("Failed to parse message: {}", e.getMessage());
            throw new ErafKafkaException("Failed to parse Kafka message", e);
        }
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean isRetryable(Exception e) {
        // 네트워크/일시적 오류는 재시도
        if (e instanceof java.net.SocketException ||
            e instanceof java.net.SocketTimeoutException ||
            e instanceof org.apache.kafka.common.errors.RetriableException) {
            return true;
        }
        // 비즈니스 로직 오류는 재시도 안 함
        return false;
    }
}
