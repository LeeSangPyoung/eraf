package com.eraf.starter.actuator;

import com.eraf.starter.actuator.health.DatabaseHealthIndicator;
import com.eraf.starter.actuator.health.KafkaHealthIndicator;
import com.eraf.starter.actuator.health.RedisHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaAdmin;

import javax.sql.DataSource;

/**
 * ERAF Actuator Auto Configuration
 * 다양한 Health Indicator 자동 등록
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
@EnableConfigurationProperties(ErafActuatorProperties.class)
public class ErafActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "erafHealthIndicator")
    public HealthIndicator erafHealthIndicator() {
        return () -> Health.up()
                .withDetail("module", "eraf-commons")
                .withDetail("version", "1.0.0-SNAPSHOT")
                .build();
    }

    /**
     * Redis Health Indicator 자동 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "eraf.actuator.health.redis.enabled", havingValue = "true", matchIfMissing = true)
    static class RedisHealthConfiguration {

        @Bean
        @ConditionalOnBean(RedisConnectionFactory.class)
        @ConditionalOnMissingBean(name = "erafRedisHealthIndicator")
        public RedisHealthIndicator erafRedisHealthIndicator(RedisConnectionFactory connectionFactory) {
            return new RedisHealthIndicator(connectionFactory);
        }
    }

    /**
     * Database Health Indicator 자동 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "eraf.actuator.health.database.enabled", havingValue = "true", matchIfMissing = true)
    static class DatabaseHealthConfiguration {

        @Bean
        @ConditionalOnBean(DataSource.class)
        @ConditionalOnMissingBean(name = "erafDatabaseHealthIndicator")
        public DatabaseHealthIndicator erafDatabaseHealthIndicator(DataSource dataSource) {
            return new DatabaseHealthIndicator(dataSource);
        }
    }

    /**
     * Kafka Health Indicator 자동 설정
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(KafkaAdmin.class)
    @ConditionalOnProperty(name = "eraf.actuator.health.kafka.enabled", havingValue = "true", matchIfMissing = true)
    static class KafkaHealthConfiguration {

        @Bean
        @ConditionalOnBean(KafkaAdmin.class)
        @ConditionalOnMissingBean(name = "erafKafkaHealthIndicator")
        public KafkaHealthIndicator erafKafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
            return new KafkaHealthIndicator(kafkaAdmin);
        }
    }
}
