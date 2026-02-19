package com.eraf.resilience;

import com.eraf.idempotent.IdempotencyStore;
import com.eraf.idempotent.IdempotentAspect;
import com.eraf.idempotent.InMemoryIdempotencyStore;
import com.eraf.lock.DistributedLockAspect;
import com.eraf.lock.InMemoryLockProvider;
import com.eraf.lock.LockProvider;
import com.eraf.lock.OptimisticRetryAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * ERAF Resilience Auto-Configuration
 *
 * <p>Circuit Breaker, Retry, Timeout, Rate Limit, Bulkhead, Idempotent, Distributed Lock 기능을 자동 구성합니다.</p>
 *
 * <p>활성화 조건:</p>
 * <ul>
 *   <li>AspectJ가 classpath에 있을 때 자동 활성화</li>
 *   <li>각 Aspect는 해당 어노테이션 사용 시 동작</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @CircuitBreaker}(name = "userService", failureThreshold = 5, successThreshold = 2)
 * public User getUser(Long id) {
 *     // ...
 * }
 *
 * {@code @Retry}(maxAttempts = 3, delay = 1000)
 * public Data fetchData() {
 *     // ...
 * }
 *
 * {@code @Timeout}(value = 5000)
 * public Response callExternalApi() {
 *     // ...
 * }
 * </pre>
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@EnableAspectJAutoProxy
public class ErafResilienceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafResilienceAutoConfiguration.class);

    /**
     * Circuit Breaker Registry 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        log.debug("Registering CircuitBreakerRegistry");
        return new CircuitBreakerRegistry();
    }

    /**
     * Circuit Breaker Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerAspect circuitBreakerAspect(CircuitBreakerRegistry registry) {
        log.info("Registering CircuitBreakerAspect - @CircuitBreaker annotation support enabled");
        return new CircuitBreakerAspect(registry);
    }

    /**
     * Retry Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryAspect retryAspect() {
        log.info("Registering RetryAspect - @Retry annotation support enabled");
        return new RetryAspect();
    }

    /**
     * Timeout Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public TimeoutAspect timeoutAspect() {
        log.info("Registering TimeoutAspect - @Timeout annotation support enabled");
        return new TimeoutAspect();
    }

    /**
     * Rate Limiter Registry 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiter.Registry rateLimiterRegistry() {
        log.debug("Registering RateLimiter.Registry");
        return new RateLimiter.Registry();
    }

    /**
     * Rate Limit Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimiter.Registry registry) {
        log.info("Registering RateLimitAspect - @RateLimit annotation support enabled");
        return new RateLimitAspect(registry);
    }

    /**
     * Bulkhead Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public BulkheadAspect bulkheadAspect() {
        log.info("Registering BulkheadAspect - @Bulkhead annotation support enabled");
        return new BulkheadAspect();
    }

    /**
     * In-Memory Idempotency Store 빈 등록 (기본 구현)
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore() {
        log.debug("Registering InMemoryIdempotencyStore (default implementation)");
        return new InMemoryIdempotencyStore();
    }

    /**
     * Idempotent Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect(IdempotencyStore store) {
        log.info("Registering IdempotentAspect - @Idempotent annotation support enabled");
        return new IdempotentAspect(store);
    }

    /**
     * In-Memory Lock Provider 빈 등록 (기본 구현)
     */
    @Bean
    @ConditionalOnMissingBean
    public LockProvider lockProvider() {
        log.debug("Registering InMemoryLockProvider (default implementation)");
        return new InMemoryLockProvider();
    }

    /**
     * Distributed Lock Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect(LockProvider lockProvider) {
        log.info("Registering DistributedLockAspect - @DistributedLock annotation support enabled");
        return new DistributedLockAspect(lockProvider);
    }

    /**
     * Optimistic Retry Aspect 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public OptimisticRetryAspect optimisticRetryAspect() {
        log.info("Registering OptimisticRetryAspect - @OptimisticRetry annotation support enabled");
        return new OptimisticRetryAspect();
    }
}
