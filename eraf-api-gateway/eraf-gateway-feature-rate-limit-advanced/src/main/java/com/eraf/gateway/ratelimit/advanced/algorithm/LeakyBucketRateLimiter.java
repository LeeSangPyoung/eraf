package com.eraf.gateway.ratelimit.advanced.algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Leaky Bucket 알고리즘 구현
 *
 * 특징:
 * - 일정한 속도로 요청 처리 (누수)
 * - 버스트 트래픽을 평탄화
 * - 큐가 가득 차면 요청 거부
 */
@Slf4j
public class LeakyBucketRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, LeakyBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;           // 버킷의 최대 용량
    private final double leakRate;        // 초당 누수 속도
    private final int windowSeconds;      // 윈도우 크기 (초)

    public LeakyBucketRateLimiter(int capacity, double leakRate, int windowSeconds) {
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean allowRequest(String key) {
        LeakyBucket bucket = buckets.computeIfAbsent(key, k -> new LeakyBucket(capacity, leakRate));
        return bucket.tryAdd();
    }

    @Override
    public long getRemainingRequests(String key) {
        LeakyBucket bucket = buckets.get(key);
        if (bucket == null) {
            return capacity;
        }
        return bucket.getRemainingCapacity();
    }

    @Override
    public long getResetTimeSeconds(String key) {
        LeakyBucket bucket = buckets.get(key);
        if (bucket == null) {
            return 0;
        }
        return bucket.getTimeUntilEmpty();
    }

    @Override
    public void reset(String key) {
        buckets.remove(key);
        log.debug("Reset leaky bucket for key: {}", key);
    }

    @Override
    public void resetAll() {
        buckets.clear();
        log.debug("Reset all leaky buckets");
    }

    /**
     * Leaky Bucket 내부 클래스
     */
    private static class LeakyBucket {
        private final int capacity;
        private final double leakRate;
        private double water;              // 현재 물의 양
        private long lastLeakTimestamp;

        public LeakyBucket(int capacity, double leakRate) {
            this.capacity = capacity;
            this.leakRate = leakRate;
            this.water = 0;
            this.lastLeakTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryAdd() {
            leak();
            if (water < capacity) {
                water++;
                return true;
            }
            return false;
        }

        public synchronized long getRemainingCapacity() {
            leak();
            return (long) (capacity - water);
        }

        public synchronized long getTimeUntilEmpty() {
            leak();
            if (water <= 0) {
                return 0;
            }
            return (long) Math.ceil(water / leakRate);
        }

        private void leak() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastLeakTimestamp;
            double leaked = (timePassed / 1000.0) * leakRate;

            if (leaked > 0) {
                water = Math.max(0, water - leaked);
                lastLeakTimestamp = now;
            }
        }
    }
}
