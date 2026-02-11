package com.eraf.openapi.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Web filter to log all requests passing through the gateway (including admin APIs)
 */
@Slf4j
@Component
public class RequestLoggingFilter implements WebFilter, Ordered {

    // In-memory storage for demo purposes
    private static final List<RequestLog> REQUEST_LOGS = new CopyOnWriteArrayList<>();
    private static final int MAX_LOGS = 1000;

    public RequestLoggingFilter() {
        log.info("========== RequestLoggingFilter initialized ==========");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        log.info("========== RequestLoggingFilter.filter() called: {} {}",
            request.getMethod(), request.getPath());

        return chain.filter(exchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = exchange.getResponse();
            int statusCode = response.getStatusCode() != null
                ? response.getStatusCode().value()
                : 200;

            RequestLog requestLog = RequestLog.builder()
                .timestamp(Instant.now())
                .method(request.getMethod().name())
                .path(request.getPath().value())
                .queryParams(request.getQueryParams().toString())
                .clientIp(getClientIp(request))
                .statusCode(statusCode)
                .duration(duration)
                .userAgent(request.getHeaders().getFirst("User-Agent"))
                .build();

            addLog(requestLog);

            log.info("Request: {} {} - Status: {} - Duration: {}ms",
                request.getMethod(), request.getPath(), statusCode, duration);
        });
    }

    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        }

        // Convert IPv6 loopback to IPv4 for better readability
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    private void addLog(RequestLog log) {
        REQUEST_LOGS.add(0, log); // Add to beginning
        if (REQUEST_LOGS.size() > MAX_LOGS) {
            REQUEST_LOGS.remove(REQUEST_LOGS.size() - 1);
        }
    }

    public static List<RequestLog> getRequestLogs(int limit) {
        return REQUEST_LOGS.stream()
            .limit(limit > 0 ? limit : REQUEST_LOGS.size())
            .toList();
    }

    public static void clearLogs() {
        REQUEST_LOGS.clear();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @lombok.Data
    @lombok.Builder
    public static class RequestLog {
        private Instant timestamp;
        private String method;
        private String path;
        private String queryParams;
        private String clientIp;
        private int statusCode;
        private long duration; // milliseconds
        private String userAgent;
    }
}
