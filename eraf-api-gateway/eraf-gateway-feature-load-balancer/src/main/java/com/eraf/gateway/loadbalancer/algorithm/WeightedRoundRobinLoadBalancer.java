package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Weighted Round Robin load balancer.
 * Distributes requests based on server weights.
 * Higher weight means more requests.
 */
@Slf4j
public class WeightedRoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final AtomicInteger currentWeight = new AtomicInteger(0);

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

        // Check if all weights are equal
        int firstWeight = servers.get(0).getWeight();
        boolean allEqual = servers.stream()
                .allMatch(s -> s.getWeight() == firstWeight);

        if (allEqual) {
            // Fallback to simple round-robin if all weights are equal
            int index = Math.abs(currentIndex.getAndIncrement() % size);
            return servers.get(index);
        }

        // Smooth weighted round-robin algorithm
        int maxWeight = servers.stream()
                .mapToInt(Server::getWeight)
                .max()
                .orElse(1);

        int totalWeight = servers.stream()
                .mapToInt(Server::getWeight)
                .sum();

        while (true) {
            int index = currentIndex.get() % size;
            int weight = currentWeight.get();

            currentIndex.incrementAndGet();
            weight++;

            if (weight > maxWeight) {
                weight = 0;
                currentWeight.set(0);
            } else {
                currentWeight.set(weight);
            }

            Server server = servers.get(index);
            if (weight <= server.getWeight()) {
                return server;
            }

            // Prevent infinite loop
            if (currentIndex.get() > totalWeight * 2) {
                log.warn("Failed to select server using weighted round-robin, falling back to first server");
                return servers.get(0);
            }
        }
    }

    @Override
    public void reset() {
        currentIndex.set(0);
        currentWeight.set(0);
    }
}
