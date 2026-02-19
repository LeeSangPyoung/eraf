package com.eraf.actuator.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import org.springframework.beans.factory.DisposableBean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Health Indicator 레지스트리
 * 여러 Health Indicator를 등록하고 관리
 */
public class HealthCheckRegistry implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckRegistry.class);

    private final Map<String, CustomHealthIndicator> indicators = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Health Indicator 등록
     */
    public void register(String name, CustomHealthIndicator indicator) {
        indicators.put(name, indicator);
        log.info("Health indicator registered: {}", name);
    }

    /**
     * Health Indicator 제거
     */
    public void unregister(String name) {
        indicators.remove(name);
        log.info("Health indicator unregistered: {}", name);
    }

    /**
     * 모든 Health Indicator 조회
     */
    public Map<String, CustomHealthIndicator> getIndicators() {
        return new LinkedHashMap<>(indicators);
    }

    /**
     * 전체 Health Check 수행
     */
    public Health checkAll() {
        Map<String, Health> results = new LinkedHashMap<>();
        boolean allUp = true;

        for (Map.Entry<String, CustomHealthIndicator> entry : indicators.entrySet()) {
            String name = entry.getKey();
            CustomHealthIndicator indicator = entry.getValue();

            try {
                Health health = checkWithTimeout(indicator);
                results.put(name, health);

                if (indicator.isCritical() && health.getStatus() == Status.DOWN) {
                    allUp = false;
                }
            } catch (Exception e) {
                log.error("Health check failed for {}: {}", name, e.getMessage());
                Health errorHealth = Health.down()
                        .withDetail("error", e.getClass().getSimpleName())
                        .withDetail("message", e.getMessage())
                        .build();
                results.put(name, errorHealth);

                if (indicator.isCritical()) {
                    allUp = false;
                }
            }
        }

        Health.Builder builder = allUp ? Health.up() : Health.down();
        results.forEach(builder::withDetail);

        return builder.build();
    }

    /**
     * Liveness Health Check
     */
    public Health checkLiveness() {
        Map<String, Health> results = indicators.entrySet().stream()
                .filter(entry -> entry.getValue().includeInLiveness())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return checkWithTimeout(entry.getValue());
                            } catch (Exception e) {
                                return Health.down()
                                        .withDetail("error", e.getMessage())
                                        .build();
                            }
                        },
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        boolean allUp = results.values().stream()
                .allMatch(h -> h.getStatus() == Status.UP);

        Health.Builder builder = allUp ? Health.up() : Health.down();
        results.forEach(builder::withDetail);

        return builder.build();
    }

    /**
     * Readiness Health Check
     */
    public Health checkReadiness() {
        Map<String, Health> results = indicators.entrySet().stream()
                .filter(entry -> entry.getValue().includeInReadiness())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return checkWithTimeout(entry.getValue());
                            } catch (Exception e) {
                                return Health.down()
                                        .withDetail("error", e.getMessage())
                                        .build();
                            }
                        },
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        boolean allUp = results.values().stream()
                .allMatch(h -> h.getStatus() == Status.UP);

        Health.Builder builder = allUp ? Health.up() : Health.down();
        results.forEach(builder::withDetail);

        return builder.build();
    }

    /**
     * 타임아웃을 적용한 Health Check
     */
    private Health checkWithTimeout(CustomHealthIndicator indicator) {
        Future<Health> future = executor.submit(indicator::check);

        try {
            return future.get(indicator.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            log.warn("Health check timeout for {}: {}ms",
                    indicator.getName(), indicator.getTimeoutMs());
            return Health.down()
                    .withDetail("error", "Timeout")
                    .withDetail("timeout", indicator.getTimeoutMs() + "ms")
                    .build();
        } catch (Exception e) {
            log.error("Health check error for {}: {}",
                    indicator.getName(), e.getMessage());
            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }

    /**
     * 특정 Health Indicator 조회
     */
    public CustomHealthIndicator getIndicator(String name) {
        return indicators.get(name);
    }

    /**
     * Health Indicator 존재 여부
     */
    public boolean hasIndicator(String name) {
        return indicators.containsKey(name);
    }

    /**
     * 등록된 Health Indicator 개수
     */
    public int size() {
        return indicators.size();
    }

    @Override
    public void destroy() {
        shutdown();
    }

    /**
     * 레지스트리 종료 (ExecutorService 정리)
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
