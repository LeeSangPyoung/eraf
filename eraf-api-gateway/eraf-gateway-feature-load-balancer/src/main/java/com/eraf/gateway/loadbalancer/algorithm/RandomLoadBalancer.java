package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random load balancer.
 * Randomly selects a server from the list.
 * Simple and effective for stateless applications.
 */
@Slf4j
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public Server selectServer(List<Server> servers, String clientIp) {
        if (servers == null || servers.isEmpty()) {
            log.warn("No servers available for load balancing");
            return null;
        }

        if (servers.size() == 1) {
            return servers.get(0);
        }

        // Select random server
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        return servers.get(index);
    }
}
