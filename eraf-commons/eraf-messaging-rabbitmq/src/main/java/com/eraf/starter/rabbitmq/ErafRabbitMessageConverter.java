package com.eraf.starter.rabbitmq;

import com.eraf.core.context.ErafContext;
import com.eraf.core.context.ErafContextHolder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * ERAF RabbitMQ 메시지 컨버터
 * TraceId 자동 전파
 */
public class ErafRabbitMessageConverter extends Jackson2JsonMessageConverter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final ErafRabbitProperties properties;

    public ErafRabbitMessageConverter(ErafRabbitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties)
            throws MessageConversionException {

        if (properties.isContextPropagationEnabled()) {
            // TraceId 전파
            String traceId = ErafContext.getTraceId();
            if (traceId != null) {
                messageProperties.setHeader(TRACE_ID_HEADER, traceId);
            }

            // RequestId 전파
            String requestId = ErafContext.getRequestId();
            if (requestId != null) {
                messageProperties.setHeader(REQUEST_ID_HEADER, requestId);
            }

            // UserId 전파
            String userId = ErafContext.getCurrentUserId();
            if (userId != null) {
                messageProperties.setHeader(USER_ID_HEADER, userId);
            }
        }

        return super.createMessage(object, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        if (properties.isContextPropagationEnabled()) {
            MessageProperties props = message.getMessageProperties();

            // TraceId 복원
            Object traceId = props.getHeader(TRACE_ID_HEADER);
            if (traceId != null) {
                ErafContextHolder.getContext().setTraceId(String.valueOf(traceId));
            }

            // RequestId 복원
            Object requestId = props.getHeader(REQUEST_ID_HEADER);
            if (requestId != null) {
                ErafContextHolder.getContext().setRequestId(String.valueOf(requestId));
            }
        }

        return super.fromMessage(message);
    }
}
