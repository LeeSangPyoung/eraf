package com.eraf.gateway.loadbalancer.health;

import com.eraf.gateway.loadbalancer.domain.Upstream;
import com.eraf.gateway.loadbalancer.repository.UpstreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled health check task.
 * Periodically runs health checks on all configured upstreams.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {
    private final UpstreamRepository upstreamRepository;
    private final HealthChecker healthChecker;

    /**
     * Run health checks every 10 seconds.
     * Individual upstream configurations may have different intervals,
     * but this provides a baseline check frequency.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void runHealthChecks() {
        try {
            upstreamRepository.findAll().forEach(upstream -> {
                try {
                    if (shouldRunHealthCheck(upstream)) {
                        healthChecker.checkUpstream(upstream);
                    }
                } catch (Exception e) {
                    log.error("Error running health check for upstream {}: {}",
                            upstream.getName(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error in health check scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Determine if health check should run for this upstream.
     */
    private boolean shouldRunHealthCheck(Upstream upstream) {
        if (!upstream.getHealthCheck().isEnabled()) {
            return false;
        }

        // Always run check if no servers have been checked yet
        boolean hasUncheckedServers = upstream.getServers().stream()
                .anyMatch(server -> server.getLastHealthCheck() == null);

        if (hasUncheckedServers) {
            return true;
        }

        // Check if enough time has passed since last check
        return upstream.getServers().stream()
                .anyMatch(server -> {
                    if (server.getLastHealthCheck() == null) {
                        return true;
                    }
                    long secondsSinceLastCheck = java.time.Duration.between(
                            server.getLastHealthCheck(),
                            java.time.Instant.now()
                    ).getSeconds();

                    return secondsSinceLastCheck >= upstream.getHealthCheck()
                            .getInterval().getSeconds();
                });
    }
}
