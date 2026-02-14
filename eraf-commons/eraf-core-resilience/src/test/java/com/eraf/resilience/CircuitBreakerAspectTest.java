package com.eraf.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.*;

/**
 * Circuit Breaker AOP Integration Tests
 */
@SpringBootTest(classes = CircuitBreakerAspectTest.TestConfig.class)
class CircuitBreakerAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private CircuitBreakerRegistry registry;

    @Test
    void shouldOpenCircuitWhenFailureThresholdExceeded() {
        // given
        int failureThreshold = 3;

        // when
        for (int i = 0; i < failureThreshold; i++) {
            assertThatThrownBy(() -> testService.failingMethod())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test failure");
        }

        // then
        assertThatThrownBy(() -> testService.failingMethod())
                .isInstanceOf(CircuitBreakerOpenException.class)
                .hasMessageContaining("testCB");
    }

    @Test
    void shouldRecordSuccessOnNormalExecution() {
        // when
        String result = testService.successMethod();

        // then
        assertThat(result).isEqualTo("success");
        CircuitBreaker cb = registry.get("successCB");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldInvokeFallbackWhenCircuitOpen() {
        // given
        for (int i = 0; i < 3; i++) {
            try {
                testService.methodWithFallback();
            } catch (Exception e) {
                // ignore
            }
        }

        // when
        String result = testService.methodWithFallback();

        // then
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void shouldInvokeFallbackWithThrowableParameter() {
        // given
        for (int i = 0; i < 3; i++) {
            try {
                testService.methodWithFallbackAndThrowable();
            } catch (Exception e) {
                // ignore
            }
        }

        // when
        String result = testService.methodWithFallbackAndThrowable();

        // then
        assertThat(result).startsWith("fallback with cause:");
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public CircuitBreakerRegistry circuitBreakerRegistry() {
            return new CircuitBreakerRegistry();
        }

        @Bean
        public CircuitBreakerAspect circuitBreakerAspect(CircuitBreakerRegistry registry) {
            return new CircuitBreakerAspect(registry);
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {
        @EnableCircuitBreaker(name = "testCB", failureThreshold = 3, openTimeoutMs = 5000)
        public String failingMethod() {
            throw new RuntimeException("Test failure");
        }

        @EnableCircuitBreaker(name = "successCB", failureThreshold = 3)
        public String successMethod() {
            return "success";
        }

        @EnableCircuitBreaker(name = "fallbackCB", failureThreshold = 3, fallbackMethod = "fallback")
        public String methodWithFallback() {
            throw new RuntimeException("Test failure");
        }

        @SuppressWarnings("unused")
        public String fallback() {
            return "fallback";
        }

        @EnableCircuitBreaker(name = "fallbackWithThrowableCB", failureThreshold = 3, fallbackMethod = "fallbackWithThrowable")
        public String methodWithFallbackAndThrowable() {
            throw new RuntimeException("Test failure");
        }

        @SuppressWarnings("unused")
        public String fallbackWithThrowable(Throwable t) {
            return "fallback with cause: " + t.getMessage();
        }
    }
}
