package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cache Gateway Filter
 * Redis 기반 응답 캐싱
 *
 * <p>GET 요청의 응답을 Redis에 캐싱하여 성능을 향상시킵니다.</p>
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

                        // 응답 캐싱을 위해 ServerHttpResponseDecorator 사용
                        ServerHttpResponse originalResponse = exchange.getResponse();
                        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

                        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                            @Override
                            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                if (body instanceof Flux) {
                                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                        // 여러 DataBuffer를 하나로 합침
                                        DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                                        byte[] content = new byte[joinedBuffer.readableByteCount()];
                                        joinedBuffer.read(content);
                                        DataBufferUtils.release(joinedBuffer);

                                        // 응답이 성공(2xx)인 경우에만 캐싱
                                        HttpStatus statusCode = (HttpStatus) getDelegate().getStatusCode();
                                        if (statusCode != null && statusCode.is2xxSuccessful()) {
                                            String responseBody = new String(content, StandardCharsets.UTF_8);

                                            // Redis에 캐싱 (비동기, fire-and-forget)
                                            redisTemplate.opsForValue()
                                                    .set(cacheKey, responseBody, Duration.ofSeconds(ttlSeconds))
                                                    .subscribe(
                                                            success -> log.debug("Cached response for key: {}", cacheKey),
                                                            error -> log.error("Failed to cache response for key: {}", cacheKey, error)
                                                    );
                                        }

                                        // 원본 응답 데이터 반환
                                        return bufferFactory.wrap(content);
                                    }));
                                }
                                return super.writeWith(body);
                            }
                        };

                        // X-Cache-Status 헤더 추가
                        decoratedResponse.getHeaders().add("X-Cache-Status", "MISS");

                        // 수정된 exchange로 필터 체인 진행
                        return chain.filter(exchange.mutate().response(decoratedResponse).build());
                    }))
                    .onErrorResume(e -> {
                        log.error("Cache operation failed", e);
                        return chain.filter(exchange);
                    });
        }

        return chain.filter(exchange);
    }
}
