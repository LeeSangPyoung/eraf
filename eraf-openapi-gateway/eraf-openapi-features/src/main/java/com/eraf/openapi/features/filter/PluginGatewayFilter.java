package com.eraf.openapi.features.filter;

import com.eraf.openapi.core.domain.GatewayPlugin;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Plugin Gateway Filter Base Interface
 * 각 플러그인 타입별로 구현하여 동적으로 필터 체인에 적용
 */
public interface PluginGatewayFilter extends GatewayFilter {

    /**
     * 플러그인 이름 반환
     */
    String getPluginName();

    /**
     * 플러그인 설정으로 필터 생성
     */
    GatewayFilter createFilter(GatewayPlugin plugin);

    /**
     * 기본 필터 실행 (override 가능)
     */
    @Override
    default Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange);
    }

    /**
     * 우선순위 반환 (작을수록 먼저 실행)
     */
    default int getOrder() {
        return 0;
    }
}
