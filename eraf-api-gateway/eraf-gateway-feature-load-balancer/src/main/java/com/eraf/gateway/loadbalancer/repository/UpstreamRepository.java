package com.eraf.gateway.loadbalancer.repository;

import com.eraf.gateway.loadbalancer.domain.Upstream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for upstream configurations.
 * Stores and manages upstream definitions in memory.
 */
@Slf4j
@Repository
public class UpstreamRepository {
    private final Map<String, Upstream> upstreams = new ConcurrentHashMap<>();

    /**
     * Save or update an upstream.
     */
    public Upstream save(Upstream upstream) {
        log.info("Saving upstream: {}", upstream.getName());
        upstreams.put(upstream.getName(), upstream);
        return upstream;
    }

    /**
     * Find upstream by name.
     */
    public Optional<Upstream> findByName(String name) {
        return Optional.ofNullable(upstreams.get(name));
    }

    /**
     * Find all upstreams.
     */
    public Collection<Upstream> findAll() {
        return upstreams.values();
    }

    /**
     * Delete upstream by name.
     */
    public void deleteByName(String name) {
        log.info("Deleting upstream: {}", name);
        upstreams.remove(name);
    }

    /**
     * Check if upstream exists.
     */
    public boolean exists(String name) {
        return upstreams.containsKey(name);
    }

    /**
     * Get count of upstreams.
     */
    public int count() {
        return upstreams.size();
    }

    /**
     * Clear all upstreams.
     */
    public void clear() {
        log.warn("Clearing all upstreams");
        upstreams.clear();
    }
}
