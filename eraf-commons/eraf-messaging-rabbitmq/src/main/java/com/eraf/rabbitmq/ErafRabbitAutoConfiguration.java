package com.eraf.rabbitmq;

import com.eraf.messaging.MessagePublisher;
import com.eraf.messaging.MessagingProperties;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * ERAF RabbitMQ Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
@EnableConfigurationProperties({ErafRabbitProperties.class, MessagingProperties.class})
public class ErafRabbitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "erafRabbitMessageConverter")
    public MessageConverter erafRabbitMessageConverter(ErafRabbitProperties properties) {
        return new ErafRabbitMessageConverter(properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "erafRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory erafRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter erafRabbitMessageConverter,
            ErafRabbitProperties properties) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(erafRabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);

        // 재시도 설정
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getRetryCount());
        retryTemplate.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getRetryInitialInterval());
        backOffPolicy.setMaxInterval(properties.getRetryMaxInterval());
        backOffPolicy.setMultiplier(properties.getRetryMultiplier());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        factory.setRetryTemplate(retryTemplate);

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate erafRabbitTemplate(ConnectionFactory connectionFactory,
                                              MessageConverter erafRabbitMessageConverter,
                                              ObjectProvider<MessagePostProcessor> postProcessorProvider) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(erafRabbitMessageConverter);
        MessagePostProcessor postProcessor = postProcessorProvider.getIfAvailable();
        if (postProcessor != null) {
            template.setBeforePublishPostProcessors(postProcessor);
        }
        return template;
    }

    /**
     * RabbitMQ 메시지 퍼블리셔 빈 등록
     */
    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnMissingBean(MessagePublisher.class)
    @ConditionalOnProperty(name = "eraf.messaging.type", havingValue = "rabbitmq")
    public MessagePublisher rabbitMessagePublisher(RabbitTemplate rabbitTemplate,
                                                    MessagingProperties messagingProperties) {
        return new RabbitMessagePublisher(rabbitTemplate, messagingProperties);
    }

    /**
     * RabbitMQ 에러 핸들러 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafRabbitErrorHandler")
    public RabbitListenerErrorHandler erafRabbitErrorHandler(ErafRabbitProperties properties) {
        return new RabbitErrorHandler(properties);
    }

    /**
     * 컨텍스트 전파 MessagePostProcessor
     * TraceId, RequestId, UserId를 메시지 헤더에 자동 전파
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafContextMessagePostProcessor")
    @ConditionalOnProperty(name = "eraf.rabbitmq.context-propagation-enabled", havingValue = "true", matchIfMissing = true)
    public MessagePostProcessor erafContextMessagePostProcessor() {
        return message -> {
            try {
                Class<?> contextClass = Class.forName("com.eraf.core.context.ErafContext");

                java.lang.reflect.Method getTraceId = contextClass.getMethod("getTraceId");
                Object traceId = getTraceId.invoke(null);
                if (traceId != null) {
                    message.getMessageProperties().setHeader("X-Trace-Id", traceId.toString());
                }

                java.lang.reflect.Method getRequestId = contextClass.getMethod("getRequestId");
                Object requestId = getRequestId.invoke(null);
                if (requestId != null) {
                    message.getMessageProperties().setHeader("X-Request-Id", requestId.toString());
                }

                java.lang.reflect.Method getUserId = contextClass.getMethod("getCurrentUserId");
                Object userId = getUserId.invoke(null);
                if (userId != null) {
                    message.getMessageProperties().setHeader("X-User-Id", userId.toString());
                }
            } catch (Exception e) {
                // ErafContext not available, skip propagation
            }
            return message;
        };
    }
}
