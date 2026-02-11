package com.eraf.openapi.features.filter.impl;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * API Key Authentication Gateway Filter
 * API Key 기반 인증 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthGatewayFilter implements PluginGatewayFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public String getPluginName() {
        return "api-key";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // API Key 설정 추출
        String headerName = (String) config.getOrDefault("header_name", "X-API-Key");
        List<String> excludePaths = (List<String>) config.getOrDefault("exclude_paths", List.of());
        boolean required = (boolean) config.getOrDefault("required", true);

        return (exchange, chain) -> applyApiKeyAuth(exchange, chain, headerName, excludePaths, required);
    }

    private Mono<Void> applyApiKeyAuth(ServerWebExchange exchange, GatewayFilterChain chain,
                                         String headerName, List<String> excludePaths, boolean required) {
        String path = exchange.getRequest().getPath().value();

        // Exclude paths 체크
        if (excludePaths.stream().anyMatch(path::startsWith)) {
            log.debug("Path excluded from API Key auth: {}", path);
            return chain.filter(exchange);
        }

        // API Key 헤더에서 추출
        String apiKey = exchange.getRequest().getHeaders().getFirst(headerName);

        if (!StringUtils.hasText(apiKey)) {
            if (required) {
                log.warn("Missing API Key header: {}", headerName);
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.API_KEY_MISSING);
            }
            return chain.filter(exchange);
        }

        // Redis에서 API Key 검증
        String redisKey = "api_key:" + apiKey;
        return redisTemplate.opsForValue()
                .get(redisKey)
                .flatMap(keyData -> {
                    if (keyData == null) {
                        log.warn("Invalid API Key: {}", apiKey);
                        return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.API_KEY_INVALID);
                    }

                    // API Key 검증 성공 - 클라이언트 정보를 헤더에 추가
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(r -> r.header("X-Client-Id", keyData))
                            .build();

                    log.debug("API Key authentication successful for path: {}", path);
                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    if (required) {
                        log.warn("API Key not found in Redis: {}", apiKey);
                        return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.API_KEY_INVALID);
                    }
                    return chain.filter(exchange);
                }))
                .onErrorResume(e -> {
                    log.error("API Key validation failed", e);
                    if (required) {
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }
}
