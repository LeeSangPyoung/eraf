package com.eraf.sample.controller.resilience;

import com.eraf.core.resilience.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Resilience 패턴 컨트롤러 (#21~#28)
 *
 * #21 Circuit Breaker 상태 모니터링
 * #22 Circuit Breaker 강제 OPEN/CLOSED
 * #23 Rate Limiter 설정 및 테스트
 * #24 Retry 테스트 UI
 * #25 Timeout 테스트 UI
 * #26 Bulkhead 동시 실행 제한 테스트
 * #27 복합 패턴 테스트 (Circuit Breaker + Retry)
 * #28 Resilience 메트릭 대시보드
 */
@Slf4j
@Controller
@RequestMapping("/resilience")
public class ResilienceController {

    // Circuit Breaker Registry
    private final CircuitBreakerRegistry circuitBreakerRegistry = new CircuitBreakerRegistry(3, 2, 10000);

    // Rate Limiter Registry
    private final RateLimiter.Registry rateLimiterRegistry = new RateLimiter.Registry(10.0, 15);

    // Bulkhead Semaphores (동시 실행 제한)
    private final Map<String, Semaphore> bulkheads = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> bulkheadCurrentCounts = new ConcurrentHashMap<>();

    // Metrics storage
    private final Map<String, AtomicLong> metrics = new ConcurrentHashMap<>();

    // ExecutorService for async operations
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @GetMapping
    public String resiliencePage(Model model) {
        // 기본 Circuit Breaker 생성
        circuitBreakerRegistry.getOrCreate("payment-service");
        circuitBreakerRegistry.getOrCreate("inventory-service");
        circuitBreakerRegistry.getOrCreate("notification-service");

        // 기본 Rate Limiter 생성
        rateLimiterRegistry.getOrCreate("api-gateway", 5.0, 10);
        rateLimiterRegistry.getOrCreate("user-api", 10.0, 20);

        // 기본 Bulkhead 생성
        bulkheads.computeIfAbsent("heavy-process", k -> new Semaphore(3));
        bulkheadCurrentCounts.computeIfAbsent("heavy-process", k -> new AtomicInteger(0));

        return "resilience/resilience";
    }

    // ===== #21 Circuit Breaker 상태 모니터링 =====
    @GetMapping("/circuit-breakers")
    @ResponseBody
    public Map<String, Object> getCircuitBreakers() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> breakers = new ArrayList<>();
        for (CircuitBreaker cb : circuitBreakerRegistry.getAll()) {
            breakers.add(circuitBreakerToMap(cb));
        }

        result.put("circuitBreakers", breakers);
        result.put("count", breakers.size());

        return result;
    }

    @GetMapping("/circuit-breaker/{name}")
    @ResponseBody
    public Map<String, Object> getCircuitBreaker(@PathVariable String name) {
        CircuitBreaker cb = circuitBreakerRegistry.get(name);
        if (cb == null) {
            return Map.of("error", "Circuit breaker not found: " + name);
        }
        return circuitBreakerToMap(cb);
    }

    // ===== #22 Circuit Breaker 강제 OPEN/CLOSED =====
    @PostMapping("/circuit-breaker/{name}/trip")
    @ResponseBody
    public Map<String, Object> tripCircuitBreaker(@PathVariable String name) {
        CircuitBreaker cb = circuitBreakerRegistry.getOrCreate(name);
        cb.trip();
        incrementMetric("circuit-breaker.trip");
        return circuitBreakerToMap(cb);
    }

    @PostMapping("/circuit-breaker/{name}/reset")
    @ResponseBody
    public Map<String, Object> resetCircuitBreaker(@PathVariable String name) {
        CircuitBreaker cb = circuitBreakerRegistry.getOrCreate(name);
        cb.reset();
        incrementMetric("circuit-breaker.reset");
        return circuitBreakerToMap(cb);
    }

    @PostMapping("/circuit-breaker/{name}/test")
    @ResponseBody
    public Map<String, Object> testCircuitBreaker(@PathVariable String name, @RequestBody CBTestRequest request) {
        Map<String, Object> result = new HashMap<>();
        CircuitBreaker cb = circuitBreakerRegistry.getOrCreate(name);

        if (!cb.allowRequest()) {
            result.put("allowed", false);
            result.put("message", "Circuit breaker is OPEN, request not allowed");
            result.put("state", cb.getState().name());
            incrementMetric("circuit-breaker.rejected");
            return result;
        }

        // 시뮬레이션: 성공/실패 확률에 따라 결과 생성
        boolean success = Math.random() * 100 < request.successRate();

        if (success) {
            cb.recordSuccess();
            result.put("success", true);
            result.put("message", "Request succeeded");
            incrementMetric("circuit-breaker.success");
        } else {
            cb.recordFailure();
            result.put("success", false);
            result.put("message", "Request failed");
            incrementMetric("circuit-breaker.failure");
        }

        result.put("allowed", true);
        result.put("state", cb.getState().name());
        result.put("failureCount", cb.getFailureCount());
        result.put("successCount", cb.getSuccessCount());

        return result;
    }

    // ===== #23 Rate Limiter 테스트 =====
    @GetMapping("/rate-limiters")
    @ResponseBody
    public Map<String, Object> getRateLimiters() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> limiters = new ArrayList<>();
        for (String name : List.of("api-gateway", "user-api")) {
            RateLimiter limiter = rateLimiterRegistry.get(name);
            if (limiter != null) {
                limiters.add(Map.of(
                        "name", limiter.getName(),
                        "permitsPerSecond", limiter.getPermitsPerSecond(),
                        "availablePermits", limiter.getAvailablePermits()
                ));
            }
        }

        result.put("rateLimiters", limiters);
        return result;
    }

    @PostMapping("/rate-limiter/{name}/acquire")
    @ResponseBody
    public Map<String, Object> acquireRateLimit(@PathVariable String name, @RequestBody RateLimitRequest request) {
        Map<String, Object> result = new HashMap<>();

        RateLimiter limiter = rateLimiterRegistry.getOrCreate(name, request.permitsPerSecond(), request.maxBurstSize());

        int acquired = 0;
        int rejected = 0;

        for (int i = 0; i < request.requestCount(); i++) {
            if (limiter.tryAcquire()) {
                acquired++;
                incrementMetric("rate-limit.acquired");
            } else {
                rejected++;
                incrementMetric("rate-limit.rejected");
            }
        }

        result.put("success", true);
        result.put("totalRequests", request.requestCount());
        result.put("acquired", acquired);
        result.put("rejected", rejected);
        result.put("availablePermits", limiter.getAvailablePermits());

        return result;
    }

    // ===== #24 Retry 테스트 =====
    @PostMapping("/retry/test")
    @ResponseBody
    public Map<String, Object> testRetry(@RequestBody RetryTestRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> attempts = new ArrayList<>();

        int attemptCount = 0;
        long delay = request.initialDelay();
        boolean success = false;
        Exception lastException = null;

        while (attemptCount < request.maxAttempts()) {
            attemptCount++;
            Map<String, Object> attempt = new HashMap<>();
            attempt.put("attempt", attemptCount);
            attempt.put("timestamp", System.currentTimeMillis());

            // 시뮬레이션: 성공 확률에 따라 결과 생성
            boolean thisAttemptSuccess = Math.random() * 100 < request.successRate();

            if (thisAttemptSuccess) {
                success = true;
                attempt.put("success", true);
                attempt.put("message", "Request succeeded");
                attempts.add(attempt);
                incrementMetric("retry.success");
                break;
            } else {
                attempt.put("success", false);
                attempt.put("message", "Request failed, will retry...");
                attempts.add(attempt);
                incrementMetric("retry.attempt");

                // 마지막 시도가 아니면 대기
                if (attemptCount < request.maxAttempts()) {
                    try {
                        Thread.sleep(Math.min(delay, request.maxDelay() > 0 ? request.maxDelay() : Long.MAX_VALUE));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    delay = (long) (delay * request.multiplier());
                }
            }
        }

        result.put("success", success);
        result.put("totalAttempts", attemptCount);
        result.put("maxAttempts", request.maxAttempts());
        result.put("attempts", attempts);

        if (!success) {
            result.put("finalStatus", "EXHAUSTED");
            incrementMetric("retry.exhausted");
        } else {
            result.put("finalStatus", "SUCCESS");
        }

        return result;
    }

    // ===== #25 Timeout 테스트 =====
    @PostMapping("/timeout/test")
    @ResponseBody
    public Map<String, Object> testTimeout(@RequestBody TimeoutTestRequest request) {
        Map<String, Object> result = new HashMap<>();

        long startTime = System.currentTimeMillis();
        Future<String> future = executorService.submit(() -> {
            // 시뮬레이션: 요청된 처리 시간만큼 대기
            Thread.sleep(request.processingTime());
            return "Operation completed successfully";
        });

        try {
            String response = future.get(request.timeout(), TimeUnit.MILLISECONDS);
            long elapsedTime = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("message", response);
            result.put("elapsedTime", elapsedTime);
            result.put("timeout", request.timeout());
            result.put("status", "COMPLETED");
            incrementMetric("timeout.success");

        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            long elapsedTime = System.currentTimeMillis() - startTime;

            result.put("success", false);
            result.put("message", "Operation timed out");
            result.put("elapsedTime", elapsedTime);
            result.put("timeout", request.timeout());
            result.put("status", "TIMEOUT");
            incrementMetric("timeout.exceeded");

        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            result.put("success", false);
            result.put("message", "Operation failed: " + e.getMessage());
            result.put("elapsedTime", elapsedTime);
            result.put("status", "ERROR");
            incrementMetric("timeout.error");
        }

        return result;
    }

    // ===== #26 Bulkhead 테스트 =====
    @GetMapping("/bulkheads")
    @ResponseBody
    public Map<String, Object> getBulkheads() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> bulkheadList = new ArrayList<>();
        for (Map.Entry<String, Semaphore> entry : bulkheads.entrySet()) {
            String name = entry.getKey();
            Semaphore semaphore = entry.getValue();
            AtomicInteger currentCount = bulkheadCurrentCounts.get(name);

            bulkheadList.add(Map.of(
                    "name", name,
                    "maxConcurrent", semaphore.availablePermits() + (currentCount != null ? currentCount.get() : 0),
                    "available", semaphore.availablePermits(),
                    "current", currentCount != null ? currentCount.get() : 0
            ));
        }

        result.put("bulkheads", bulkheadList);
        return result;
    }

    @PostMapping("/bulkhead/{name}/execute")
    @ResponseBody
    public Map<String, Object> executeBulkhead(@PathVariable String name, @RequestBody BulkheadRequest request) {
        Map<String, Object> result = new HashMap<>();

        Semaphore semaphore = bulkheads.computeIfAbsent(name, k -> new Semaphore(request.maxConcurrent()));
        AtomicInteger currentCount = bulkheadCurrentCounts.computeIfAbsent(name, k -> new AtomicInteger(0));

        boolean acquired = semaphore.tryAcquire();

        if (!acquired) {
            result.put("success", false);
            result.put("message", "Bulkhead full, request rejected");
            result.put("status", "REJECTED");
            result.put("currentConcurrent", currentCount.get());
            incrementMetric("bulkhead.rejected");
            return result;
        }

        currentCount.incrementAndGet();
        incrementMetric("bulkhead.acquired");

        // 비동기로 작업 실행
        executorService.submit(() -> {
            try {
                Thread.sleep(request.processingTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
                currentCount.decrementAndGet();
                incrementMetric("bulkhead.released");
            }
        });

        result.put("success", true);
        result.put("message", "Request accepted, processing in background");
        result.put("status", "PROCESSING");
        result.put("currentConcurrent", currentCount.get());
        result.put("processingTime", request.processingTime());

        return result;
    }

    // ===== #27 복합 패턴 테스트 (Circuit Breaker + Retry) =====
    @PostMapping("/combined/test")
    @ResponseBody
    public Map<String, Object> testCombinedPattern(@RequestBody CombinedTestRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> attempts = new ArrayList<>();

        CircuitBreaker cb = circuitBreakerRegistry.getOrCreate(request.circuitBreakerName());
        int attemptCount = 0;
        boolean success = false;

        while (attemptCount < request.maxRetries()) {
            attemptCount++;
            Map<String, Object> attempt = new HashMap<>();
            attempt.put("attempt", attemptCount);
            attempt.put("circuitBreakerState", cb.getState().name());

            // Circuit Breaker 확인
            if (!cb.allowRequest()) {
                attempt.put("success", false);
                attempt.put("message", "Circuit breaker OPEN, skipping request");
                attempt.put("skipped", true);
                attempts.add(attempt);
                continue;
            }

            // 요청 시뮬레이션
            boolean thisAttemptSuccess = Math.random() * 100 < request.successRate();

            if (thisAttemptSuccess) {
                cb.recordSuccess();
                success = true;
                attempt.put("success", true);
                attempt.put("message", "Request succeeded");
                attempt.put("skipped", false);
                attempts.add(attempt);
                break;
            } else {
                cb.recordFailure();
                attempt.put("success", false);
                attempt.put("message", "Request failed");
                attempt.put("skipped", false);
                attempts.add(attempt);

                // 재시도 전 대기
                if (attemptCount < request.maxRetries()) {
                    try {
                        Thread.sleep(request.retryDelay());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        result.put("success", success);
        result.put("totalAttempts", attemptCount);
        result.put("attempts", attempts);
        result.put("finalCircuitBreakerState", cb.getState().name());
        result.put("circuitBreakerFailureCount", cb.getFailureCount());

        return result;
    }

    // ===== #28 Resilience 메트릭 대시보드 =====
    @GetMapping("/metrics")
    @ResponseBody
    public Map<String, Object> getMetrics() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Long> metricsSnapshot = new HashMap<>();
        metrics.forEach((key, value) -> metricsSnapshot.put(key, value.get()));

        result.put("metrics", metricsSnapshot);

        // 요약 정보
        result.put("summary", Map.of(
                "circuitBreakerCount", circuitBreakerRegistry.size(),
                "openCircuitBreakers", circuitBreakerRegistry.getAll().stream()
                        .filter(cb -> cb.getState() == CircuitBreaker.State.OPEN).count(),
                "totalRateLimitAcquired", metrics.getOrDefault("rate-limit.acquired", new AtomicLong(0)).get(),
                "totalRateLimitRejected", metrics.getOrDefault("rate-limit.rejected", new AtomicLong(0)).get(),
                "totalRetryAttempts", metrics.getOrDefault("retry.attempt", new AtomicLong(0)).get(),
                "totalTimeouts", metrics.getOrDefault("timeout.exceeded", new AtomicLong(0)).get()
        ));

        return result;
    }

    @DeleteMapping("/metrics")
    @ResponseBody
    public Map<String, Object> resetMetrics() {
        metrics.clear();
        return Map.of("success", true, "message", "Metrics reset");
    }

    // ===== Helper Methods =====
    private Map<String, Object> circuitBreakerToMap(CircuitBreaker cb) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", cb.getName());
        map.put("state", cb.getState().name());
        map.put("failureCount", cb.getFailureCount());
        map.put("failureThreshold", cb.getFailureThreshold());
        map.put("successCount", cb.getSuccessCount());
        map.put("successThreshold", cb.getSuccessThreshold());
        map.put("openTimeoutMs", cb.getOpenTimeoutMs());
        map.put("lastFailureTime", cb.getLastFailureTime() != null ? cb.getLastFailureTime().toString() : null);
        map.put("openedAt", cb.getOpenedAt() != null ? cb.getOpenedAt().toString() : null);
        return map;
    }

    private void incrementMetric(String key) {
        metrics.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    // ===== Request DTOs =====
    record CBTestRequest(double successRate) {}
    record RateLimitRequest(int requestCount, double permitsPerSecond, long maxBurstSize) {}
    record RetryTestRequest(int maxAttempts, long initialDelay, double multiplier, long maxDelay, double successRate) {}
    record TimeoutTestRequest(long timeout, long processingTime) {}
    record BulkheadRequest(int maxConcurrent, long processingTime) {}
    record CombinedTestRequest(String circuitBreakerName, int maxRetries, long retryDelay, double successRate) {}
}
