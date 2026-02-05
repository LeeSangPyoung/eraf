package com.eraf.saga.event;

import com.eraf.core.messaging.MessagePublisher;
import com.eraf.saga.ErafSagaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 메시징 기반 Saga 이벤트 발행자
 * Kafka/RabbitMQ 등을 통해 분산 환경에서 이벤트 발행
 */
public class MessagingSagaEventPublisher implements SagaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagingSagaEventPublisher.class);
    private static final String DEFAULT_TOPIC = "eraf.saga.events";

    private final MessagePublisher messagePublisher;
    private final ErafSagaProperties properties;

    public MessagingSagaEventPublisher(MessagePublisher messagePublisher,
                                        ErafSagaProperties properties) {
        this.messagePublisher = messagePublisher;
        this.properties = properties;
    }

    @Override
    public void publish(SagaEvent event) {
        String topic = properties.getEventTopic() != null ?
                properties.getEventTopic() : DEFAULT_TOPIC;
        publish(topic, event);
    }

    @Override
    public void publish(String topic, SagaEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        log.debug("Publishing saga event to {}: {} for saga {}",
                topic, event.getEventType(), event.getSagaId());

        try {
            messagePublisher.publish(topic, event);
        } catch (Exception e) {
            log.error("Failed to publish saga event: {} for saga {}",
                    event.getEventType(), event.getSagaId(), e);
            // 이벤트 발행 실패는 Saga 실행에 영향을 주지 않음
        }
    }
}
