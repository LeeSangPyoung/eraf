package com.eraf.openapi.features.filter.impl;

import com.eraf.core.logging.AuditLogger;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Timeout Gateway Filter
 * API/Route별 커스텀 타임아웃 설정
 *
 * Config 예시:
 * {
 *   "timeout_ms": 5000,
 *   "message": "Request timeout exceeded"
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeoutGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "timeout";
    }

    @Override
    public int getOrder() {
        // 가장 바깥에서 타임아웃 감시 (Retry보다 앞에 실행)
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        long timeoutMs = ((Number) config.getOrDefault("timeout_ms", 30000)).longValue();
        String message = (String) config.getOrDefault("message", "Gateway Timeout");

        return (exchange, chain) -> applyTimeout(exchange, chain, timeoutMs, message);
    }

    private Mono<Void> applyTimeout(ServerWebExchange exchange, GatewayFilterChain chain,
                                     long timeoutMs, String message) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(exchange)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnSuccess(v -> {
                    exchange.getResponse().getHeaders()
                            .add("X-Timeout-Limit", timeoutMs + "ms");
                })
                .onErrorResume(TimeoutException.class, e -> {
                    log.warn("Request timeout after {}ms for {} {}", timeoutMs, method, path);

                    AuditLogger.log(AuditLogger.AuditEvent.builder()
                            .action("GATEWAY_TIMEOUT")
                            .resource(method + " " + path)
                            .failure()
                            .detail("timeout_ms", timeoutMs)
                            .build());

                    exchange.getResponse().getHeaders().add("X-Timeout-Limit", timeoutMs + "ms");
                    exchange.getResponse().getHeaders().add("X-Timeout-Exceeded", "true");

                    return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.GATEWAY_TIMEOUT, message);
                });
    }
}
