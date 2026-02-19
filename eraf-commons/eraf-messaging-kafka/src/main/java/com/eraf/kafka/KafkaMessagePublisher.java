package com.eraf.kafka;

import com.eraf.core.context.ErafContext;
import com.eraf.kafka.delayed.KafkaDelayedMessageDispatcher;
import com.eraf.messaging.ErafMessage;
import com.eraf.messaging.MessagingProperties;
import com.eraf.messaging.MessagePublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 메시지 퍼블리셔 구현
 *
 * <p>지연 발행({@link #publishDelayed})은 {@link KafkaDelayedMessageDispatcher}가
 * 등록된 경우 인-메모리 DelayQueue 방식으로 동작합니다.</p>
 */
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessagingProperties properties;
    private final KafkaDelayedMessageDispatcher delayedDispatcher;

    public KafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  MessagingProperties properties) {
        this(kafkaTemplate, properties, null);
    }

    public KafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  MessagingProperties properties,
                                  KafkaDelayedMessageDispatcher delayedDispatcher) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.delayedDispatcher = delayedDispatcher;
    }

    @Override
    public void publish(String destination, Object message) {
        publish(destination, message, null);
    }

    @Override
    public void publish(String destination, Object message, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        ProducerRecord<String, Object> record = createRecord(destination, erafMessage, headers);
        kafkaTemplate.send(record);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String destination, Object message) {
        return publishAsync(destination, message, null);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String destination, Object message, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        ProducerRecord<String, Object> record = createRecord(destination, erafMessage, headers);

        return kafkaTemplate.send(record)
                .thenAccept(result -> {})
                .toCompletableFuture();
    }

    /**
     * 지연 발행
     *
     * <p>{@link KafkaDelayedMessageDispatcher}가 빈으로 등록된 경우 인-메모리 DelayQueue로 처리합니다.
     * 디스패처가 없으면 {@link UnsupportedOperationException}을 던집니다.</p>
     *
     * <p><strong>주의:</strong> 인-메모리 방식이므로 애플리케이션 재시작 시 소실됩니다.
     * 영속적 지연이 필요하다면 eraf.kafka.delayed-messaging.enabled=true 설정 후
     * DB 기반 스케줄러와 조합하세요.</p>
     *
     * @param destination 대상 토픽
     * @param message     발행할 메시지
     * @param delayMillis 지연 시간 (밀리초)
     */
    @Override
    public void publish(String destination, String key, Object message) {
        publish(destination, key, message, null);
    }

    @Override
    public void publish(String destination, String key, Object message, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        ProducerRecord<String, Object> record = createRecord(destination, key, erafMessage, headers);
        kafkaTemplate.send(record);
    }

    @Override
    public void publishDelayed(String destination, Object message, long delayMillis) {
        publishDelayed(destination, message, delayMillis, null);
    }

    @Override
    public void publishDelayed(String destination, Object message, long delayMillis, Map<String, Object> headers) {
        if (delayedDispatcher == null) {
            throw new UnsupportedOperationException(
                    "Delayed messaging requires eraf.kafka.delayed-messaging.enabled=true. " +
                    "Kafka does not support delayed messages natively; ERAF uses an in-memory DelayQueue.");
        }
        ErafMessage<Object> erafMessage = wrapMessage(message);
        delayedDispatcher.schedule(destination, erafMessage, headers, delayMillis);
    }

    @SuppressWarnings("unchecked")
    private ErafMessage<Object> wrapMessage(Object message) {
        if (message instanceof ErafMessage) {
            return (ErafMessage<Object>) message;
        }

        ErafMessage<Object> erafMessage = ErafMessage.of(message);

        if (properties.isContextPropagationEnabled()) {
            String traceId = ErafContext.getTraceId();
            if (traceId != null) {
                erafMessage.withTraceId(traceId);
            }
        }

        return erafMessage;
    }

    private ProducerRecord<String, Object> createRecord(String destination,
                                                         ErafMessage<Object> message,
                                                         Map<String, Object> additionalHeaders) {
        return createRecord(destination, message.getMessageId(), message, additionalHeaders);
    }

    private ProducerRecord<String, Object> createRecord(String destination,
                                                         String key,
                                                         ErafMessage<Object> message,
                                                         Map<String, Object> additionalHeaders) {
        Headers headers = new RecordHeaders();

        if (message.getTraceId() != null) {
            headers.add("X-Trace-Id", message.getTraceId().getBytes(StandardCharsets.UTF_8));
        }

        headers.add("X-Message-Id", message.getMessageId().getBytes(StandardCharsets.UTF_8));

        if (additionalHeaders != null) {
            additionalHeaders.forEach((k, value) -> {
                if (value != null) {
                    headers.add(k, String.valueOf(value).getBytes(StandardCharsets.UTF_8));
                }
            });
        }

        return new ProducerRecord<>(destination, null, null, key, message, headers);
    }
}
