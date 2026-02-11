package com.eraf.openapi.features.filter.impl;

import com.eraf.core.logging.AuditLogger;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
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

/**
 * Circuit Breaker Gateway Filter
 * Resilience4j 기반 Circuit Breaker 패턴 적용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerGatewayFilter implements PluginGatewayFilter, Ordered {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public String getPluginName() {
        return "circuit-breaker";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 500;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // Circuit Breaker 설정 추출
        String name = (String) config.getOrDefault("name", "default");
        int failureRateThreshold = ((Number) config.getOrDefault("failure_rate_threshold", 50)).intValue();
        int slowCallRateThreshold = ((Number) config.getOrDefault("slow_call_rate_threshold", 100)).intValue();
        int slowCallDurationSeconds = ((Number) config.getOrDefault("slow_call_duration_seconds", 5)).intValue();
        int minimumNumberOfCalls = ((Number) config.getOrDefault("minimum_number_of_calls", 10)).intValue();
        int waitDurationInOpenStateSeconds = ((Number) config.getOrDefault("wait_duration_in_open_state_seconds", 60)).intValue();

        // Circuit Breaker 생성
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .slowCallRateThreshold(slowCallRateThreshold)
                .slowCallDurationThreshold(Duration.ofSeconds(slowCallDurationSeconds))
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationInOpenStateSeconds))
                .build();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name, circuitBreakerConfig);

        return (exchange, chain) -> applyCircuitBreaker(exchange, chain, circuitBreaker);
    }

    private Mono<Void> applyCircuitBreaker(ServerWebExchange exchange, GatewayFilterChain chain,
                                             CircuitBreaker circuitBreaker) {
        return chain.filter(exchange)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    log.error("Circuit breaker triggered for path: {}",
                            exchange.getRequest().getPath(), throwable);

                    AuditLogger.log(AuditLogger.AuditEvent.builder()
                            .action("CIRCUIT_BREAKER_OPEN")
                            .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                            .failure()
                            .detail("state", circuitBreaker.getState().name())
                            .build());

                    exchange.getResponse().getHeaders().add("X-Circuit-Breaker-Status",
                            circuitBreaker.getState().name());
                    return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.CIRCUIT_BREAKER_OPEN);
                });
    }
}
