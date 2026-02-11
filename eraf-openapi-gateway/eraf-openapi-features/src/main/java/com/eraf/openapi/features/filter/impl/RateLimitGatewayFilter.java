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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Rate Limit Gateway Filter
 * Redis 기반 분산 Rate Limiting
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitGatewayFilter implements PluginGatewayFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public String getPluginName() {
        return "rate-limit";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // Rate limit 설정 추출
        int limit = ((Number) config.getOrDefault("limit", 100)).intValue();
        int windowSeconds = ((Number) config.getOrDefault("window_seconds", 60)).intValue();
        String keyPrefix = (String) config.getOrDefault("key_prefix", "rate_limit");

        return (exchange, chain) -> applyRateLimit(exchange, chain, keyPrefix, limit, windowSeconds);
    }

    private Mono<Void> applyRateLimit(ServerWebExchange exchange, GatewayFilterChain chain,
                                       String keyPrefix, int limit, int windowSeconds) {
        // Rate limit 키 생성 (IP 기반)
        String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String key = String.format("%s:%s", keyPrefix, clientIp);

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // 첫 요청이면 만료 시간 설정
                        return redisTemplate.expire(key, Duration.ofSeconds(windowSeconds))
                                .flatMap(result -> processRequest(exchange, chain, count, limit));
                    } else {
                        return processRequest(exchange, chain, count, limit);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Rate limit check failed", e);
                    // Redis 오류 시에도 요청은 통과
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain,
                                       Long count, int limit) {
        // Rate limit 헤더 추가
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, limit - count)));

        if (count > limit) {
            log.warn("Rate limit exceeded: {} > {}", count, limit);

            AuditLogger.log(AuditLogger.AuditEvent.builder()
                    .action("RATE_LIMIT_EXCEEDED")
                    .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                    .failure()
                    .detail("count", count)
                    .detail("limit", limit)
                    .build());

            exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "60");
            return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        }

        return chain.filter(exchange);
    }
}
