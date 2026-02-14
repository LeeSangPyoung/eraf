package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 캐시 메트릭 수집
 * Hit/Miss 비율, 캐시 크기, Eviction 통계
 */
public class CacheMetrics {

    private final MeterRegistry registry;
    private final Map<String, CacheStats> statsMap = new ConcurrentHashMap<>();

    public CacheMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 캐시 등록
     */
    public void registerCache(String cacheName) {
        statsMap.computeIfAbsent(cacheName, name -> {
            CacheStats stats = new CacheStats();

            // Hit 카운터
            stats.hitCounter = Counter.builder("cache.gets")
                    .tag("cache", name)
                    .tag("result", "hit")
                    .description("Cache hits")
                    .register(registry);

            // Miss 카운터
            stats.missCounter = Counter.builder("cache.gets")
                    .tag("cache", name)
                    .tag("result", "miss")
                    .description("Cache misses")
                    .register(registry);

            // Put 카운터
            stats.putCounter = Counter.builder("cache.puts")
                    .tag("cache", name)
                    .description("Cache puts")
                    .register(registry);

            // Eviction 카운터
            stats.evictionCounter = Counter.builder("cache.evictions")
                    .tag("cache", name)
                    .description("Cache evictions")
                    .register(registry);

            // 캐시 크기 게이지
            Gauge.builder("cache.size", stats.size, AtomicLong::doubleValue)
                    .tag("cache", name)
                    .description("Cache size")
                    .register(registry);

            // Hit 비율 게이지 (0.0 ~ 1.0)
            Gauge.builder("cache.hit.ratio", stats, s -> {
                long total = s.getHits() + s.getMisses();
                return total == 0 ? 0.0 : (double) s.getHits() / total;
            })
                    .tag("cache", name)
                    .description("Cache hit ratio")
                    .register(registry);

            return stats;
        });
    }

    /**
     * 캐시 Hit 기록
     */
    public void recordHit(String cacheName) {
        CacheStats stats = getOrCreate(cacheName);
        stats.hitCounter.increment();
        stats.totalHits.incrementAndGet();
    }

    /**
     * 캐시 Miss 기록
     */
    public void recordMiss(String cacheName) {
        CacheStats stats = getOrCreate(cacheName);
        stats.missCounter.increment();
        stats.totalMisses.incrementAndGet();
    }

    /**
     * 캐시 Put 기록
     */
    public void recordPut(String cacheName) {
        CacheStats stats = getOrCreate(cacheName);
        stats.putCounter.increment();
        stats.size.incrementAndGet();
    }

    /**
     * 캐시 Eviction 기록
     */
    public void recordEviction(String cacheName) {
        CacheStats stats = getOrCreate(cacheName);
        stats.evictionCounter.increment();
        stats.size.decrementAndGet();
    }

    /**
     * 캐시 크기 업데이트
     */
    public void setSize(String cacheName, long size) {
        CacheStats stats = getOrCreate(cacheName);
        stats.size.set(size);
    }

    /**
     * 캐시 로드 시간 측정
     */
    public Timer.Sample startLoad(String cacheName) {
        return Timer.start(registry);
    }

    /**
     * 캐시 로드 완료
     */
    public long stopLoad(Timer.Sample sample, String cacheName, boolean success) {
        return sample.stop(Timer.builder("cache.load.time")
                .tag("cache", cacheName)
                .tag("result", success ? "success" : "failure")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry));
    }

    /**
     * 캐시 통계 조회
     */
    public CacheStats getStats(String cacheName) {
        return statsMap.get(cacheName);
    }

    /**
     * 캐시 제거
     */
    public void unregisterCache(String cacheName) {
        statsMap.remove(cacheName);
    }

    /**
     * 캐시 통계 가져오기 또는 생성
     */
    private CacheStats getOrCreate(String cacheName) {
        CacheStats stats = statsMap.get(cacheName);
        if (stats == null) {
            registerCache(cacheName);
            stats = statsMap.get(cacheName);
        }
        return stats;
    }

    /**
     * 캐시 통계 클래스
     */
    public static class CacheStats {
        private Counter hitCounter;
        private Counter missCounter;
        private Counter putCounter;
        private Counter evictionCounter;
        private final AtomicLong size = new AtomicLong(0);
        private final AtomicLong totalHits = new AtomicLong(0);
        private final AtomicLong totalMisses = new AtomicLong(0);

        public long getHits() {
            return totalHits.get();
        }

        public long getMisses() {
            return totalMisses.get();
        }

        public long getSize() {
            return size.get();
        }

        public double getHitRatio() {
            long total = totalHits.get() + totalMisses.get();
            return total == 0 ? 0.0 : (double) totalHits.get() / total;
        }

        public double getMissRatio() {
            return 1.0 - getHitRatio();
        }
    }
}
