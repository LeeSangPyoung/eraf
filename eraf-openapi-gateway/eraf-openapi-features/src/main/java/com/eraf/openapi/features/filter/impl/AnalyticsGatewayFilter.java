package com.eraf.openapi.features.filter.impl;

import com.eraf.converter.JsonConverter;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Analytics Gateway Filter
 * 요청/응답 분석 및 메트릭 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsGatewayFilter implements PluginGatewayFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public String getPluginName() {
        return "analytics";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // Analytics 설정 추출
        boolean collectHeaders = (boolean) config.getOrDefault("collect_headers", false);
        boolean collectBody = (boolean) config.getOrDefault("collect_body", false);

        return (exchange, chain) -> applyAnalytics(exchange, chain, collectHeaders, collectBody);
    }

    private Mono<Void> applyAnalytics(ServerWebExchange exchange, GatewayFilterChain chain,
                                        boolean collectHeaders, boolean collectBody) {
        long startTime = System.currentTimeMillis();

        // 요청 정보 수집
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;

                    // Analytics 데이터 저장
                    saveAnalytics(method, path, clientIp, statusCode, duration,
                            collectHeaders ? exchange.getRequest().getHeaders().toSingleValueMap() : null)
                            .subscribe(
                                    result -> log.debug("Analytics saved: {} {} - {} ({}ms)",
                                            method, path, statusCode, duration),
                                    error -> log.error("Failed to save analytics", error)
                            );
                })
                .doOnError(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Request failed: {} {} - error after {}ms", method, path, duration, throwable);

                    // 에러도 분석 데이터에 포함
                    saveAnalytics(method, path, clientIp, 500, duration, null)
                            .subscribe();
                });
    }

    private Mono<Boolean> saveAnalytics(String method, String path, String clientIp,
                                         int statusCode, long duration, Map<String, String> headers) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String analyticsKey = String.format("analytics:%s:%s:%s", timestamp, method, path);

        Map<String, Object> analyticsMap = Map.of(
                "method", method,
                "path", path,
                "client_ip", clientIp,
                "status", statusCode,
                "duration", duration,
                "timestamp", timestamp
        );
        String analyticsData = JsonConverter.toJson(analyticsMap);

        // Redis에 Analytics 데이터 저장
        return redisTemplate.opsForValue()
                .set(analyticsKey, analyticsData)
                .flatMap(result -> {
                    // 메트릭 카운터 증가
                    String counterKey = String.format("metrics:count:%s:%s", method, path);
                    return redisTemplate.opsForValue().increment(counterKey).map(count -> true);
                })
                .onErrorResume(e -> {
                    log.error("Failed to save analytics to Redis", e);
                    return Mono.just(false);
                });
    }
}
