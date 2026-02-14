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
 * Rate Limit AOP Integration Tests
 */
@SpringBootTest(classes = RateLimitAspectTest.TestConfig.class)
class RateLimitAspectTest {

    @Autowired
    private TestService testService;

    @Test
    void shouldAllowRequestsWithinLimit() {
        // when & then
        for (int i = 0; i < 3; i++) {
            assertThat(testService.limitedMethod()).isEqualTo("success");
        }
    }

    @Test
    void shouldRejectRequestsExceedingLimit() {
        // given
        for (int i = 0; i < 6; i++) {
            try {
                testService.limitedMethod2();
            } catch (RateLimitExceededException e) {
                // Expected after limit is reached
            }
        }

        // when & then
        assertThatThrownBy(() -> testService.limitedMethod2())
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void shouldUseKeyExpressionForDifferentLimits() {
        // when & then
        for (int i = 0; i < 3; i++) {
            assertThat(testService.limitedByUser("user1")).isEqualTo("success");
        }

        // Different user should have separate limit
        assertThat(testService.limitedByUser("user2")).isEqualTo("success");
    }

    @Test
    void shouldInvokeFallbackWhenLimitExceeded() {
        // given
        for (int i = 0; i < 5; i++) {
            testService.methodWithFallback();
        }

        // when
        String result = testService.methodWithFallback();

        // then
        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void shouldBlockWhenBlockingModeEnabled() throws InterruptedException {
        // given
        for (int i = 0; i < 10; i++) {
            testService.blockingMethod();
        }

        // when
        long startTime = System.currentTimeMillis();
        testService.blockingMethod();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertThat(elapsedTime).isGreaterThanOrEqualTo(80); // Should wait for permit
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public RateLimiter.Registry rateLimiterRegistry() {
            return new RateLimiter.Registry();
        }

        @Bean
        public RateLimitAspect rateLimitAspect(RateLimiter.Registry registry) {
            return new RateLimitAspect(registry);
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {
        @RateLimit(name = "testLimit", permitsPerSecond = 5, maxBurstSize = 5)
        public String limitedMethod() {
            return "success";
        }

        @RateLimit(name = "testLimit2", permitsPerSecond = 5, maxBurstSize = 5)
        public String limitedMethod2() {
            return "success";
        }

        @RateLimit(name = "userLimit", permitsPerSecond = 3, maxBurstSize = 3, key = "#userId")
        public String limitedByUser(String userId) {
            return "success";
        }

        @RateLimit(name = "fallbackLimit", permitsPerSecond = 5, maxBurstSize = 5, fallbackMethod = "fallback")
        public String methodWithFallback() {
            return "success";
        }

        @SuppressWarnings("unused")
        public String fallback() {
            return "fallback";
        }

        @RateLimit(name = "blockingLimit", permitsPerSecond = 10, maxBurstSize = 10, blocking = true)
        public String blockingMethod() {
            return "success";
        }
    }
}
