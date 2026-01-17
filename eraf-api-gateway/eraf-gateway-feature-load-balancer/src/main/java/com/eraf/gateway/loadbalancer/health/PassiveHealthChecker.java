package com.eraf.gateway.loadbalancer.health;

import com.eraf.gateway.loadbalancer.domain.HealthCheckConfig;
import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive health checker.
 * Monitors actual traffic and marks servers unhealthy based on real request failures.
 */
@Slf4j
@Component
public class PassiveHealthChecker {
    /**
     * Track recent failures per server.
     */
    private final Map<Server, FailureTracker> failureTrackers = new ConcurrentHashMap<>();

    /**
     * Record a successful request to a server.
     */
    public void recordSuccess(Server server, HealthCheckConfig config) {
        if (!config.isPassiveEnabled()) {
            return;
        }

        FailureTracker tracker = failureTrackers.computeIfAbsent(server, k -> new FailureTracker());
        tracker.recordSuccess();
    }

    /**
     * Record a failed request to a server.
     */
    public void recordFailure(Server server, HealthCheckConfig config) {
        if (!config.isPassiveEnabled()) {
            return;
        }

        server.recordFailure();

        FailureTracker tracker = failureTrackers.computeIfAbsent(server, k -> new FailureTracker());
        tracker.recordFailure();

        // Clean up old failures outside the window
        tracker.cleanUpOldFailures(config.getPassiveWindow());

        // Check if server should be marked unhealthy
        int recentFailures = tracker.getRecentFailureCount(config.getPassiveWindow());
        if (server.isHealthy() && recentFailures >= config.getPassiveUnhealthyThreshold()) {
            log.warn("Server {}:{} marked as UNHEALTHY by passive health check after {} failures in {}",
                    server.getHost(), server.getPort(), recentFailures, config.getPassiveWindow());
            server.setHealthy(false);
        }
    }

    /**
     * Clear failure tracking for a server.
     */
    public void clearTracking(Server server) {
        failureTrackers.remove(server);
    }

    /**
     * Tracks failures within a time window.
     */
    private static class FailureTracker {
        private final Map<Instant, AtomicInteger> failuresByTime = new ConcurrentHashMap<>();

        public void recordFailure() {
            Instant now = Instant.now();
            failuresByTime.computeIfAbsent(now, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }

        public void recordSuccess() {
            // Success resets the failure tracking
            failuresByTime.clear();
        }

        public int getRecentFailureCount(Duration window) {
            Instant cutoff = Instant.now().minus(window);
            return failuresByTime.entrySet().stream()
                    .filter(entry -> entry.getKey().isAfter(cutoff))
                    .mapToInt(entry -> entry.getValue().get())
                    .sum();
        }

        public void cleanUpOldFailures(Duration window) {
            Instant cutoff = Instant.now().minus(window);
            failuresByTime.entrySet()
                    .removeIf(entry -> entry.getKey().isBefore(cutoff));
        }
    }
}
