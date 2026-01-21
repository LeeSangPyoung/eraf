package com.eraf.starter.rabbitmq;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@EnableConfigurationProperties(ErafRabbitProperties.class)
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
                                              MessageConverter erafRabbitMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(erafRabbitMessageConverter);
        return template;
    }
}
