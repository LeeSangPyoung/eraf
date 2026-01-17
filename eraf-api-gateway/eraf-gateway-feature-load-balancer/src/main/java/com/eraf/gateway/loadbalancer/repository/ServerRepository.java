package com.eraf.gateway.loadbalancer.repository;

import com.eraf.gateway.loadbalancer.domain.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for server instances.
 * Manages server metadata and state.
 */
@Slf4j
@Repository
public class ServerRepository {
    // Map of upstream name to list of servers
    private final Map<String, List<Server>> serversByUpstream = new ConcurrentHashMap<>();

    /**
     * Add a server to an upstream.
     */
    public void addServer(String upstreamName, Server server) {
        log.info("Adding server {}:{} to upstream {}",
                server.getHost(), server.getPort(), upstreamName);

        serversByUpstream.computeIfAbsent(upstreamName, k -> new ArrayList<>())
                .add(server);
    }

    /**
     * Remove a server from an upstream.
     */
    public void removeServer(String upstreamName, Server server) {
        log.info("Removing server {}:{} from upstream {}",
                server.getHost(), server.getPort(), upstreamName);

        List<Server> servers = serversByUpstream.get(upstreamName);
        if (servers != null) {
            servers.removeIf(s ->
                    s.getHost().equals(server.getHost()) &&
                    s.getPort().equals(server.getPort()));
        }
    }

    /**
     * Get all servers for an upstream.
     */
    public List<Server> findByUpstream(String upstreamName) {
        return serversByUpstream.getOrDefault(upstreamName, Collections.emptyList());
    }

    /**
     * Find a specific server in an upstream.
     */
    public Optional<Server> findServer(String upstreamName, String host, Integer port) {
        return findByUpstream(upstreamName).stream()
                .filter(s -> s.getHost().equals(host) && s.getPort().equals(port))
                .findFirst();
    }

    /**
     * Get all healthy servers for an upstream.
     */
    public List<Server> findHealthyServers(String upstreamName) {
        return findByUpstream(upstreamName).stream()
                .filter(Server::isHealthy)
                .collect(Collectors.toList());
    }

    /**
     * Update server health status.
     */
    public void updateServerHealth(String upstreamName, String host, Integer port, boolean healthy) {
        findServer(upstreamName, host, port).ifPresent(server -> {
            log.info("Updating server {}:{} health status to: {}",
                    host, port, healthy ? "HEALTHY" : "UNHEALTHY");
            server.setHealthy(healthy);
        });
    }

    /**
     * Clear all servers for an upstream.
     */
    public void clearUpstream(String upstreamName) {
        log.info("Clearing all servers for upstream: {}", upstreamName);
        serversByUpstream.remove(upstreamName);
    }

    /**
     * Get count of servers for an upstream.
     */
    public int countByUpstream(String upstreamName) {
        return findByUpstream(upstreamName).size();
    }

    /**
     * Get count of healthy servers for an upstream.
     */
    public int countHealthyByUpstream(String upstreamName) {
        return (int) findByUpstream(upstreamName).stream()
                .filter(Server::isHealthy)
                .count();
    }
}
