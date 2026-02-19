package com.eraf.gateway.filter;

import com.eraf.gateway.ErafGatewayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiting Gateway Filter
 * Token Bucket 알고리즘 기반 요청 제한
 */
public class RateLimitGatewayFilter implements GatewayFilter, Ordered {

    private final ErafGatewayProperties.RateLimitConfig config;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitGatewayFilter(ErafGatewayProperties.RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!config.isEnabled()) {
            return chain.filter(exchange);
        }

        String key = getKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(config));

        if (bucket.tryConsume()) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
    }

    private String getKey(ServerWebExchange exchange) {
        // IP 주소 기반 Rate Limiting
        String clientIp = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return clientIp;
    }

    @Override
    public int getOrder() {
        return -100; // 높은 우선순위
    }

    /**
     * Token Bucket 알고리즘 구현
     */
    private static class TokenBucket {
        private final int replenishRate;
        private final int burstCapacity;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTime;

        public TokenBucket(ErafGatewayProperties.RateLimitConfig config) {
            this.replenishRate = config.getReplenishRate();
            this.burstCapacity = config.getBurstCapacity();
            this.tokens = new AtomicInteger(burstCapacity);
            this.lastRefillTime = new AtomicLong(System.nanoTime());
        }

        public boolean tryConsume() {
            refill();
            return tokens.getAndUpdate(current -> current > 0 ? current - 1 : 0) > 0;
        }

        private void refill() {
            long now = System.nanoTime();
            long lastRefill = lastRefillTime.get();
            long elapsedNanos = now - lastRefill;
            long elapsedSeconds = elapsedNanos / 1_000_000_000;

            if (elapsedSeconds > 0 && lastRefillTime.compareAndSet(lastRefill, now)) {
                int tokensToAdd = (int) (elapsedSeconds * replenishRate);
                tokens.updateAndGet(current -> Math.min(current + tokensToAdd, burstCapacity));
            }
        }
    }
}
