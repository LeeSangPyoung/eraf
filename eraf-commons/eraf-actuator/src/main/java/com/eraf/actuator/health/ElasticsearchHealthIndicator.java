package com.eraf.actuator.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Elasticsearch Health Indicator
 * <p>
 * Elasticsearch 클러스터의 {@code /_cluster/health} 엔드포인트를 호출하여
 * 클러스터 상태를 확인합니다.
 */
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchHealthIndicator.class);

    private final String clusterUrl;
    private final RestTemplate restTemplate;

    public ElasticsearchHealthIndicator(String clusterUrl) {
        this.clusterUrl = clusterUrl;
        this.restTemplate = new RestTemplate();
    }

    public ElasticsearchHealthIndicator(String clusterUrl, RestTemplate restTemplate) {
        this.clusterUrl = clusterUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Health health() {
        Instant start = Instant.now();
        try {
            String healthUrl = clusterUrl.endsWith("/")
                    ? clusterUrl + "_cluster/health"
                    : clusterUrl + "/_cluster/health";

            Map<String, Object> response = restTemplate.getForObject(healthUrl, Map.class);
            long responseTime = Duration.between(start, Instant.now()).toMillis();

            if (response == null) {
                return Health.down()
                        .withDetail("url", clusterUrl)
                        .withDetail("error", "Empty response")
                        .build();
            }

            String clusterStatus = (String) response.get("status");
            String clusterName = (String) response.get("cluster_name");

            Health.Builder builder = "red".equalsIgnoreCase(clusterStatus)
                    ? Health.down()
                    : Health.up();

            return builder
                    .withDetail("clusterName", clusterName)
                    .withDetail("clusterStatus", clusterStatus)
                    .withDetail("numberOfNodes", response.get("number_of_nodes"))
                    .withDetail("activeShards", response.get("active_shards"))
                    .withDetail("responseTime", responseTime + "ms")
                    .build();

        } catch (Exception e) {
            long responseTime = Duration.between(start, Instant.now()).toMillis();
            log.error("Elasticsearch health check failed: {}", e.getMessage());

            return Health.down()
                    .withDetail("url", clusterUrl)
                    .withDetail("error", e.getMessage())
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
        }
    }
}
