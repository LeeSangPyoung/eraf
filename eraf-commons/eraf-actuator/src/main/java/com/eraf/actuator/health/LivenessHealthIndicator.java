package com.eraf.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Liveness Probe용 Health Indicator
 * 애플리케이션이 살아있는지 확인 (재시작이 필요한지)
 *
 * Liveness가 실패하면 Kubernetes가 컨테이너를 재시작
 */
public class LivenessHealthIndicator implements HealthIndicator {

    private volatile boolean alive = true;

    @Override
    public Health health() {
        if (alive) {
            return Health.up()
                    .withDetail("status", "alive")
                    .withDetail("description", "Application is running")
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "dead")
                    .withDetail("description", "Application needs restart")
                    .build();
        }
    }

    /**
     * 애플리케이션을 "살아있음" 상태로 설정
     */
    public void markAsAlive() {
        this.alive = true;
    }

    /**
     * 애플리케이션을 "죽은" 상태로 설정
     * (재시작이 필요한 심각한 오류 발생 시)
     */
    public void markAsDead() {
        this.alive = false;
    }

    /**
     * 현재 상태 조회
     */
    public boolean isAlive() {
        return alive;
    }
}
