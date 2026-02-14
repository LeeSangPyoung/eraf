package com.eraf.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Readiness Probe용 Health Indicator
 * 애플리케이션이 트래픽을 받을 준비가 되었는지 확인
 *
 * Readiness가 실패하면 Kubernetes가 트래픽을 보내지 않음
 */
public class ReadinessHealthIndicator implements HealthIndicator {

    private volatile boolean ready = false;
    private final Map<String, Boolean> dependencies = new ConcurrentHashMap<>();

    @Override
    public Health health() {
        if (!ready) {
            return Health.down()
                    .withDetail("status", "not_ready")
                    .withDetail("description", "Application is starting up")
                    .withDetail("dependencies", new HashMap<>(dependencies))
                    .build();
        }

        // 의존성 체크
        boolean allDependenciesReady = dependencies.isEmpty() ||
                dependencies.values().stream().allMatch(Boolean::booleanValue);

        if (allDependenciesReady) {
            return Health.up()
                    .withDetail("status", "ready")
                    .withDetail("description", "Application is ready to accept traffic")
                    .withDetail("dependencies", new HashMap<>(dependencies))
                    .build();
        } else {
            Map<String, Object> details = new HashMap<>();
            details.put("status", "not_ready");
            details.put("description", "Some dependencies are not ready");

            Map<String, String> depStatus = new HashMap<>();
            dependencies.forEach((key, value) ->
                depStatus.put(key, value ? "ready" : "not_ready")
            );
            details.put("dependencies", depStatus);

            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }

    /**
     * 애플리케이션을 "준비됨" 상태로 설정
     */
    public void markAsReady() {
        this.ready = true;
    }

    /**
     * 애플리케이션을 "준비 안 됨" 상태로 설정
     */
    public void markAsNotReady() {
        this.ready = false;
    }

    /**
     * 의존성 추가
     */
    public void addDependency(String name, boolean ready) {
        dependencies.put(name, ready);
    }

    /**
     * 의존성 제거
     */
    public void removeDependency(String name) {
        dependencies.remove(name);
    }

    /**
     * 의존성 상태 업데이트
     */
    public void updateDependency(String name, boolean ready) {
        dependencies.put(name, ready);
    }

    /**
     * 모든 의존성 초기화
     */
    public void clearDependencies() {
        dependencies.clear();
    }

    /**
     * 현재 준비 상태 조회
     */
    public boolean isReady() {
        return ready && (dependencies.isEmpty() ||
                dependencies.values().stream().allMatch(Boolean::booleanValue));
    }
}
