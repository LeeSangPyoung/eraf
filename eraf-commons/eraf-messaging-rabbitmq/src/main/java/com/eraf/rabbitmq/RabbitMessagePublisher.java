package com.eraf.rabbitmq;

import com.eraf.messaging.ErafMessage;
import com.eraf.messaging.MessagePublisher;
import com.eraf.messaging.MessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RabbitMQ 메시지 퍼블리셔 구현
 */
public class RabbitMessagePublisher implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    public RabbitMessagePublisher(RabbitTemplate rabbitTemplate,
                                  MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(String destination, Object message) {
        publish(destination, message, null);
    }

    @Override
    public void publish(String destination, Object message, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        rabbitTemplate.convertAndSend(destination, erafMessage, msg -> {
            applyHeaders(msg.getMessageProperties(), erafMessage, headers);
            return msg;
        });
    }

    @Override
    public CompletableFuture<Void> publishAsync(String destination, Object message) {
        return publishAsync(destination, message, null);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String destination, Object message, Map<String, Object> headers) {
        return CompletableFuture.runAsync(() -> publish(destination, message, headers));
    }

    @Override
    public void publish(String destination, String key, Object message) {
        publish(destination, key, message, null);
    }

    @Override
    public void publish(String destination, String key, Object message, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        rabbitTemplate.convertAndSend(destination, key, erafMessage, msg -> {
            applyHeaders(msg.getMessageProperties(), erafMessage, headers);
            return msg;
        });
    }

    @Override
    public void publishDelayed(String destination, Object message, long delayMillis) {
        publishDelayed(destination, message, delayMillis, null);
    }

    @Override
    public void publishDelayed(String destination, Object message, long delayMillis, Map<String, Object> headers) {
        ErafMessage<Object> erafMessage = wrapMessage(message);
        rabbitTemplate.convertAndSend(destination, erafMessage, msg -> {
            msg.getMessageProperties().setDelayLong(delayMillis);
            applyHeaders(msg.getMessageProperties(), erafMessage, headers);
            return msg;
        });
    }

    @SuppressWarnings("unchecked")
    private ErafMessage<Object> wrapMessage(Object message) {
        if (message instanceof ErafMessage) {
            return (ErafMessage<Object>) message;
        }

        ErafMessage<Object> erafMessage = ErafMessage.of(message);

        if (properties.isContextPropagationEnabled()) {
            try {
                Class<?> contextClass = Class.forName("com.eraf.core.context.ErafContext");
                java.lang.reflect.Method getTraceId = contextClass.getMethod("getTraceId");
                Object traceId = getTraceId.invoke(null);
                if (traceId != null) {
                    erafMessage.withTraceId(traceId.toString());
                }
            } catch (Exception e) {
                // ErafContext not available, skip context propagation
            }
        }

        return erafMessage;
    }

    private void applyHeaders(MessageProperties messageProperties,
                              ErafMessage<Object> erafMessage,
                              Map<String, Object> additionalHeaders) {
        // TraceId 헤더 추가
        if (erafMessage.getTraceId() != null) {
            messageProperties.setHeader("X-Trace-Id", erafMessage.getTraceId());
        }

        // MessageId 헤더 추가
        messageProperties.setHeader("X-Message-Id", erafMessage.getMessageId());

        // 추가 헤더
        if (additionalHeaders != null) {
            additionalHeaders.forEach((key, value) -> {
                if (value != null) {
                    messageProperties.setHeader(key, value);
                }
            });
        }
    }
}
