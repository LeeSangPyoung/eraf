package com.eraf.core.resilience;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rate Limiter 구현 (Token Bucket 알고리즘)
 *
 * 사용 예:
 * <pre>
 * RateLimiter limiter = RateLimiter.builder("api")
 *     .permitsPerSecond(100)
 *     .maxBurstSize(150)
 *     .build();
 *
 * if (limiter.tryAcquire()) {
 *     // 요청 처리
 * } else {
 *     // 요청 거부
 * }
 * </pre>
 */
public class RateLimiter {

    private final String name;
    private final double permitsPerSecond;
    private final long maxPermits;
    private final ReentrantLock lock = new ReentrantLock();

    private double storedPermits;
    private long nextFreeTicketMicros;

    private RateLimiter(String name, double permitsPerSecond, long maxBurstSize) {
        this.name = name;
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = maxBurstSize > 0 ? maxBurstSize : (long) permitsPerSecond;
        this.storedPermits = maxPermits;
        this.nextFreeTicketMicros = nowMicros();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * 허가 획득 시도 (비차단)
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 여러 허가 획득 시도 (비차단)
     */
    public boolean tryAcquire(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }

        lock.lock();
        try {
            long nowMicros = nowMicros();
            resync(nowMicros);

            if (storedPermits >= permits) {
                storedPermits -= permits;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 허가 획득 (차단)
     */
    public void acquire() throws InterruptedException {
        acquire(1);
    }

    /**
     * 여러 허가 획득 (차단)
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }

        long waitMicros;
        lock.lock();
        try {
            long nowMicros = nowMicros();
            resync(nowMicros);

            if (storedPermits >= permits) {
                storedPermits -= permits;
                return;
            }

            double permitsNeeded = permits - storedPermits;
            waitMicros = (long) (permitsNeeded / permitsPerSecond * 1_000_000);
            storedPermits = 0;
            nextFreeTicketMicros = nowMicros + waitMicros;
        } finally {
            lock.unlock();
        }

        if (waitMicros > 0) {
            Thread.sleep(waitMicros / 1000, (int) ((waitMicros % 1000) * 1000));
        }
    }

    /**
     * 현재 사용 가능한 허가 수
     */
    public double getAvailablePermits() {
        lock.lock();
        try {
            resync(nowMicros());
            return storedPermits;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 초당 허가 수
     */
    public double getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public String getName() {
        return name;
    }

    private void resync(long nowMicros) {
        if (nowMicros > nextFreeTicketMicros) {
            double newPermits = (nowMicros - nextFreeTicketMicros) / 1_000_000.0 * permitsPerSecond;
            storedPermits = Math.min(maxPermits, storedPermits + newPermits);
            nextFreeTicketMicros = nowMicros;
        }
    }

    private long nowMicros() {
        return System.nanoTime() / 1000;
    }

    public static class Builder {
        private final String name;
        private double permitsPerSecond = 10.0;
        private long maxBurstSize = 0;

        public Builder(String name) {
            this.name = name;
        }

        /**
         * 초당 허가 수 (기본값: 10)
         */
        public Builder permitsPerSecond(double permitsPerSecond) {
            this.permitsPerSecond = permitsPerSecond;
            return this;
        }

        /**
         * 최대 버스트 크기 (기본값: permitsPerSecond)
         */
        public Builder maxBurstSize(long maxBurstSize) {
            this.maxBurstSize = maxBurstSize;
            return this;
        }

        public RateLimiter build() {
            return new RateLimiter(name, permitsPerSecond, maxBurstSize);
        }
    }

    /**
     * Rate Limiter 레지스트리
     */
    public static class Registry {
        private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
        private final double defaultPermitsPerSecond;
        private final long defaultMaxBurstSize;

        public Registry() {
            this(100.0, 150);
        }

        public Registry(double defaultPermitsPerSecond, long defaultMaxBurstSize) {
            this.defaultPermitsPerSecond = defaultPermitsPerSecond;
            this.defaultMaxBurstSize = defaultMaxBurstSize;
        }

        public RateLimiter getOrCreate(String name) {
            return limiters.computeIfAbsent(name, n ->
                    RateLimiter.builder(n)
                            .permitsPerSecond(defaultPermitsPerSecond)
                            .maxBurstSize(defaultMaxBurstSize)
                            .build()
            );
        }

        public RateLimiter getOrCreate(String name, double permitsPerSecond, long maxBurstSize) {
            return limiters.computeIfAbsent(name, n ->
                    RateLimiter.builder(n)
                            .permitsPerSecond(permitsPerSecond)
                            .maxBurstSize(maxBurstSize)
                            .build()
            );
        }

        public RateLimiter get(String name) {
            return limiters.get(name);
        }

        public void remove(String name) {
            limiters.remove(name);
        }
    }
}
