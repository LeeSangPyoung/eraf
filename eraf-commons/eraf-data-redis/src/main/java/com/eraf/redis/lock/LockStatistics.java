package com.eraf.redis.lock;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 분산 락 통계 추적기
 *
 * <p>락 획득/해제/타임아웃 통계를 추적합니다.</p>
 */
public class LockStatistics {

    private final AtomicLong totalAcquired = new AtomicLong(0);
    private final AtomicLong totalReleased = new AtomicLong(0);
    private final AtomicLong totalTimeouts = new AtomicLong(0);
    private final AtomicLong totalWaitTimeMs = new AtomicLong(0);
    private final AtomicLong totalHoldTimeMs = new AtomicLong(0);

    private final Map<String, LockKeyStats> keyStats = new ConcurrentHashMap<>();
    private final Map<String, LockHoldInfo> activeLocks = new ConcurrentHashMap<>();

    private final Instant startTime = Instant.now();

    /**
     * 락 획득 기록
     *
     * @param key 락 키
     * @param owner 소유자
     * @param waitTimeMs 대기 시간 (밀리초)
     */
    public void recordAcquire(String key, String owner, long waitTimeMs) {
        totalAcquired.incrementAndGet();
        totalWaitTimeMs.addAndGet(waitTimeMs);

        getOrCreateKeyStats(key).recordAcquire(waitTimeMs);
        activeLocks.put(key, new LockHoldInfo(key, owner, Instant.now()));
    }

    /**
     * 락 해제 기록
     */
    public void recordRelease(String key) {
        totalReleased.incrementAndGet();

        LockHoldInfo holdInfo = activeLocks.remove(key);
        if (holdInfo != null) {
            long holdTimeMs = Duration.between(holdInfo.acquiredAt, Instant.now()).toMillis();
            totalHoldTimeMs.addAndGet(holdTimeMs);
            getOrCreateKeyStats(key).recordRelease(holdTimeMs);
        }
    }

    /**
     * 락 타임아웃 기록
     */
    public void recordTimeout(String key) {
        totalTimeouts.incrementAndGet();
        getOrCreateKeyStats(key).recordTimeout();
    }

    /**
     * 전역 통계 조회
     */
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long acquired = totalAcquired.get();
        long released = totalReleased.get();
        long timeouts = totalTimeouts.get();
        long totalAttempts = acquired + timeouts;

        stats.put("totalAcquired", acquired);
        stats.put("totalReleased", released);
        stats.put("totalTimeouts", timeouts);
        stats.put("totalAttempts", totalAttempts);
        stats.put("successRate", totalAttempts > 0 ? (double) acquired / totalAttempts * 100 : 0.0);
        stats.put("averageWaitTimeMs", acquired > 0 ? (double) totalWaitTimeMs.get() / acquired : 0.0);
        stats.put("averageHoldTimeMs", released > 0 ? (double) totalHoldTimeMs.get() / released : 0.0);
        stats.put("activeLockCount", activeLocks.size());
        stats.put("trackedKeyCount", keyStats.size());
        stats.put("uptimeSeconds", Duration.between(startTime, Instant.now()).getSeconds());

        return stats;
    }

    /**
     * 특정 키 통계 조회
     */
    public Map<String, Object> getKeyStatistics(String key) {
        LockKeyStats stats = keyStats.get(key);
        if (stats == null) {
            return Map.of("exists", false);
        }
        return stats.toMap();
    }

    /**
     * 현재 활성 락 목록 조회
     */
    public List<Map<String, Object>> getActiveLocks() {
        List<Map<String, Object>> result = new ArrayList<>();
        activeLocks.forEach((key, info) -> {
            Map<String, Object> lockInfo = new LinkedHashMap<>();
            lockInfo.put("key", info.key);
            lockInfo.put("owner", info.owner);
            lockInfo.put("acquiredAt", info.acquiredAt.toString());
            lockInfo.put("holdTimeMs", Duration.between(info.acquiredAt, Instant.now()).toMillis());
            result.add(lockInfo);
        });
        return result;
    }

    /**
     * 통계 초기화
     */
    public void reset() {
        totalAcquired.set(0);
        totalReleased.set(0);
        totalTimeouts.set(0);
        totalWaitTimeMs.set(0);
        totalHoldTimeMs.set(0);
        keyStats.clear();
    }

    private LockKeyStats getOrCreateKeyStats(String key) {
        return keyStats.computeIfAbsent(key, k -> new LockKeyStats(k));
    }

    /**
     * 키별 통계
     */
    private static class LockKeyStats {
        private final String key;
        private final AtomicLong acquireCount = new AtomicLong(0);
        private final AtomicLong releaseCount = new AtomicLong(0);
        private final AtomicLong timeoutCount = new AtomicLong(0);
        private final AtomicLong totalWaitTimeMs = new AtomicLong(0);
        private final AtomicLong totalHoldTimeMs = new AtomicLong(0);
        private final AtomicLong maxWaitTimeMs = new AtomicLong(0);
        private final AtomicLong maxHoldTimeMs = new AtomicLong(0);

        LockKeyStats(String key) {
            this.key = key;
        }

        void recordAcquire(long waitTimeMs) {
            acquireCount.incrementAndGet();
            totalWaitTimeMs.addAndGet(waitTimeMs);
            maxWaitTimeMs.accumulateAndGet(waitTimeMs, Math::max);
        }

        void recordRelease(long holdTimeMs) {
            releaseCount.incrementAndGet();
            totalHoldTimeMs.addAndGet(holdTimeMs);
            maxHoldTimeMs.accumulateAndGet(holdTimeMs, Math::max);
        }

        void recordTimeout() {
            timeoutCount.incrementAndGet();
        }

        Map<String, Object> toMap() {
            long acquired = acquireCount.get();
            long released = releaseCount.get();
            long timeouts = timeoutCount.get();
            long totalAttempts = acquired + timeouts;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", key);
            map.put("acquireCount", acquired);
            map.put("releaseCount", released);
            map.put("timeoutCount", timeouts);
            map.put("successRate", totalAttempts > 0 ? (double) acquired / totalAttempts * 100 : 0.0);
            map.put("averageWaitTimeMs", acquired > 0 ? (double) totalWaitTimeMs.get() / acquired : 0.0);
            map.put("maxWaitTimeMs", maxWaitTimeMs.get());
            map.put("averageHoldTimeMs", released > 0 ? (double) totalHoldTimeMs.get() / released : 0.0);
            map.put("maxHoldTimeMs", maxHoldTimeMs.get());
            return map;
        }
    }

    /**
     * 활성 락 정보
     */
    private static class LockHoldInfo {
        final String key;
        final String owner;
        final Instant acquiredAt;

        LockHoldInfo(String key, String owner, Instant acquiredAt) {
            this.key = key;
            this.owner = owner;
            this.acquiredAt = acquiredAt;
        }
    }
}
