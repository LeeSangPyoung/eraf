package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Request Transform Gateway Filter
 * 요청 헤더/쿼리파라미터/경로 변환
 *
 * Config 예시:
 * {
 *   "add_headers": { "X-Custom-Header": "value", "X-Request-Source": "gateway" },
 *   "remove_headers": ["X-Internal-Only", "X-Debug"],
 *   "rename_headers": { "X-Old-Name": "X-New-Name" },
 *   "add_query_params": { "gateway": "true", "version": "2" },
 *   "remove_query_params": ["debug", "trace"],
 *   "path_rewrite": { "regex": "/api/v1/(.*)", "replacement": "/internal/$1" }
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTransformGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "request-transformer";
    }

    @Override
    public int getOrder() {
        // 인증 필터(200) 전에 실행하여 헤더를 먼저 변환
        return Ordered.HIGHEST_PRECEDENCE + 150;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        Map<String, String> addHeaders = (Map<String, String>) config.getOrDefault("add_headers", Map.of());
        List<String> removeHeaders = (List<String>) config.getOrDefault("remove_headers", List.of());
        Map<String, String> renameHeaders = (Map<String, String>) config.getOrDefault("rename_headers", Map.of());
        Map<String, String> addQueryParams = (Map<String, String>) config.getOrDefault("add_query_params", Map.of());
        List<String> removeQueryParams = (List<String>) config.getOrDefault("remove_query_params", List.of());
        Map<String, String> pathRewrite = (Map<String, String>) config.getOrDefault("path_rewrite", Map.of());

        return (exchange, chain) -> applyRequestTransform(exchange, chain,
                addHeaders, removeHeaders, renameHeaders,
                addQueryParams, removeQueryParams, pathRewrite);
    }

    private Mono<Void> applyRequestTransform(ServerWebExchange exchange, GatewayFilterChain chain,
                                              Map<String, String> addHeaders,
                                              List<String> removeHeaders,
                                              Map<String, String> renameHeaders,
                                              Map<String, String> addQueryParams,
                                              List<String> removeQueryParams,
                                              Map<String, String> pathRewrite) {

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

        // 1. 헤더 추가
        addHeaders.forEach((name, value) -> {
            requestBuilder.header(name, value);
            log.debug("Request transform: added header {}={}", name, value);
        });

        // 2. 헤더 삭제
        removeHeaders.forEach(name -> {
            requestBuilder.headers(headers -> headers.remove(name));
            log.debug("Request transform: removed header {}", name);
        });

        // 3. 헤더 이름 변경
        renameHeaders.forEach((oldName, newName) -> {
            List<String> values = exchange.getRequest().getHeaders().get(oldName);
            if (values != null && !values.isEmpty()) {
                requestBuilder.headers(headers -> {
                    headers.remove(oldName);
                    headers.addAll(newName, values);
                });
                log.debug("Request transform: renamed header {} -> {}", oldName, newName);
            }
        });

        // 4. 쿼리 파라미터 변환 (URI 재구성 필요)
        if (!addQueryParams.isEmpty() || !removeQueryParams.isEmpty()) {
            URI originalUri = exchange.getRequest().getURI();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(originalUri);

            // 쿼리 파라미터 삭제
            removeQueryParams.forEach(param -> uriBuilder.replaceQueryParam(param));

            // 쿼리 파라미터 추가
            addQueryParams.forEach(uriBuilder::queryParam);

            requestBuilder.uri(uriBuilder.build().toUri());
            log.debug("Request transform: modified query params (add={}, remove={})",
                    addQueryParams.keySet(), removeQueryParams);
        }

        // 5. 경로 재작성 (정규식 기반)
        if (!pathRewrite.isEmpty()) {
            String regex = pathRewrite.get("regex");
            String replacement = pathRewrite.get("replacement");
            if (regex != null && replacement != null) {
                String originalPath = exchange.getRequest().getPath().value();
                String newPath = originalPath.replaceAll(regex, replacement);
                if (!originalPath.equals(newPath)) {
                    requestBuilder.path(newPath);
                    log.debug("Request transform: path rewrite {} -> {}", originalPath, newPath);
                }
            }
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(requestBuilder.build())
                .build();

        return chain.filter(mutatedExchange);
    }
}
