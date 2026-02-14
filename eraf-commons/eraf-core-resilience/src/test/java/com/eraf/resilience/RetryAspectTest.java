package com.eraf.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Retry AOP Integration Tests
 */
@SpringBootTest(classes = RetryAspectTest.TestConfig.class)
class RetryAspectTest {

    @Autowired
    private TestService testService;

    @Test
    void shouldSucceedOnFirstAttempt() {
        // given
        testService.setFailUntilAttempt(1);

        // when
        String result = testService.successMethod();

        // then
        assertThat(result).isEqualTo("success");
        assertThat(testService.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldRetryOnFailureAndEventuallySucceed() {
        // given
        testService.setFailUntilAttempt(3);

        // when
        String result = testService.retryMethod();

        // then
        assertThat(result).isEqualTo("success");
        assertThat(testService.getAttemptCount()).isEqualTo(3);
    }

    @Test
    void shouldThrowRetryExhaustedExceptionWhenMaxAttemptsExceeded() {
        // given
        testService.setFailUntilAttempt(10);

        // when & then
        assertThatThrownBy(() -> testService.retryMethod())
                .isInstanceOf(RetryExhaustedException.class)
                .hasMessageContaining("3");
    }

    @Test
    void shouldApplyExponentialBackoff() {
        // given
        testService.setFailUntilAttempt(3);

        // when
        long startTime = System.currentTimeMillis();
        testService.exponentialBackoffMethod();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        // 100ms * 1 + 100ms * 2 = 300ms minimum
        assertThat(elapsedTime).isGreaterThanOrEqualTo(250);
        assertThat(testService.getAttemptCount()).isEqualTo(3);
    }

    @Test
    void shouldRespectMaxDelay() {
        // given
        testService.setFailUntilAttempt(3);

        // when
        long startTime = System.currentTimeMillis();
        testService.maxDelayMethod();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        // Max delay is 150ms per retry, so total should be around 300ms
        assertThat(elapsedTime).isLessThan(400);
    }

    @Test
    void shouldOnlyRetrySpecificExceptions() {
        // given
        testService.setFailUntilAttempt(10);

        // when & then
        assertThatThrownBy(() -> testService.retryOnSpecificException())
                .isInstanceOf(RetryExhaustedException.class);

        // Should retry 3 times
        assertThat(testService.getAttemptCount()).isEqualTo(3);
    }

    @Test
    void shouldNotRetryIgnoredExceptions() {
        // given
        testService.setFailUntilAttempt(0);

        // when & then
        assertThatThrownBy(() -> testService.ignoreSpecificException())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ignored exception");

        // Attempt count should be 1 (no retry)
        assertThat(testService.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldInvokeFallbackWhenRetriesExhausted() {
        // given
        testService.setFailUntilAttempt(10);

        // when
        String result = testService.methodWithFallback();

        // then
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void shouldInvokeFallbackWithThrowableParameter() {
        // given
        testService.setFailUntilAttempt(10);

        // when
        String result = testService.methodWithFallbackAndThrowable();

        // then
        assertThat(result).startsWith("fallback with cause:");
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public RetryAspect retryAspect() {
            return new RetryAspect();
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private int failUntilAttempt = 0;

        public void setFailUntilAttempt(int failUntilAttempt) {
            this.failUntilAttempt = failUntilAttempt;
            this.attemptCount.set(0);
        }

        public int getAttemptCount() {
            return attemptCount.get();
        }

        @Retry(maxAttempts = 1)
        public String successMethod() {
            attemptCount.incrementAndGet();
            return "success";
        }

        @Retry(maxAttempts = 3, delay = 10)
        public String retryMethod() {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < failUntilAttempt) {
                throw new RuntimeException("Attempt " + attempt + " failed");
            }
            return "success";
        }

        @Retry(maxAttempts = 3, delay = 100, multiplier = 2.0)
        public String exponentialBackoffMethod() {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < failUntilAttempt) {
                throw new RuntimeException("Attempt " + attempt + " failed");
            }
            return "success";
        }

        @Retry(maxAttempts = 3, delay = 100, multiplier = 2.0, maxDelay = 150)
        public String maxDelayMethod() {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < failUntilAttempt) {
                throw new RuntimeException("Attempt " + attempt + " failed");
            }
            return "success";
        }

        @Retry(maxAttempts = 3, delay = 10, retryOn = {RuntimeException.class})
        public String retryOnSpecificException() {
            attemptCount.incrementAndGet();
            throw new RuntimeException("Retryable");
        }

        @Retry(maxAttempts = 3, delay = 10, ignoreOn = {IllegalArgumentException.class})
        public String ignoreSpecificException() {
            attemptCount.incrementAndGet();
            throw new IllegalArgumentException("Ignored exception");
        }

        @Retry(maxAttempts = 3, delay = 10, fallbackMethod = "fallback")
        public String methodWithFallback() {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < failUntilAttempt) {
                throw new RuntimeException("Attempt " + attempt + " failed");
            }
            return "success";
        }

        @SuppressWarnings("unused")
        public String fallback() {
            return "fallback";
        }

        @Retry(maxAttempts = 3, delay = 10, fallbackMethod = "fallbackWithThrowable")
        public String methodWithFallbackAndThrowable() {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < failUntilAttempt) {
                throw new RuntimeException("Attempt " + attempt + " failed");
            }
            return "success";
        }

        @SuppressWarnings("unused")
        public String fallbackWithThrowable(Throwable t) {
            return "fallback with cause: " + t.getMessage();
        }
    }
}
