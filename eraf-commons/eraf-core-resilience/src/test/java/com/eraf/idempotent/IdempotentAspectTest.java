package com.eraf.idempotent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Idempotent AOP Integration Tests
 */
@SpringBootTest(classes = IdempotentAspectTest.TestConfig.class)
class IdempotentAspectTest {

    @Autowired
    private TestService testService;

    @Test
    void shouldExecuteMethodOnFirstCall() {
        // when
        String result = testService.idempotentMethod("param1");

        // then
        assertThat(result).isEqualTo("executed");
        assertThat(testService.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void shouldReturnCachedResultOnDuplicateCall() {
        // given
        testService.idempotentMethod("param1");
        int firstCount = testService.getExecutionCount();

        // when
        String result = testService.idempotentMethod("param1");

        // then
        assertThat(result).isEqualTo("executed");
        assertThat(testService.getExecutionCount()).isEqualTo(firstCount); // No new execution
    }

    @Test
    void shouldExecuteAgainForDifferentParameters() {
        // given
        testService.idempotentMethod("param1");
        int firstCount = testService.getExecutionCount();

        // when
        String result = testService.idempotentMethod("param2");

        // then
        assertThat(result).isEqualTo("executed");
        assertThat(testService.getExecutionCount()).isGreaterThan(firstCount);
    }

    @Test
    void shouldThrowExceptionWhenThrowOnDuplicateEnabled() {
        // given
        testService.throwOnDuplicateMethod("param1");

        // when & then
        assertThatThrownBy(() -> testService.throwOnDuplicateMethod("param1"))
                .isInstanceOf(IdempotentException.class)
                .hasMessageContaining("중복된 요청입니다");
    }

    @Test
    void shouldUseCustomKeyExpression() {
        // given
        testService.customKeyMethod("user1", "order1");
        int firstCount = testService.getExecutionCount();

        // when
        String result = testService.customKeyMethod("user1", "order1");

        // then
        assertThat(result).isEqualTo("executed");
        assertThat(testService.getExecutionCount()).isEqualTo(firstCount); // No new execution
    }

    @Test
    void shouldAllowRetryAfterFailure() {
        // given
        try {
            testService.failingMethod("param1");
        } catch (RuntimeException e) {
            // Expected failure
        }
        int countAfterFailure = testService.getExecutionCount();

        // when
        String result = testService.failingMethod("param1");

        // then
        assertThat(result).isEqualTo("executed");
        assertThat(testService.getExecutionCount()).isGreaterThan(countAfterFailure);
    }

    @Test
    void shouldExpireAfterTimeout() throws InterruptedException {
        // given
        testService.shortTimeoutMethod("param1");
        int firstCount = testService.getExecutionCount();

        // when
        Thread.sleep(150);
        testService.shortTimeoutMethod("param1");

        // then
        assertThat(testService.getExecutionCount()).isEqualTo(firstCount + 1); // Executed again after expiry
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public IdempotencyStore idempotencyStore() {
            return new InMemoryIdempotencyStore();
        }

        @Bean
        public IdempotentAspect idempotentAspect(IdempotencyStore store) {
            return new IdempotentAspect(store);
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {
        private final AtomicInteger executionCount = new AtomicInteger(0);
        private boolean shouldFail = true;

        public int getExecutionCount() {
            return executionCount.get();
        }

        @Idempotent(timeout = 1, timeUnit = TimeUnit.HOURS)
        public String idempotentMethod(String param) {
            executionCount.incrementAndGet();
            return "executed";
        }

        @Idempotent(timeout = 1, timeUnit = TimeUnit.HOURS, throwOnDuplicate = true)
        public String throwOnDuplicateMethod(String param) {
            executionCount.incrementAndGet();
            return "executed";
        }

        @Idempotent(key = "#userId + ':' + #orderId", timeout = 1, timeUnit = TimeUnit.HOURS)
        public String customKeyMethod(String userId, String orderId) {
            executionCount.incrementAndGet();
            return "executed";
        }

        @Idempotent(timeout = 1, timeUnit = TimeUnit.HOURS)
        public String failingMethod(String param) {
            executionCount.incrementAndGet();
            if (shouldFail) {
                shouldFail = false;
                throw new RuntimeException("Test failure");
            }
            return "executed";
        }

        @Idempotent(timeout = 100, timeUnit = TimeUnit.MILLISECONDS)
        public String shortTimeoutMethod(String param) {
            executionCount.incrementAndGet();
            return "executed";
        }
    }
}
