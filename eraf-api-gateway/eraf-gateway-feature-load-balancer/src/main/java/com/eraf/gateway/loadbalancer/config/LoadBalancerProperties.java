package com.eraf.gateway.loadbalancer.config;

import com.eraf.gateway.loadbalancer.domain.LoadBalancerAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for load balancer.
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.load-balancer")
public class LoadBalancerProperties {
    /**
     * Enable load balancer.
     */
    private boolean enabled = true;

    /**
     * Default load balancing algorithm.
     */
    private LoadBalancerAlgorithm defaultAlgorithm = LoadBalancerAlgorithm.ROUND_ROBIN;

    /**
     * Health check configuration.
     */
    private HealthCheckProperties healthCheck = new HealthCheckProperties();

    /**
     * Canary deployment configuration.
     */
    private CanaryProperties canary = new CanaryProperties();

    /**
     * Path patterns to exclude from load balancing.
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 30000;

    /**
     * Health check properties.
     */
    @Data
    public static class HealthCheckProperties {
        /**
         * Enable active health checks.
         */
        private boolean enabled = true;

        /**
         * Health check interval.
         */
        private Duration interval = Duration.ofSeconds(10);

        /**
         * Health check timeout.
         */
        private Duration timeout = Duration.ofSeconds(5);

        /**
         * Health check path.
         */
        private String path = "/health";

        /**
         * Expected HTTP status codes.
         */
        private int[] expectedStatuses = {200, 204};

        /**
         * Healthy threshold.
         */
        private int healthyThreshold = 2;

        /**
         * Unhealthy threshold.
         */
        private int unhealthyThreshold = 3;

        /**
         * Enable passive health checks.
         */
        private boolean passiveEnabled = true;

        /**
         * Passive unhealthy threshold.
         */
        private int passiveUnhealthyThreshold = 5;

        /**
         * Passive check time window.
         */
        private Duration passiveWindow = Duration.ofMinutes(1);
    }

    /**
     * Canary deployment properties.
     */
    @Data
    public static class CanaryProperties {
        /**
         * Enable canary deployment.
         */
        private boolean enabled = false;

        /**
         * Default canary traffic percentage (0-100).
         */
        private int defaultPercentage = 10;

        /**
         * Canary version header name.
         */
        private String versionHeader = "X-Canary-Version";
    }
}
