package com.eraf.gateway.ratelimit.advanced.algorithm;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fixed Window 알고리즘 구현
 *
 * 특징:
 * - 고정된 시간 윈도우 사용
 * - 간단하고 효율적
 * - 낮은 메모리 사용
 * - 윈도우 경계에서 버스트 가능 (단점)
 */
@Slf4j
public class FixedWindowRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, FixedWindow> windows = new ConcurrentHashMap<>();
    private final int maxRequests;        // 최대 요청 수
    private final int windowSeconds;      // 윈도우 크기 (초)

    public FixedWindowRateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public boolean allowRequest(String key) {
        FixedWindow window = windows.computeIfAbsent(key, k -> new FixedWindow(maxRequests, windowSeconds));
        return window.tryIncrement();
    }

    @Override
    public long getRemainingRequests(String key) {
        FixedWindow window = windows.get(key);
        if (window == null) {
            return maxRequests;
        }
        return window.getRemainingRequests();
    }

    @Override
    public long getResetTimeSeconds(String key) {
        FixedWindow window = windows.get(key);
        if (window == null) {
            return windowSeconds;
        }
        return window.getTimeUntilReset();
    }

    @Override
    public void reset(String key) {
        windows.remove(key);
        log.debug("Reset fixed window for key: {}", key);
    }

    @Override
    public void resetAll() {
        windows.clear();
        log.debug("Reset all fixed windows");
    }

    /**
     * Fixed Window 내부 클래스
     */
    private static class FixedWindow {
        private final int maxRequests;
        private final long windowMillis;
        private final AtomicInteger counter;
        private final AtomicLong windowStart;

        public FixedWindow(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowSeconds * 1000L;
            this.counter = new AtomicInteger(0);
            this.windowStart = new AtomicLong(getCurrentWindow());
        }

        public boolean tryIncrement() {
            long currentWindow = getCurrentWindow();
            long storedWindow = windowStart.get();

            // 새로운 윈도우인 경우 리셋
            if (currentWindow > storedWindow) {
                synchronized (this) {
                    if (windowStart.compareAndSet(storedWindow, currentWindow)) {
                        counter.set(0);
                    }
                }
            }

            // 카운터 증가
            int current = counter.incrementAndGet();
            if (current <= maxRequests) {
                return true;
            } else {
                counter.decrementAndGet(); // 롤백
                return false;
            }
        }

        public long getRemainingRequests() {
            checkAndResetWindow();
            return Math.max(0, maxRequests - counter.get());
        }

        public long getTimeUntilReset() {
            long currentWindow = getCurrentWindow();
            long nextWindow = currentWindow + windowMillis;
            long now = System.currentTimeMillis();
            return Math.max(0, (nextWindow - now) / 1000);
        }

        private void checkAndResetWindow() {
            long currentWindow = getCurrentWindow();
            long storedWindow = windowStart.get();

            if (currentWindow > storedWindow) {
                synchronized (this) {
                    if (windowStart.compareAndSet(storedWindow, currentWindow)) {
                        counter.set(0);
                    }
                }
            }
        }

        private long getCurrentWindow() {
            long now = System.currentTimeMillis();
            return (now / windowMillis) * windowMillis;
        }
    }
}
