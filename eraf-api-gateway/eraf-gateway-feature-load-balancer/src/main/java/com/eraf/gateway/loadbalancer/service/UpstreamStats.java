package com.eraf.gateway.loadbalancer.service;

import com.eraf.gateway.loadbalancer.domain.LoadBalancerAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statistics for an upstream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstreamStats {
    /**
     * Upstream name.
     */
    private String name;

    /**
     * Load balancing algorithm.
     */
    private LoadBalancerAlgorithm algorithm;

    /**
     * Total number of servers.
     */
    private int totalServers;

    /**
     * Number of healthy servers.
     */
    private int healthyServers;

    /**
     * Total active connections.
     */
    private int totalActiveConnections;

    /**
     * Total requests handled.
     */
    private long totalRequests;

    /**
     * Total failed requests.
     */
    private long totalFailedRequests;

    /**
     * Overall failure rate (0.0 to 1.0).
     */
    private double overallFailureRate;

    /**
     * Whether canary deployment is enabled.
     */
    private boolean canaryEnabled;

    /**
     * Canary traffic percentage.
     */
    private int canaryPercentage;

    /**
     * Get success rate percentage.
     */
    public double getSuccessRatePercentage() {
        return (1.0 - overallFailureRate) * 100.0;
    }

    /**
     * Get failure rate percentage.
     */
    public double getFailureRatePercentage() {
        return overallFailureRate * 100.0;
    }

    /**
     * Get health percentage (healthy servers / total servers).
     */
    public double getHealthPercentage() {
        if (totalServers == 0) {
            return 0.0;
        }
        return ((double) healthyServers / totalServers) * 100.0;
    }
}
