package com.eraf.gateway.loadbalancer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Backend server in an upstream.
 * Represents a single server instance that can handle requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Server {
    /**
     * Server host (IP or hostname).
     */
    private String host;

    /**
     * Server port.
     */
    private Integer port;

    /**
     * Server weight for weighted load balancing.
     * Higher weight means more requests.
     */
    @Builder.Default
    private int weight = 1;

    /**
     * Current health status.
     */
    @Builder.Default
    private volatile boolean healthy = true;

    /**
     * Number of active connections.
     */
    @Builder.Default
    private AtomicInteger activeConnections = new AtomicInteger(0);

    /**
     * Total number of requests handled.
     */
    @Builder.Default
    private AtomicLong totalRequests = new AtomicLong(0);

    /**
     * Total number of failed requests.
     */
    @Builder.Default
    private AtomicLong failedRequests = new AtomicLong(0);

    /**
     * Consecutive health check successes.
     */
    @Builder.Default
    private AtomicInteger consecutiveSuccesses = new AtomicInteger(0);

    /**
     * Consecutive health check failures.
     */
    @Builder.Default
    private AtomicInteger consecutiveFailures = new AtomicInteger(0);

    /**
     * Last health check time.
     */
    private volatile Instant lastHealthCheck;

    /**
     * Last failure time.
     */
    private volatile Instant lastFailure;

    /**
     * Server metadata (for canary deployment, blue-green, etc.).
     */
    private String version;

    /**
     * Server tags for routing decisions.
     */
    private String[] tags;

    /**
     * Get server URL.
     */
    public String getUrl() {
        return String.format("http://%s:%d", host, port);
    }

    /**
     * Increment active connections.
     */
    public void incrementConnections() {
        activeConnections.incrementAndGet();
        totalRequests.incrementAndGet();
    }

    /**
     * Decrement active connections.
     */
    public void decrementConnections() {
        activeConnections.decrementAndGet();
    }

    /**
     * Record a failed request.
     */
    public void recordFailure() {
        failedRequests.incrementAndGet();
        consecutiveFailures.incrementAndGet();
        consecutiveSuccesses.set(0);
        lastFailure = Instant.now();
    }

    /**
     * Record a successful health check.
     */
    public void recordSuccess() {
        consecutiveSuccesses.incrementAndGet();
        consecutiveFailures.set(0);
        lastHealthCheck = Instant.now();
    }

    /**
     * Record a failed health check.
     */
    public void recordHealthCheckFailure() {
        consecutiveFailures.incrementAndGet();
        consecutiveSuccesses.set(0);
        lastHealthCheck = Instant.now();
    }

    /**
     * Get failure rate.
     */
    public double getFailureRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) failedRequests.get() / total;
    }

    /**
     * Check if server matches given version (for canary deployment).
     */
    public boolean matchesVersion(String targetVersion) {
        return targetVersion == null || targetVersion.equals(version);
    }

    /**
     * Check if server has given tag.
     */
    public boolean hasTag(String tag) {
        if (tags == null) {
            return false;
        }
        for (String t : tags) {
            if (t.equals(tag)) {
                return true;
            }
        }
        return false;
    }
}
