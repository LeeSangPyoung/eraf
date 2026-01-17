package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * IP Hash load balancer.
 * Routes requests based on client IP address hash.
 * Provides sticky sessions - same client always goes to same server.
 */
@Slf4j
public class IpHashLoadBalancer implements LoadBalancer {

    @Override
    public Server selectServer(List<Server> servers, String clientIp) {
        if (servers == null || servers.isEmpty()) {
            log.warn("No servers available for load balancing");
            return null;
        }

        if (servers.size() == 1) {
            return servers.get(0);
        }

        if (clientIp == null || clientIp.isEmpty()) {
            log.warn("Client IP not available for IP hash, falling back to first server");
            return servers.get(0);
        }

        // Hash the client IP to get consistent server selection
        int hash = clientIp.hashCode();
        int index = Math.abs(hash % servers.size());

        return servers.get(index);
    }
}
