package com.eraf.starter.actuator.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.concurrent.TimeUnit;

/**
 * Kafka 상태 확인 Health Indicator
 */
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;
    private final long timeoutMs;

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this(kafkaAdmin, 5000L);
    }

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin, long timeoutMs) {
        this.kafkaAdmin = kafkaAdmin;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();

            String clusterId = clusterResult.clusterId().get(timeoutMs, TimeUnit.MILLISECONDS);
            int nodeCount = clusterResult.nodes().get(timeoutMs, TimeUnit.MILLISECONDS).size();

            return Health.up()
                    .withDetail("kafka", "connected")
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("kafka", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
