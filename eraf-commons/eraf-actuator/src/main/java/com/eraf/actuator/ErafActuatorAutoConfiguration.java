package com.eraf.actuator;

import com.eraf.actuator.health.*;
import com.eraf.actuator.metrics.*;
import com.eraf.actuator.tracing.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaAdmin;

import javax.sql.DataSource;

/**
 * ERAF Actuator Auto Configuration
 * Health Indicator, Metrics, Tracing 자동 등록
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
@EnableConfigurationProperties({
        ErafActuatorProperties.class,
        ErafMetricsProperties.class,
        ErafTracingProperties.class
})
public class ErafActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "erafHealthIndicator")
    public HealthIndicator erafHealthIndicator() {
        return () -> Health.up()
                .withDetail("module", "eraf-commons")
                .withDetail("version", "1.0.0-SNAPSHOT")
                .build();
    }

    // ===== Metrics =====

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(name = "eraf.actuator.metrics.enabled", havingValue = "true", matchIfMissing = true)
    static class MetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public TimedAspect erafTimedAspect(MeterRegistry meterRegistry) {
            return new TimedAspect(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        public CountedAspect erafCountedAspect(MeterRegistry meterRegistry) {
            return new CountedAspect(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        public BusinessMetrics businessMetrics(MeterRegistry meterRegistry, ErafMetricsProperties properties) {
            return new BusinessMetrics(meterRegistry, properties.getBusinessPrefix());
        }

        @Bean
        @ConditionalOnMissingBean
        public CustomMetrics customMetrics(MeterRegistry meterRegistry) {
            return new CustomMetrics(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheMetrics cacheMetrics(MeterRegistry meterRegistry) {
            return new CacheMetrics(meterRegistry);
        }

        @Bean
        @ConditionalOnMissingBean
        public ApiMetrics apiMetrics(MeterRegistry meterRegistry) {
            return new ApiMetrics(meterRegistry);
        }
    }

    // ===== Tracing =====

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(name = "eraf.actuator.tracing.enabled", havingValue = "true", matchIfMissing = true)
    static class TracingConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public TracingFilter tracingFilter(ErafTracingProperties properties) {
            return new TracingFilter(properties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "tracingFilterRegistration")
        public FilterRegistrationBean<TracingFilter> tracingFilterRegistration(TracingFilter tracingFilter) {
            FilterRegistrationBean<TracingFilter> registration = new FilterRegistrationBean<>(tracingFilter);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
            registration.addUrlPatterns("/*");
            registration.setName("tracingFilter");
            return registration;
        }

        @Bean
        @ConditionalOnMissingBean
        public TracedAspect tracedAspect() {
            return new TracedAspect();
        }
    }

    // ===== Health Indicators =====

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

    // ===== Advanced Health Indicators =====

    /**
     * Health Check Registry
     */
    @Bean
    @ConditionalOnMissingBean
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    /**
     * Liveness Health Indicator
     */
    @Bean
    @ConditionalOnMissingBean
    public LivenessHealthIndicator livenessHealthIndicator() {
        LivenessHealthIndicator indicator = new LivenessHealthIndicator();
        indicator.markAsAlive();  // 기본적으로 살아있음
        return indicator;
    }

    /**
     * Readiness Health Indicator
     */
    @Bean
    @ConditionalOnMissingBean
    public ReadinessHealthIndicator readinessHealthIndicator() {
        ReadinessHealthIndicator indicator = new ReadinessHealthIndicator();
        // 기본적으로 준비 안 됨 (애플리케이션이 명시적으로 준비 완료를 표시해야 함)
        indicator.markAsNotReady();
        return indicator;
    }
}
