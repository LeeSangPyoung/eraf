package com.eraf.starter.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Redis 상태 확인 Health Indicator
 */
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("redis", "connected")
                        .withDetail("ping", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "unexpected response")
                        .withDetail("ping", pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
