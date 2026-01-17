package com.eraf.gateway.loadbalancer.algorithm;

import com.eraf.gateway.loadbalancer.domain.Server;

import java.util.List;

/**
 * Load balancer interface.
 * Defines the contract for server selection algorithms.
 */
public interface LoadBalancer {
    /**
     * Select a server from the list of healthy servers.
     *
     * @param servers List of healthy servers
     * @param clientIp Client IP address (for IP hash algorithm)
     * @return Selected server, or null if no server available
     */
    Server selectServer(List<Server> servers, String clientIp);

    /**
     * Reset the load balancer state.
     * Called when server list changes.
     */
    default void reset() {
        // Default implementation does nothing
    }
}
