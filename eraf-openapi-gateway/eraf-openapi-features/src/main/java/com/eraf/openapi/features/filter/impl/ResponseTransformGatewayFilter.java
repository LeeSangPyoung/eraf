package com.eraf.openapi.features.filter.impl;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Response Transform Gateway Filter
 * 응답 헤더 변환
 *
 * Config 예시:
 * {
 *   "add_headers": { "X-Powered-By": "ERAF Gateway", "X-Response-Time": "${elapsed}" },
 *   "remove_headers": ["Server", "X-Upstream-Internal"],
 *   "rename_headers": { "X-Old-Response": "X-New-Response" },
 *   "override_status": 200
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseTransformGatewayFilter implements PluginGatewayFilter, Ordered {

    @Override
    public String getPluginName() {
        return "response-transformer";
    }

    @Override
    public int getOrder() {
        // 응답 처리이므로 필터 체인에서 나중에 등록 (역순 실행 시 먼저 처리됨)
        return Ordered.LOWEST_PRECEDENCE - 50;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        Map<String, String> addHeaders = (Map<String, String>) config.getOrDefault("add_headers", Map.of());
        List<String> removeHeaders = (List<String>) config.getOrDefault("remove_headers", List.of());
        Map<String, String> renameHeaders = (Map<String, String>) config.getOrDefault("rename_headers", Map.of());
        Number overrideStatus = (Number) config.get("override_status");

        return (exchange, chain) -> applyResponseTransform(exchange, chain,
                addHeaders, removeHeaders, renameHeaders, overrideStatus);
    }

    private Mono<Void> applyResponseTransform(ServerWebExchange exchange, GatewayFilterChain chain,
                                               Map<String, String> addHeaders,
                                               List<String> removeHeaders,
                                               Map<String, String> renameHeaders,
                                               Number overrideStatus) {

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

            // 1. 헤더 추가 (${elapsed} 변수 치환 지원)
            long elapsed = System.currentTimeMillis() - startTime;
            addHeaders.forEach((name, value) -> {
                String resolvedValue = value.replace("${elapsed}", String.valueOf(elapsed));
                responseHeaders.set(name, resolvedValue);
                log.debug("Response transform: added header {}={}", name, resolvedValue);
            });

            // 2. 헤더 삭제
            removeHeaders.forEach(name -> {
                responseHeaders.remove(name);
                log.debug("Response transform: removed header {}", name);
            });

            // 3. 헤더 이름 변경
            renameHeaders.forEach((oldName, newName) -> {
                List<String> values = responseHeaders.get(oldName);
                if (values != null && !values.isEmpty()) {
                    responseHeaders.remove(oldName);
                    responseHeaders.addAll(newName, values);
                    log.debug("Response transform: renamed header {} -> {}", oldName, newName);
                }
            });

            // 4. 상태 코드 오버라이드
            if (overrideStatus != null) {
                exchange.getResponse().setRawStatusCode(overrideStatus.intValue());
                log.debug("Response transform: overrode status to {}", overrideStatus);
            }
        }));
    }
}
