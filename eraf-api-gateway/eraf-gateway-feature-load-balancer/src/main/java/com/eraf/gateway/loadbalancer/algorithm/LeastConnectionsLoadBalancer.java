package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

/**
 * Least Connections load balancer.
 * Routes requests to the server with fewest active connections.
 * Good for long-lived connections and varying request processing times.
 */
@Slf4j
public class LeastConnectionsLoadBalancer implements LoadBalancer {

    @Override
    public Server selectServer(List<Server> servers, String clientIp) {
        if (servers == null || servers.isEmpty()) {
            log.warn("No servers available for load balancing");
            return null;
        }

        if (servers.size() == 1) {
            return servers.get(0);
        }

        // Find server with least active connections
        return servers.stream()
                .min(Comparator.comparingInt(s -> s.getActiveConnections().get()))
                .orElse(null);
    }
}
