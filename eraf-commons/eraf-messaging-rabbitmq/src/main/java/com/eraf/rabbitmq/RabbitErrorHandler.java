package com.eraf.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

/**
 * ERAF RabbitMQ 에러 핸들러
 * 메시지 처리 실패 시 로깅 및 예외 전파
 */
public class RabbitErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RabbitErrorHandler.class);

    private final ErafRabbitProperties properties;

    public RabbitErrorHandler(ErafRabbitProperties properties) {
        this.properties = properties;
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

        // 예외를 다시 던져서 재시도 메커니즘이 동작하도록 함
        throw exception;
    }
}
