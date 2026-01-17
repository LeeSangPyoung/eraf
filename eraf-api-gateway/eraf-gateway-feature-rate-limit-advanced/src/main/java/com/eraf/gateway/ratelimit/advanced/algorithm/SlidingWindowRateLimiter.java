package com.eraf.gateway.ratelimit.advanced.algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Sliding Window 알고리즘 구현
 *
 * 특징:
 * - 시간 윈도우가 요청마다 이동
 * - 정확한 요청 수 제한
 * - 버스트 트래픽 방지
 * - 메모리 사용량이 높음
 */
@Slf4j
public class SlidingWindowRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final int maxRequests;        // 최대 요청 수
    private final int windowSeconds;      // 윈도우 크기 (초)

    public SlidingWindowRateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean allowRequest(String key) {
        SlidingWindow window = windows.computeIfAbsent(key, k -> new SlidingWindow(maxRequests, windowSeconds));
        return window.tryAdd();
    }

    @Override
    public long getRemainingRequests(String key) {
        SlidingWindow window = windows.get(key);
        if (window == null) {
            return maxRequests;
        }
        return window.getRemainingRequests();
    }

    @Override
    public long getResetTimeSeconds(String key) {
        SlidingWindow window = windows.get(key);
        if (window == null) {
            return 0;
        }
        return window.getTimeUntilOldestExpires();
    }

    @Override
    public void reset(String key) {
        windows.remove(key);
        log.debug("Reset sliding window for key: {}", key);
    }

    @Override
    public void resetAll() {
        windows.clear();
        log.debug("Reset all sliding windows");
    }

    /**
     * Sliding Window 내부 클래스
     */
    private static class SlidingWindow {
        private final int maxRequests;
        private final long windowMillis;
        private final ConcurrentLinkedQueue<Long> timestamps;

        public SlidingWindow(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowSeconds * 1000L;
            this.timestamps = new ConcurrentLinkedQueue<>();
        }

        public synchronized boolean tryAdd() {
            removeExpiredTimestamps();

            if (timestamps.size() < maxRequests) {
                timestamps.add(System.currentTimeMillis());
                return true;
            }
            return false;
        }

        public synchronized long getRemainingRequests() {
            removeExpiredTimestamps();
            return Math.max(0, maxRequests - timestamps.size());
        }

        public synchronized long getTimeUntilOldestExpires() {
            removeExpiredTimestamps();

            if (timestamps.isEmpty()) {
                return 0;
            }

            Long oldestTimestamp = timestamps.peek();
            if (oldestTimestamp == null) {
                return 0;
            }

            long now = System.currentTimeMillis();
            long expiresAt = oldestTimestamp + windowMillis;
            return Math.max(0, (expiresAt - now) / 1000);
        }

        private void removeExpiredTimestamps() {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMillis;

            while (!timestamps.isEmpty()) {
                Long timestamp = timestamps.peek();
                if (timestamp != null && timestamp < cutoff) {
                    timestamps.poll();
                } else {
                    break;
                }
            }
        }
    }
}
