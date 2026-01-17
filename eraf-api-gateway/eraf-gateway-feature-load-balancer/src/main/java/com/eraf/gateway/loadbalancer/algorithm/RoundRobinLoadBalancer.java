package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round Robin load balancer.
 * Distributes requests evenly across all servers in sequential order.
 */
@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public Server selectServer(List<Server> servers, String clientIp) {
        if (servers == null || servers.isEmpty()) {
            log.warn("No servers available for load balancing");
            return null;
        }

        int size = servers.size();
        if (size == 1) {
            return servers.get(0);
        }

        // Get next index using round-robin
        int index = Math.abs(currentIndex.getAndIncrement() % size);
        return servers.get(index);
    }

    @Override
    public void reset() {
        currentIndex.set(0);
    }
}
