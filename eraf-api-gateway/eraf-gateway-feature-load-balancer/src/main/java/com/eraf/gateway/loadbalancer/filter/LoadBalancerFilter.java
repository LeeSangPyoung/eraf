package com.eraf.gateway.loadbalancer.filter;

import com.eraf.gateway.core.filter.GatewayFilter;
import com.eraf.gateway.core.filter.GatewayFilterChain;
import com.eraf.gateway.core.route.RouteContext;
import com.eraf.gateway.loadbalancer.config.LoadBalancerProperties;
import com.eraf.gateway.loadbalancer.domain.Server;
import com.eraf.gateway.loadbalancer.domain.Upstream;
import com.eraf.gateway.loadbalancer.health.PassiveHealthChecker;
import com.eraf.gateway.loadbalancer.proxy.HttpProxyClient;
import com.eraf.gateway.loadbalancer.proxy.ProxyResponse;
import com.eraf.gateway.loadbalancer.service.LoadBalancerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Load balancer filter.
 * Selects backend server and proxies requests based on configured algorithm.
 */
@Slf4j
@Component
@Order(FilterOrder.LOAD_BALANCER)
@RequiredArgsConstructor
public class LoadBalancerFilter implements GatewayFilter {
    private final LoadBalancerProperties properties;
    private final LoadBalancerService loadBalancerService;
    private final HttpProxyClient proxyClient;
    private final PassiveHealthChecker passiveHealthChecker;

    @Override
    public void doFilter(RouteContext context, GatewayFilterChain chain) throws Exception {
        if (!properties.isEnabled()) {
            chain.doFilter(context);
            return;
        }

        HttpServletRequest request = context.getRequest();
        String requestPath = request.getRequestURI();

        // Check if path should be excluded
        if (shouldExclude(requestPath)) {
            log.debug("Path {} excluded from load balancing", requestPath);
            chain.doFilter(context);
            return;
        }

        // Get upstream name from route context or route metadata
        String upstreamName = context.getAttribute("upstream");
        if (upstreamName == null) {
            log.debug("No upstream configured for path: {}", requestPath);
            chain.doFilter(context);
            return;
        }

        // Select backend server
        String clientIp = getClientIp(request);
        Server server = loadBalancerService.selectServer(upstreamName, clientIp);

        if (server == null) {
            log.error("No available server for upstream: {}", upstreamName);
            sendError(context.getResponse(), 503, "No available backend servers");
            return;
        }

        // Increment connection count
        server.incrementConnections();
        log.info("Routing request {} to server {}:{} (upstream: {})",
                requestPath, server.getHost(), server.getPort(), upstreamName);

        try {
            // Get upstream configuration
            Upstream upstream = loadBalancerService.getAllUpstreams().stream()
                    .filter(u -> u.getName().equals(upstreamName))
                    .findFirst()
                    .orElse(null);

            if (upstream == null) {
                throw new IllegalStateException("Upstream not found: " + upstreamName);
            }

            // Proxy request to selected server
            ProxyResponse proxyResponse = proxyClient.proxyRequest(request, server, upstream)
                    .block();

            if (proxyResponse == null) {
                throw new IOException("Proxy response is null");
            }

            // Handle response
            handleProxyResponse(context.getResponse(), proxyResponse, server, upstream);

            // Record success/failure for passive health check
            if (proxyResponse.isSuccess() && !proxyResponse.isServerError()) {
                passiveHealthChecker.recordSuccess(server, upstream.getHealthCheck());
            } else if (proxyResponse.isServerError()) {
                passiveHealthChecker.recordFailure(server, upstream.getHealthCheck());
            }

        } catch (Exception e) {
            log.error("Error proxying request to {}:{}: {}",
                    server.getHost(), server.getPort(), e.getMessage(), e);

            // Record failure for passive health check
            Upstream upstream = loadBalancerService.getAllUpstreams().stream()
                    .filter(u -> u.getName().equals(upstreamName))
                    .findFirst()
                    .orElse(null);

            if (upstream != null) {
                passiveHealthChecker.recordFailure(server, upstream.getHealthCheck());
            }

            sendError(context.getResponse(), 503, "Backend server unavailable: " + e.getMessage());
        } finally {
            // Decrement connection count
            server.decrementConnections();
        }
    }

    /**
     * Check if path should be excluded from load balancing.
     */
    private boolean shouldExclude(String path) {
        if (properties.getExcludePatterns() == null || properties.getExcludePatterns().isEmpty()) {
            return false;
        }

        return properties.getExcludePatterns().stream()
                .anyMatch(pattern -> Pattern.matches(pattern, path));
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            // Take first IP if multiple
            int commaIndex = ip.indexOf(',');
            if (commaIndex > 0) {
                ip = ip.substring(0, commaIndex).trim();
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * Handle proxy response.
     */
    private void handleProxyResponse(HttpServletResponse response, ProxyResponse proxyResponse,
                                     Server server, Upstream upstream) throws IOException {
        response.setStatus(proxyResponse.getStatusCode());

        // Copy headers
        if (proxyResponse.getHeaders() != null) {
            proxyResponse.getHeaders().forEach((name, values) -> {
                values.forEach(value -> response.addHeader(name, value));
            });
        }

        // Add load balancer headers
        response.addHeader("X-Upstream-Server", server.getHost() + ":" + server.getPort());
        response.addHeader("X-Upstream-Name", upstream.getName());

        // Write response body
        if (proxyResponse.getBody() != null) {
            response.getWriter().write(proxyResponse.getBody());
        }
    }

    /**
     * Send error response.
     */
    private void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

    @Override
    public int getOrder() {
        return FilterOrder.LOAD_BALANCER;
    }
}
