package com.eraf.gateway.filter;

import com.eraf.gateway.ErafGatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("RateLimitGatewayFilter 테스트")
class RateLimitGatewayFilterTest {

    private ErafGatewayProperties.RateLimitConfig config;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        config = new ErafGatewayProperties.RateLimitConfig();
        chain = mock(GatewayFilterChain.class);
    }

    private ServerWebExchange createExchange(String ip) {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 8080));
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(response.setComplete()).thenReturn(Mono.empty());

        return exchange;
    }

    @Test
    @DisplayName("비활성화 시 통과")
    void filter_WhenDisabled_ShouldPassThrough() {
        config.setEnabled(false);
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);
        ServerWebExchange exchange = createExchange("10.0.0.1");

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verify(exchange.getResponse(), never()).setStatusCode(any());
    }

    @Test
    @DisplayName("제한 이내 요청 허용")
    void filter_WhenUnderLimit_ShouldAllow() {
        config.setEnabled(true);
        config.setReplenishRate(10);
        config.setBurstCapacity(5);
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);

        ServerWebExchange exchange = createExchange("10.0.0.2");
        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("제한 초과 시 429 반환")
    void filter_WhenExceeded_ShouldReturn429() {
        config.setEnabled(true);
        config.setReplenishRate(1);
        config.setBurstCapacity(2);
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);

        // 버스트 용량(2) 소진
        for (int i = 0; i < 2; i++) {
            ServerWebExchange ex = createExchange("10.0.0.3");
            filter.filter(ex, chain).block();
        }

        // 3번째 요청 → 429
        ServerWebExchange exceeded = createExchange("10.0.0.3");
        filter.filter(exceeded, chain).block();

        verify(exceeded.getResponse()).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("서로 다른 IP는 별도 버킷")
    void filter_DifferentIps_ShouldHaveSeparateBuckets() {
        config.setEnabled(true);
        config.setReplenishRate(1);
        config.setBurstCapacity(1);
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);

        // IP1 토큰 소진
        ServerWebExchange ex1 = createExchange("10.0.0.10");
        filter.filter(ex1, chain).block();
        verify(chain).filter(ex1);

        // IP2는 별도 버킷이므로 통과
        ServerWebExchange ex2 = createExchange("10.0.0.20");
        filter.filter(ex2, chain).block();
        verify(chain).filter(ex2);
    }

    @Test
    @DisplayName("order는 -100")
    void getOrder_ShouldReturnHighPriority() {
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);
        assertThat(filter.getOrder()).isEqualTo(-100);
    }

    @Test
    @DisplayName("remoteAddress가 null이면 unknown 키 사용")
    void filter_WhenNoRemoteAddress_ShouldUseUnknownKey() {
        config.setEnabled(true);
        config.setBurstCapacity(1);
        config.setReplenishRate(1);
        RateLimitGatewayFilter filter = new RateLimitGatewayFilter(config);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getRemoteAddress()).thenReturn(null);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }
}
