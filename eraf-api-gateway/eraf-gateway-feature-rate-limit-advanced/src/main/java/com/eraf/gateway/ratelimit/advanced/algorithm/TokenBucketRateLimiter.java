package com.eraf.gateway.ratelimit.advanced.algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Token Bucket 알고리즘 구현
 * Kong Gateway의 기본 알고리즘
 *
 * 특징:
 * - 일정 속도로 토큰 생성
 * - 버스트 트래픽 허용 (버킷이 가득 찬 경우)
 * - 유연한 트래픽 처리
 */
@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;           // 버킷의 최대 용량
    private final double refillRate;      // 초당 토큰 리필 속도
    private final int windowSeconds;      // 윈도우 크기 (초)

    public TokenBucketRateLimiter(int capacity, double refillRate, int windowSeconds) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean allowRequest(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillRate));
        return bucket.tryConsume();
    }

    @Override
    public long getRemainingRequests(String key) {
        TokenBucket bucket = buckets.get(key);
        if (bucket == null) {
            return capacity;
        }
        return bucket.getAvailableTokens();
    }

    @Override
    public long getResetTimeSeconds(String key) {
        TokenBucket bucket = buckets.get(key);
        if (bucket == null) {
            return 0;
        }
        return bucket.getTimeUntilRefill();
    }

    @Override
    public void reset(String key) {
        buckets.remove(key);
        log.debug("Reset token bucket for key: {}", key);
    }

    @Override
    public void resetAll() {
        buckets.clear();
        log.debug("Reset all token buckets");
    }

    /**
     * Token Bucket 내부 클래스
     */
    private static class TokenBucket {
        private final int capacity;
        private final double refillRate;
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(int capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized long getAvailableTokens() {
            refill();
            return (long) tokens;
        }

        public synchronized long getTimeUntilRefill() {
            if (tokens >= capacity) {
                return 0;
            }
            double tokensNeeded = 1 - tokens;
            return (long) Math.ceil(tokensNeeded / refillRate);
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTimestamp;
            double tokensToAdd = (timePassed / 1000.0) * refillRate;

            if (tokensToAdd > 0) {
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }
}
