package com.eraf.gateway.loadbalancer.health;

import com.eraf.gateway.loadbalancer.domain.HealthCheckConfig;
import com.eraf.gateway.loadbalancer.domain.Server;
import com.eraf.gateway.loadbalancer.domain.Upstream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Active health checker.
 * Periodically checks server health by sending HTTP requests.
 */
@Slf4j
@Component
public class HealthChecker {
    private final WebClient webClient;

    public HealthChecker(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Perform health check on a server.
     */
    public void checkServer(Server server, HealthCheckConfig config) {
        if (!config.isEnabled()) {
            return;
        }

        String healthUrl = server.getUrl() + config.getPath();

        webClient.get()
                .uri(healthUrl)
                .retrieve()
                .toBodilessEntity()
                .timeout(config.getTimeout())
                .subscribe(
                        response -> handleHealthCheckSuccess(server, config, response.getStatusCode()),
                        error -> handleHealthCheckFailure(server, config, error)
                );
    }

    /**
     * Handle successful health check.
     */
    private void handleHealthCheckSuccess(Server server, HealthCheckConfig config, HttpStatus status) {
        if (config.isExpectedStatus(status.value())) {
            server.recordSuccess();

            if (!server.isHealthy() &&
                server.getConsecutiveSuccesses().get() >= config.getHealthyThreshold()) {
                log.info("Server {}:{} marked as HEALTHY after {} consecutive successes",
                        server.getHost(), server.getPort(), config.getHealthyThreshold());
                server.setHealthy(true);
            }
        } else {
            log.warn("Server {}:{} returned unexpected status: {}",
                    server.getHost(), server.getPort(), status.value());
            handleHealthCheckFailure(server, config,
                new RuntimeException("Unexpected status: " + status.value()));
        }
    }

    /**
     * Handle failed health check.
     */
    private void handleHealthCheckFailure(Server server, HealthCheckConfig config, Throwable error) {
        server.recordHealthCheckFailure();

        log.debug("Health check failed for {}:{}: {}",
                server.getHost(), server.getPort(), error.getMessage());

        if (server.isHealthy() &&
            server.getConsecutiveFailures().get() >= config.getUnhealthyThreshold()) {
            log.warn("Server {}:{} marked as UNHEALTHY after {} consecutive failures",
                    server.getHost(), server.getPort(), config.getUnhealthyThreshold());
            server.setHealthy(false);
        }
    }

    /**
     * Check all servers in an upstream.
     */
    public void checkUpstream(Upstream upstream) {
        if (!upstream.getHealthCheck().isEnabled()) {
            return;
        }

        log.debug("Performing health checks for upstream: {}", upstream.getName());

        upstream.getServers().forEach(server -> {
            try {
                checkServer(server, upstream.getHealthCheck());
            } catch (Exception e) {
                log.error("Error checking server {}:{}: {}",
                        server.getHost(), server.getPort(), e.getMessage());
            }
        });
    }
}
