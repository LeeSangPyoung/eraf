package com.eraf.gateway.loadbalancer.domain;

/**
 * Load balancer algorithm types.
 * Represents different strategies for distributing traffic across backend servers.
 */
public enum LoadBalancerAlgorithm {
    /**
     * Round Robin - Distributes requests evenly across all servers in order.
     */
    ROUND_ROBIN,

    /**
     * Weighted Round Robin - Distributes requests based on server weights.
     * Higher weight means more requests.
     */
    WEIGHTED_ROUND_ROBIN,

    /**
     * Least Connections - Routes to server with fewest active connections.
     * Good for long-lived connections.
     */
    LEAST_CONNECTIONS,

    /**
     * Random - Randomly selects a server.
     * Simple and effective for stateless applications.
     */
    RANDOM,

    /**
     * IP Hash - Routes based on client IP address hash.
     * Provides sticky sessions for same client IP.
     */
    IP_HASH
}
