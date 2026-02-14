package com.eraf.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Bulkhead AOP Integration Tests
 */
@SpringBootTest(classes = BulkheadAspectTest.TestConfig.class)
class BulkheadAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private BulkheadAspect bulkheadAspect;

    @Test
    void shouldAllowExecutionWithinConcurrencyLimit() {
        // when
        String result = testService.bulkheadMethod();

        // then
        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldRejectExecutionWhenBulkheadFull() throws InterruptedException {
        // given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch finishLatch = new CountDownLatch(2);

        // Start 2 threads to fill the bulkhead (maxConcurrent = 2)
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    testService.limitedBulkhead();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }

        readyLatch.await();
        startLatch.countDown();
        Thread.sleep(50); // Let threads acquire permits

        // when & then
        assertThatThrownBy(() -> testService.limitedBulkhead())
                .isInstanceOf(BulkheadFullException.class)
                .hasMessageContaining("testBulkhead");

        finishLatch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void shouldLimitConcurrentExecutions() throws InterruptedException {
        // given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(5);
        AtomicInteger concurrentExecutions = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        // when
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    testService.trackConcurrencyMethod(concurrentExecutions, maxConcurrent);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        finishLatch.await(5, TimeUnit.SECONDS);

        // then
        assertThat(maxConcurrent.get()).isLessThanOrEqualTo(2); // Max concurrent should not exceed 2
    }

    @Test
    void shouldInvokeFallbackWhenBulkheadFull() throws InterruptedException {
        // given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch finishLatch = new CountDownLatch(2);

        // Fill the bulkhead
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    testService.bulkheadWithFallback();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }

        readyLatch.await();
        startLatch.countDown();
        Thread.sleep(50);

        // when
        String result = testService.bulkheadWithFallback();

        // then
        assertThat(result).isEqualTo("fallback");

        finishLatch.await(2, TimeUnit.SECONDS);
    }

    @Test
    void shouldWaitForPermitsWhenMaxWaitConfigured() {
        // when
        long startTime = System.currentTimeMillis();
        String result = testService.bulkheadWithWait();
        long elapsedTime = System.currentTimeMillis() - startTime;

        // then
        assertThat(result).isEqualTo("success");
        assertThat(elapsedTime).isLessThan(100); // Should not wait as bulkhead is empty
    }

    @Test
    void shouldCheckAvailablePermits() {
        // when
        int availablePermits = bulkheadAspect.getAvailablePermits("testBulkhead");

        // then
        assertThat(availablePermits).isEqualTo(2); // maxConcurrent = 2
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        public BulkheadAspect bulkheadAspect() {
            return new BulkheadAspect();
        }

        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {
        @Bulkhead(name = "simpleBulkhead", maxConcurrent = 10)
        public String bulkheadMethod() {
            return "success";
        }

        @Bulkhead(name = "testBulkhead", maxConcurrent = 2)
        public void limitedBulkhead() throws InterruptedException {
            Thread.sleep(200);
        }

        @Bulkhead(name = "trackBulkhead", maxConcurrent = 2)
        public void trackConcurrencyMethod(AtomicInteger concurrentExecutions, AtomicInteger maxConcurrent) throws InterruptedException {
            int current = concurrentExecutions.incrementAndGet();
            maxConcurrent.updateAndGet(max -> Math.max(max, current));
            Thread.sleep(100);
            concurrentExecutions.decrementAndGet();
        }

        @Bulkhead(name = "fallbackBulkhead", maxConcurrent = 2, fallbackMethod = "fallback")
        public String bulkheadWithFallback() throws InterruptedException {
            Thread.sleep(200);
            return "success";
        }

        @SuppressWarnings("unused")
        public String fallback() {
            return "fallback";
        }

        @Bulkhead(name = "waitBulkhead", maxConcurrent = 10, maxWait = 5, waitTimeoutMs = 1000)
        public String bulkheadWithWait() {
            return "success";
        }
    }
}
