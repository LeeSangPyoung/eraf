package com.eraf.openapi.features.filter.impl;

import com.eraf.core.logging.AuditLogger;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retry Gateway Filter
 * 지수 백오프(Exponential Backoff) 기반 자동 재시도 필터
 *
 * Config 예시:
 * {
 *   "max_attempts": 3,
 *   "wait_duration_ms": 1000,
 *   "multiplier": 2.0,
 *   "max_delay_ms": 10000,
 *   "retry_on_status": [502, 503, 504],
 *   "retry_methods": ["GET", "PUT"]
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "retry";
    }

    @Override
    public int getOrder() {
        // Circuit Breaker(500) 뒤에 실행 - Retry가 Circuit Breaker를 감싸는 형태
        return Ordered.HIGHEST_PRECEDENCE + 400;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        int maxAttempts = ((Number) config.getOrDefault("max_attempts", 3)).intValue();
        long waitDurationMs = ((Number) config.getOrDefault("wait_duration_ms", 1000)).longValue();
        double multiplier = ((Number) config.getOrDefault("multiplier", 2.0)).doubleValue();
        long maxDelayMs = ((Number) config.getOrDefault("max_delay_ms", 10000)).longValue();

        // 재시도할 HTTP 상태 코드 (기본: 502, 503, 504)
        List<Number> retryOnStatusList = (List<Number>) config.getOrDefault("retry_on_status",
                List.of(502, 503, 504));
        Set<Integer> retryOnStatus = retryOnStatusList.stream()
                .map(Number::intValue)
                .collect(Collectors.toSet());

        // 재시도할 HTTP 메서드 (기본: GET, PUT, DELETE - 멱등성 보장되는 메서드)
        List<String> retryMethodsList = (List<String>) config.getOrDefault("retry_methods",
                List.of("GET", "PUT", "DELETE"));
        Set<HttpMethod> retryMethods = retryMethodsList.stream()
                .map(HttpMethod::valueOf)
                .collect(Collectors.toSet());

        return (exchange, chain) -> applyRetry(exchange, chain,
                maxAttempts, waitDurationMs, multiplier, maxDelayMs,
                retryOnStatus, retryMethods);
    }

    private Mono<Void> applyRetry(ServerWebExchange exchange, GatewayFilterChain chain,
                                   int maxAttempts, long waitDurationMs, double multiplier, long maxDelayMs,
                                   Set<Integer> retryOnStatus, Set<HttpMethod> retryMethods) {

        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getPath().value();

        // 멱등성이 보장되지 않는 메서드는 재시도하지 않음
        if (!retryMethods.contains(method)) {
            log.debug("Retry skipped for non-idempotent method: {} {}", method, path);
            return chain.filter(exchange);
        }

        return chain.filter(exchange)
                .then(Mono.<Void>defer(() -> {
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    if (statusCode != null && retryOnStatus.contains(statusCode.value())) {
                        return Mono.error(new RetryableStatusException(statusCode.value()));
                    }
                    return Mono.empty();
                }))
                .retryWhen(Retry.backoff(maxAttempts - 1, Duration.ofMillis(waitDurationMs))
                        .jitter(0.1)
                        .maxBackoff(Duration.ofMillis(maxDelayMs))
                        .filter(throwable -> throwable instanceof RetryableStatusException)
                        .doBeforeRetry(retrySignal -> {
                            long attempt = retrySignal.totalRetries() + 1;
                            log.info("Retry attempt {}/{} for {} {} (reason: {})",
                                    attempt, maxAttempts - 1, method, path,
                                    retrySignal.failure().getMessage());
                        })
                )
                .doOnError(throwable -> {
                    if (!(throwable instanceof RetryableStatusException)) {
                        log.warn("All retry attempts exhausted for {} {}", method, path);
                    }
                })
                .onErrorResume(throwable -> {
                    if (Exceptions.isRetryExhausted(throwable)) {
                        exchange.getResponse().getHeaders().add("X-Retry-Exhausted", "true");

                        AuditLogger.log(AuditLogger.AuditEvent.builder()
                                .action("RETRY_EXHAUSTED")
                                .resource(method + " " + path)
                                .failure()
                                .detail("max_attempts", maxAttempts)
                                .build());
                    }
                    return Mono.empty();
                });
    }

    /**
     * 재시도 가능한 상태 코드 예외
     */
    private static class RetryableStatusException extends RuntimeException {
        private final int statusCode;

        RetryableStatusException(int statusCode) {
            super("Retryable HTTP status: " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
