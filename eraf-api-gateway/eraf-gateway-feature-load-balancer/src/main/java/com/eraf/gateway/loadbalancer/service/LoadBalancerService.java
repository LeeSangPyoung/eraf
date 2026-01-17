package com.eraf.gateway.loadbalancer.service;

import com.eraf.gateway.loadbalancer.algorithm.*;
import com.eraf.gateway.loadbalancer.domain.LoadBalancerAlgorithm;
import com.eraf.gateway.loadbalancer.domain.Server;
import com.eraf.gateway.loadbalancer.domain.Upstream;
import com.eraf.gateway.loadbalancer.repository.UpstreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Load balancer service.
 * Manages server selection and upstream statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadBalancerService {
    private final UpstreamRepository upstreamRepository;
    private final Map<LoadBalancerAlgorithm, LoadBalancer> loadBalancers = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialize load balancer implementations
        loadBalancers.put(LoadBalancerAlgorithm.ROUND_ROBIN, new RoundRobinLoadBalancer());
        loadBalancers.put(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN, new WeightedRoundRobinLoadBalancer());
        loadBalancers.put(LoadBalancerAlgorithm.LEAST_CONNECTIONS, new LeastConnectionsLoadBalancer());
        loadBalancers.put(LoadBalancerAlgorithm.RANDOM, new RandomLoadBalancer());
        loadBalancers.put(LoadBalancerAlgorithm.IP_HASH, new IpHashLoadBalancer());
    }

    /**
     * Select a server from an upstream.
     */
    public Server selectServer(String upstreamName, String clientIp) {
        Upstream upstream = upstreamRepository.findByName(upstreamName)
                .orElseThrow(() -> new IllegalArgumentException("Upstream not found: " + upstreamName));

        // Handle canary deployment
        if (upstream.isCanaryEnabled()) {
            return selectServerWithCanary(upstream, clientIp);
        }

        List<Server> healthyServers = upstream.getHealthyServers();
        if (healthyServers.isEmpty()) {
            log.warn("No healthy servers available for upstream: {}", upstreamName);
            return null;
        }

        LoadBalancer loadBalancer = loadBalancers.get(upstream.getAlgorithm());
        if (loadBalancer == null) {
            log.error("Load balancer not found for algorithm: {}, falling back to ROUND_ROBIN",
                    upstream.getAlgorithm());
            loadBalancer = loadBalancers.get(LoadBalancerAlgorithm.ROUND_ROBIN);
        }

        return loadBalancer.selectServer(healthyServers, clientIp);
    }

    /**
     * Select server with canary deployment support.
     */
    private Server selectServerWithCanary(Upstream upstream, String clientIp) {
        int canaryPercentage = upstream.getCanaryPercentage();
        int random = ThreadLocalRandom.current().nextInt(100);

        String targetVersion;
        if (random < canaryPercentage) {
            // Route to canary version
            targetVersion = upstream.getCanaryVersion();
            log.debug("Routing to canary version: {}", targetVersion);
        } else {
            // Route to stable version (null means non-canary servers)
            targetVersion = null;
            log.debug("Routing to stable version");
        }

        List<Server> healthyServers = upstream.getHealthyServers(targetVersion);
        if (healthyServers.isEmpty()) {
            log.warn("No healthy servers available for version: {}, falling back to all servers",
                    targetVersion);
            healthyServers = upstream.getHealthyServers();
        }

        if (healthyServers.isEmpty()) {
            return null;
        }

        LoadBalancer loadBalancer = loadBalancers.get(upstream.getAlgorithm());
        return loadBalancer.selectServer(healthyServers, clientIp);
    }

    /**
     * Mark a server as down (unhealthy).
     */
    public void markServerDown(String upstreamName, Server server) {
        log.warn("Marking server {}:{} as DOWN in upstream {}",
                server.getHost(), server.getPort(), upstreamName);
        server.setHealthy(false);

        // Reset load balancer state when server list changes
        Upstream upstream = upstreamRepository.findByName(upstreamName).orElse(null);
        if (upstream != null) {
            LoadBalancer loadBalancer = loadBalancers.get(upstream.getAlgorithm());
            if (loadBalancer != null) {
                loadBalancer.reset();
            }
        }
    }

    /**
     * Mark a server as up (healthy).
     */
    public void markServerUp(String upstreamName, Server server) {
        log.info("Marking server {}:{} as UP in upstream {}",
                server.getHost(), server.getPort(), upstreamName);
        server.setHealthy(true);

        // Reset load balancer state when server list changes
        Upstream upstream = upstreamRepository.findByName(upstreamName).orElse(null);
        if (upstream != null) {
            LoadBalancer loadBalancer = loadBalancers.get(upstream.getAlgorithm());
            if (loadBalancer != null) {
                loadBalancer.reset();
            }
        }
    }

    /**
     * Get statistics for an upstream.
     */
    public UpstreamStats getUpstreamStats(String upstreamName) {
        Upstream upstream = upstreamRepository.findByName(upstreamName)
                .orElseThrow(() -> new IllegalArgumentException("Upstream not found: " + upstreamName));

        return UpstreamStats.builder()
                .name(upstream.getName())
                .algorithm(upstream.getAlgorithm())
                .totalServers(upstream.getTotalServers())
                .healthyServers(upstream.getHealthyServerCount())
                .totalActiveConnections(upstream.getTotalActiveConnections())
                .totalRequests(upstream.getTotalRequests())
                .totalFailedRequests(upstream.getTotalFailedRequests())
                .overallFailureRate(upstream.getOverallFailureRate())
                .canaryEnabled(upstream.isCanaryEnabled())
                .canaryPercentage(upstream.getCanaryPercentage())
                .build();
    }

    /**
     * Get all upstreams.
     */
    public List<Upstream> getAllUpstreams() {
        return (List<Upstream>) upstreamRepository.findAll();
    }

    /**
     * Create or update an upstream.
     */
    public Upstream saveUpstream(Upstream upstream) {
        return upstreamRepository.save(upstream);
    }

    /**
     * Delete an upstream.
     */
    public void deleteUpstream(String upstreamName) {
        upstreamRepository.deleteByName(upstreamName);
    }
}
