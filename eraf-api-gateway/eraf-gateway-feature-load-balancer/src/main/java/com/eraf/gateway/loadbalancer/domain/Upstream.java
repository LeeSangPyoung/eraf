package com.eraf.gateway.loadbalancer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Upstream configuration.
 * Represents a group of backend servers with load balancing configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Upstream {
    /**
     * Upstream name (unique identifier).
     */
    private String name;

    /**
     * Load balancing algorithm.
     */
    @Builder.Default
    private LoadBalancerAlgorithm algorithm = LoadBalancerAlgorithm.ROUND_ROBIN;

    /**
     * List of backend servers.
     */
    @Builder.Default
    private List<Server> servers = new ArrayList<>();

    /**
     * Health check configuration.
     */
    @Builder.Default
    private HealthCheckConfig healthCheck = HealthCheckConfig.builder().build();

    /**
     * Canary deployment configuration.
     * Percentage of traffic to route to canary version (0-100).
     */
    @Builder.Default
    private int canaryPercentage = 0;

    /**
     * Canary version identifier.
     */
    private String canaryVersion;

    /**
     * Enable sticky sessions (only for IP_HASH algorithm).
     */
    @Builder.Default
    private boolean stickySession = false;

    /**
     * Connection timeout.
     */
    @Builder.Default
    private int connectTimeout = 5000;

    /**
     * Read timeout.
     */
    @Builder.Default
    private int readTimeout = 30000;

    /**
     * Get all healthy servers.
     */
    public List<Server> getHealthyServers() {
        return servers.stream()
                .filter(Server::isHealthy)
                .collect(Collectors.toList());
    }

    /**
     * Get healthy servers for specific version (for canary deployment).
     */
    public List<Server> getHealthyServers(String version) {
        return servers.stream()
                .filter(Server::isHealthy)
                .filter(server -> server.matchesVersion(version))
                .collect(Collectors.toList());
    }

    /**
     * Add a server to the upstream.
     */
    public void addServer(Server server) {
        servers.add(server);
    }

    /**
     * Remove a server from the upstream.
     */
    public void removeServer(Server server) {
        servers.remove(server);
    }

    /**
     * Get total number of servers.
     */
    public int getTotalServers() {
        return servers.size();
    }

    /**
     * Get number of healthy servers.
     */
    public int getHealthyServerCount() {
        return (int) servers.stream()
                .filter(Server::isHealthy)
                .count();
    }

    /**
     * Get total active connections across all servers.
     */
    public int getTotalActiveConnections() {
        return servers.stream()
                .mapToInt(server -> server.getActiveConnections().get())
                .sum();
    }

    /**
     * Get total requests across all servers.
     */
    public long getTotalRequests() {
        return servers.stream()
                .mapToLong(server -> server.getTotalRequests().get())
                .sum();
    }

    /**
     * Get total failed requests across all servers.
     */
    public long getTotalFailedRequests() {
        return servers.stream()
                .mapToLong(server -> server.getFailedRequests().get())
                .sum();
    }

    /**
     * Get overall failure rate.
     */
    public double getOverallFailureRate() {
        long total = getTotalRequests();
        if (total == 0) {
            return 0.0;
        }
        return (double) getTotalFailedRequests() / total;
    }

    /**
     * Check if canary deployment is enabled.
     */
    public boolean isCanaryEnabled() {
        return canaryPercentage > 0 && canaryVersion != null;
    }
}
