package com.eraf.gateway.loadbalancer.filter;

import org.springframework.core.Ordered;

/**
 * Filter order constants for load balancer.
 */
public final class FilterOrder {
    /**
     * Load balancer filter order.
     * Runs after circuit breaker (HIGHEST + 45).
     */
    public static final int LOAD_BALANCER = Ordered.HIGHEST_PRECEDENCE + 45;

    private FilterOrder() {
        // Utility class
    }
}
