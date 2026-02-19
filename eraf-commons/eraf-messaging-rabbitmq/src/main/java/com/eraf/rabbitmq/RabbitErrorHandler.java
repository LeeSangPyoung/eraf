package com.eraf.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

/**
 * ERAF RabbitMQ 에러 핸들러
 * 메시지 처리 실패 시 로깅 및 DLQ 전송
 */
public class RabbitErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RabbitErrorHandler.class);

    private final ErafRabbitProperties properties;
    private final RabbitTemplate rabbitTemplate;

    public RabbitErrorHandler(ErafRabbitProperties properties) {
        this(properties, null);
    }

    public RabbitErrorHandler(ErafRabbitProperties properties, RabbitTemplate rabbitTemplate) {
        this.properties = properties;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Object handleError(Message amqpMessage,
                              org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws Exception {
        String messageId = amqpMessage.getMessageProperties().getMessageId();
        String queue = amqpMessage.getMessageProperties().getConsumerQueue();
        String traceId = (String) amqpMessage.getMessageProperties().getHeader("X-Trace-Id");

        log.error("RabbitMQ message processing failed - queue: {}, messageId: {}, traceId: {}, error: {}",
                queue, messageId, traceId, exception.getMessage(), exception.getCause());

        // DLQ 활성화 시 메시지를 DLQ로 라우팅
        if (properties.isDlqEnabled() && rabbitTemplate != null && queue != null) {
            try {
                String dlqQueue = queue + properties.getDlqSuffix();
                amqpMessage.getMessageProperties().setHeader("x-original-queue", queue);
                amqpMessage.getMessageProperties().setHeader("x-exception-message", exception.getMessage());
                amqpMessage.getMessageProperties().setHeader("x-exception-class",
                        exception.getCause() != null ? exception.getCause().getClass().getName() : exception.getClass().getName());
                amqpMessage.getMessageProperties().setHeader("x-failed-timestamp", System.currentTimeMillis());

                rabbitTemplate.send("", dlqQueue, amqpMessage);
                log.info("Message sent to DLQ: {} (originalQueue: {}, messageId: {})", dlqQueue, queue, messageId);
                return null; // DLQ로 보냈으므로 정상 처리로 간주
            } catch (Exception dlqEx) {
                log.error("Failed to send message to DLQ: {}", dlqEx.getMessage(), dlqEx);
            }
        }

        // DLQ 비활성화이거나 DLQ 전송 실패 시 예외를 다시 던져서 재시도 메커니즘이 동작하도록 함
        throw exception;
    }
}
