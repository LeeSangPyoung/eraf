package com.eraf.starter.messaging.kafka;

import com.eraf.core.context.ErafContext;
import com.eraf.starter.messaging.ErafMessage;
import com.eraf.starter.messaging.ErafMessagingProperties;
import com.eraf.starter.messaging.MessagePublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka 메시지 퍼블리셔 구현
 */
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ErafMessagingProperties properties;

    public KafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  ErafMessagingProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
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

    @Override
    public void publishDelayed(String destination, Object message, long delayMillis) {
        // Kafka는 기본적으로 지연 메시지를 지원하지 않음
        // 별도의 지연 토픽 패턴 사용 필요
        throw new UnsupportedOperationException("Kafka does not support delayed messages natively. Use delay topic pattern.");
    }

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
        Headers headers = new RecordHeaders();

        // TraceId 헤더 추가
        if (message.getTraceId() != null) {
            headers.add("X-Trace-Id", message.getTraceId().getBytes(StandardCharsets.UTF_8));
        }

        // MessageId 헤더 추가
        headers.add("X-Message-Id", message.getMessageId().getBytes(StandardCharsets.UTF_8));

        // 추가 헤더
        if (additionalHeaders != null) {
            additionalHeaders.forEach((key, value) -> {
                if (value != null) {
                    headers.add(key, String.valueOf(value).getBytes(StandardCharsets.UTF_8));
                }
            });
        }

        return new ProducerRecord<>(destination, null, null, message.getMessageId(), message, headers);
    }
}
