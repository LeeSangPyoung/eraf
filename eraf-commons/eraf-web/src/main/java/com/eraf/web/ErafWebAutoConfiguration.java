package com.eraf.web;

import com.eraf.code.CodeRepository;
import com.eraf.code.CodeService;
import com.eraf.code.InMemoryCodeRepository;
import com.eraf.core.config.FeatureToggle;
import com.eraf.core.config.FeatureToggleAspect;
import com.eraf.exception.GlobalExceptionHandler;
import com.eraf.util.file.FileStorageService;
import com.eraf.util.file.LocalFileStorageService;
import com.eraf.i18n.MessageAspect;
import com.eraf.i18n.MessageService;
import com.eraf.idempotent.IdempotencyStore;
import com.eraf.idempotent.IdempotentAspect;
import com.eraf.idempotent.InMemoryIdempotencyStore;
import com.eraf.lock.DistributedLockAspect;
import com.eraf.lock.InMemoryLockProvider;
import com.eraf.lock.LockProvider;
import com.eraf.resilience.*;
import com.eraf.response.ApiResponse;
import com.eraf.sequence.SequenceAspect;
import com.eraf.web.context.AsyncRequestContextPropagator;
import com.eraf.web.context.RequestContextFilter;
import com.eraf.web.filter.RequestLoggingFilter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;

import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ERAF Web Auto Configuration
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ApiResponse.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(ErafWebProperties.class)
@Import({ErafWebMvcConfigurer.class})
public class ErafWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper erafObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 인메모리 멱등성 저장소 (Redis 미사용 시 폴백)
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    @ConditionalOnProperty(name = "eraf.web.idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotencyStore inMemoryIdempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    /**
     * 멱등성 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.web.idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotentAspect idempotentAspect(IdempotencyStore idempotencyStore) {
        return new IdempotentAspect(idempotencyStore);
    }

    /**
     * 인메모리 락 제공자 (Redis 미사용 시 폴백)
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(name = "eraf.web.lock.enabled", havingValue = "true", matchIfMissing = true)
    public LockProvider inMemoryLockProvider() {
        return new InMemoryLockProvider();
    }

    /**
     * 분산 락 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.web.lock.enabled", havingValue = "true", matchIfMissing = true)
    public DistributedLockAspect distributedLockAspect(LockProvider lockProvider) {
        return new DistributedLockAspect(lockProvider);
    }

    /**
     * 인메모리 공통코드 저장소 (JPA 미사용 시 폴백)
     */
    @Bean
    @ConditionalOnMissingBean(CodeRepository.class)
    public CodeRepository inMemoryCodeRepository() {
        return new InMemoryCodeRepository();
    }

    /**
     * 공통코드 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeService codeService(CodeRepository codeRepository) {
        return new CodeService(codeRepository);
    }

    /**
     * 기능 토글 관리자
     */
    @Bean
    @ConditionalOnMissingBean
    public FeatureToggle featureToggle() {
        return new FeatureToggle();
    }

    /**
     * 기능 토글 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.web.feature-toggle.enabled", havingValue = "true", matchIfMissing = true)
    public FeatureToggleAspect featureToggleAspect(FeatureToggle featureToggle, BeanFactory beanFactory) {
        return new FeatureToggleAspect(featureToggle, beanFactory);
    }

    /**
     * 채번 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.web.sequence.enabled", havingValue = "true", matchIfMissing = true)
    public SequenceAspect sequenceAspect() {
        return new SequenceAspect();
    }

    /**
     * 다국어 메시지 서비스
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageService messageService(MessageSource messageSource) {
        return new MessageService(messageSource);
    }

    /**
     * 메시지 국제화 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.web.i18n.enabled", havingValue = "true", matchIfMissing = true)
    public MessageAspect messageAspect(MessageSource messageSource) {
        return new MessageAspect(messageSource);
    }

    /**
     * RequestContext 필터 (최우선 순위)
     */
    @Bean
    @ConditionalOnMissingBean(RequestContextFilter.class)
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        RequestContextFilter filter = new RequestContextFilter();

        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/*");
        registration.setName("requestContextFilter");

        return registration;
    }

    /**
     * 비동기 작업에서 RequestContext 전파
     */
    @Bean
    @ConditionalOnMissingBean(AsyncRequestContextPropagator.class)
    public AsyncRequestContextPropagator asyncRequestContextPropagator() {
        return new AsyncRequestContextPropagator();
    }

    /**
     * 요청/응답 로깅 필터
     */
    @Bean
    @ConditionalOnMissingBean(RequestLoggingFilter.class)
    @ConditionalOnProperty(name = "eraf.web.logging.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter(ErafWebProperties properties) {
        ErafWebProperties.LoggingConfig config = properties.getLogging();

        RequestLoggingFilter filter = new RequestLoggingFilter(
                config.isIncludePayload(),
                config.getMaxPayloadLength(),
                Arrays.asList(config.getExcludePatterns()),
                config.getSampleRate()
        );

        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("requestLoggingFilter");

        return registration;
    }

    /**
     * 로컬 파일 저장소 서비스
     */
    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    @ConditionalOnProperty(name = "eraf.web.file-upload.enabled", havingValue = "true", matchIfMissing = true)
    public FileStorageService localFileStorageService(ErafWebProperties properties) {
        ErafWebProperties.FileUploadConfig config = properties.getFileUpload();
        return new LocalFileStorageService(
                config.getUploadPath(),
                config.getBaseUrl(),
                config.isCreateDateDirectory()
        );
    }

    // ===== Resilience 패턴 =====

    /**
     * Circuit Breaker 레지스트리
     */
    @Bean("erafCircuitBreakerRegistry")
    @ConditionalOnMissingBean(CircuitBreakerRegistry.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.circuit-breaker.enabled", havingValue = "true", matchIfMissing = true)
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return new CircuitBreakerRegistry();
    }

    /**
     * Circuit Breaker AOP Aspect
     */
    @Bean("erafCircuitBreakerAspect")
    @ConditionalOnMissingBean(CircuitBreakerAspect.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.circuit-breaker.enabled", havingValue = "true", matchIfMissing = true)
    public CircuitBreakerAspect circuitBreakerAspect(CircuitBreakerRegistry registry) {
        return new CircuitBreakerAspect(registry);
    }

    /**
     * Rate Limiter 레지스트리
     */
    @Bean("erafRateLimiterRegistry")
    @ConditionalOnMissingBean(RateLimiter.Registry.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimiter.Registry rateLimiterRegistry() {
        return new RateLimiter.Registry();
    }

    /**
     * Rate Limit AOP Aspect
     */
    @Bean("erafRateLimitAspect")
    @ConditionalOnMissingBean(RateLimitAspect.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitAspect rateLimitAspect(RateLimiter.Registry registry) {
        return new RateLimitAspect(registry);
    }

    /**
     * Timeout AOP Aspect
     */
    @Bean("erafTimeoutAspect")
    @ConditionalOnMissingBean(TimeoutAspect.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.timeout.enabled", havingValue = "true", matchIfMissing = true)
    public TimeoutAspect timeoutAspect() {
        return new TimeoutAspect();
    }

    /**
     * Bulkhead AOP Aspect
     */
    @Bean("erafBulkheadAspect")
    @ConditionalOnMissingBean(BulkheadAspect.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.bulkhead.enabled", havingValue = "true", matchIfMissing = true)
    public BulkheadAspect bulkheadAspect() {
        return new BulkheadAspect();
    }

    /**
     * Retry AOP Aspect
     */
    @Bean("erafRetryAspect")
    @ConditionalOnMissingBean(RetryAspect.class)
    @ConditionalOnProperty(name = "eraf.web.resilience.retry.enabled", havingValue = "true", matchIfMissing = true)
    public RetryAspect retryAspect() {
        return new RetryAspect();
    }
}
