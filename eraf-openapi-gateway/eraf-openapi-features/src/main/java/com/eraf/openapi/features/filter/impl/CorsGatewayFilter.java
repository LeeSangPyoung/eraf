package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * CORS Gateway Filter
 * Cross-Origin Resource Sharing 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CorsGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "cors";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // CORS 설정 추출
        List<String> allowedOrigins = (List<String>) config.getOrDefault("allowed_origins", List.of("*"));
        List<String> allowedMethods = (List<String>) config.getOrDefault("allowed_methods",
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        List<String> allowedHeaders = (List<String>) config.getOrDefault("allowed_headers", List.of("*"));
        List<String> exposedHeaders = (List<String>) config.getOrDefault("exposed_headers", List.of());
        boolean allowCredentials = (boolean) config.getOrDefault("allow_credentials", true);
        int maxAgeSeconds = ((Number) config.getOrDefault("max_age_seconds", 3600)).intValue();

        return (exchange, chain) -> applyCors(exchange, chain, allowedOrigins, allowedMethods,
                allowedHeaders, exposedHeaders, allowCredentials, maxAgeSeconds);
    }

    private Mono<Void> applyCors(ServerWebExchange exchange, GatewayFilterChain chain,
                                   List<String> allowedOrigins, List<String> allowedMethods,
                                   List<String> allowedHeaders, List<String> exposedHeaders,
                                   boolean allowCredentials, int maxAgeSeconds) {

        String origin = exchange.getRequest().getHeaders().getOrigin();
        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

        // Origin 검증
        if (origin != null && (allowedOrigins.contains("*") || allowedOrigins.contains(origin))) {
            responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);

            if (allowCredentials) {
                responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }

            // Preflight 요청 처리
            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        String.join(", ", allowedMethods));
                responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        String.join(", ", allowedHeaders));
                responseHeaders.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAgeSeconds));

                exchange.getResponse().setStatusCode(HttpStatus.OK);
                return exchange.getResponse().setComplete();
            }

            // Exposed Headers 설정
            if (!exposedHeaders.isEmpty()) {
                responseHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        String.join(", ", exposedHeaders));
            }
        }

        return chain.filter(exchange);
    }
}
