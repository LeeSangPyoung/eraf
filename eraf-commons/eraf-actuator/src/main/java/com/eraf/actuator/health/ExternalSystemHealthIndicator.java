package com.eraf.actuator.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 기반 외부 시스템 Health Indicator
 * REST API로 외부 시스템의 상태를 확인
 */
public class ExternalSystemHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ExternalSystemHealthIndicator.class);

    private final String systemName;
    private final String healthCheckUrl;
    private final RestTemplate restTemplate;
    private final long timeoutMs;
    private final HttpMethod method;

    public ExternalSystemHealthIndicator(String systemName, String healthCheckUrl) {
        this(systemName, healthCheckUrl, new RestTemplate(), 5000, HttpMethod.GET);
    }

    public ExternalSystemHealthIndicator(String systemName,
                                          String healthCheckUrl,
                                          RestTemplate restTemplate,
                                          long timeoutMs,
                                          HttpMethod method) {
        this.systemName = systemName;
        this.healthCheckUrl = healthCheckUrl;
        this.restTemplate = restTemplate;
        this.timeoutMs = timeoutMs;
        this.method = method;
    }

    @Override
    public Health health() {
        Instant startTime = Instant.now();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    healthCheckUrl,
                    method,
                    null,
                    String.class
            );

            long responseTime = Duration.between(startTime, Instant.now()).toMillis();

            Map<String, Object> details = new HashMap<>();
            details.put("system", systemName);
            details.put("url", healthCheckUrl);
            details.put("responseTime", responseTime + "ms");
            details.put("status", response.getStatusCode().value());
            details.put("checkedAt", startTime);

            if (response.getStatusCode() == HttpStatus.OK ||
                response.getStatusCode() == HttpStatus.NO_CONTENT) {

                if (responseTime > timeoutMs) {
                    log.warn("{} health check slow: {}ms (timeout: {}ms)",
                            systemName, responseTime, timeoutMs);
                    return Health.up()
                            .withDetails(details)
                            .withDetail("warning", "Slow response")
                            .build();
                }

                return Health.up()
                        .withDetails(details)
                        .build();

            } else {
                details.put("error", "Unexpected status code: " + response.getStatusCode());
                return Health.down()
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            long responseTime = Duration.between(startTime, Instant.now()).toMillis();

            log.error("{} health check failed: {}", systemName, e.getMessage());

            Map<String, Object> details = new HashMap<>();
            details.put("system", systemName);
            details.put("url", healthCheckUrl);
            details.put("responseTime", responseTime + "ms");
            details.put("error", e.getClass().getSimpleName());
            details.put("message", e.getMessage());
            details.put("checkedAt", startTime);

            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }

    public String getSystemName() {
        return systemName;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }
}
