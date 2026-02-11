package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cache Gateway Filter
 * Redis 기반 응답 캐싱
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheGatewayFilter implements PluginGatewayFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public String getPluginName() {
        return "cache";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 300;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // Cache 설정 추출
        int ttlSeconds = ((Number) config.getOrDefault("ttl_seconds", 300)).intValue();
        List<String> cacheMethods = (List<String>) config.getOrDefault("methods", List.of("GET"));
        List<String> excludePaths = (List<String>) config.getOrDefault("exclude_paths", List.of());

        return (exchange, chain) -> applyCache(exchange, chain, ttlSeconds, cacheMethods, excludePaths);
    }

    private Mono<Void> applyCache(ServerWebExchange exchange, GatewayFilterChain chain,
                                    int ttlSeconds, List<String> cacheMethods, List<String> excludePaths) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String query = exchange.getRequest().getURI().getQuery();

        // Exclude paths 체크
        if (excludePaths.stream().anyMatch(path::startsWith)) {
            log.debug("Path excluded from cache: {}", path);
            return chain.filter(exchange);
        }

        // Cache 대상 메소드 확인
        if (!cacheMethods.contains(method)) {
            return chain.filter(exchange);
        }

        // Cache 키 생성
        String cacheKey = String.format("cache:%s:%s%s",
                method, path, query != null ? "?" + query : "");

        // GET 요청만 캐싱 (안전한 메소드)
        if (HttpMethod.GET.matches(method)) {
            return redisTemplate.opsForValue()
                    .get(cacheKey)
                    .flatMap(cachedResponse -> {
                        log.debug("Cache HIT: {}", cacheKey);
                        // 캐시된 응답 반환
                        exchange.getResponse().setStatusCode(HttpStatus.OK);
                        exchange.getResponse().getHeaders().add("X-Cache-Status", "HIT");
                        return exchange.getResponse()
                                .writeWith(Mono.just(exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(cachedResponse.getBytes())));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.debug("Cache MISS: {}", cacheKey);
                        exchange.getResponse().getHeaders().add("X-Cache-Status", "MISS");

                        // TODO: 응답을 캐싱하려면 response body를 캡처해야 함
                        // 현재는 기본 구조만 구현
                        return chain.filter(exchange)
                                .then(Mono.defer(() -> cacheResponse(cacheKey, ttlSeconds)));
                    }))
                    .onErrorResume(e -> {
                        log.error("Cache operation failed", e);
                        return chain.filter(exchange);
                    });
        }

        return chain.filter(exchange);
    }

    private Mono<Void> cacheResponse(String cacheKey, int ttlSeconds) {
        // TODO: 실제 응답 본문을 캡처하여 캐싱
        // Spring Cloud Gateway의 ModifyResponseBodyGatewayFilterFactory 참고
        return Mono.empty();
    }
}
