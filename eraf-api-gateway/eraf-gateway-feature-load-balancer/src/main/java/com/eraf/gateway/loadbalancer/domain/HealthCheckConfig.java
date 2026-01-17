package com.eraf.gateway.loadbalancer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Health check configuration for upstream servers.
 * Defines how and when to check server health.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckConfig {
    /**
     * Enable active health checks.
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Health check interval.
     */
    @Builder.Default
    private Duration interval = Duration.ofSeconds(10);

    /**
     * Health check timeout.
     */
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(5);

    /**
     * HTTP path to check (for HTTP health checks).
     */
    @Builder.Default
    private String path = "/health";

    /**
     * Expected HTTP status codes for healthy response.
     */
    @Builder.Default
    private int[] expectedStatuses = {200, 204};

    /**
     * Number of consecutive successful checks required to mark server as healthy.
     */
    @Builder.Default
    private int healthyThreshold = 2;

    /**
     * Number of consecutive failed checks required to mark server as unhealthy.
     */
    @Builder.Default
    private int unhealthyThreshold = 3;

    /**
     * Enable passive health checks (based on actual traffic).
     */
    @Builder.Default
    private boolean passiveEnabled = true;

    /**
     * Number of consecutive failures in actual traffic to mark server as unhealthy.
     */
    @Builder.Default
    private int passiveUnhealthyThreshold = 5;

    /**
     * Time window for passive health checks.
     */
    @Builder.Default
    private Duration passiveWindow = Duration.ofMinutes(1);

    /**
     * Check if given HTTP status code is expected.
     */
    public boolean isExpectedStatus(int status) {
        for (int expectedStatus : expectedStatuses) {
            if (expectedStatus == status) {
                return true;
            }
        }
        return false;
    }
}
