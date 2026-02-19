package com.eraf.actuator.health;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * RabbitMQ 상태 확인 Health Indicator
 */
public class RabbitMQHealthIndicator implements HealthIndicator {

    private final ConnectionFactory connectionFactory;

    public RabbitMQHealthIndicator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (Connection connection = connectionFactory.createConnection()) {
            if (connection.isOpen()) {
                return Health.up()
                        .withDetail("rabbitmq", "connected")
                        .withDetail("host", connectionFactory.getHost())
                        .withDetail("port", connectionFactory.getPort())
                        .build();
            } else {
                return Health.down()
                        .withDetail("rabbitmq", "connection not open")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("rabbitmq", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
