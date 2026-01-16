package com.eraf.gateway.store.memory;

import com.eraf.gateway.domain.RateLimitRecord;
import com.eraf.gateway.repository.RateLimitRecordRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Rate Limit Record Repository 구현체
 * 슬라이딩 윈도우 알고리즘 사용
 */
public class InMemoryRateLimitRecordRepository implements RateLimitRecordRepository {

    private final Map<String, RateLimitRecord> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<RateLimitRecord> findByKey(String key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public RateLimitRecord save(RateLimitRecord record) {
        storage.put(record.getKey(), record);
        return record;
    }

    @Override
    public void deleteByKey(String key) {
        storage.remove(key);
    }

    @Override
    public void cleanupExpired() {
        // 기본적으로 1시간 이상 지난 레코드 정리
        Instant threshold = Instant.now().minusSeconds(3600);
        storage.entrySet().removeIf(entry ->
                entry.getValue().getLastRequest().isBefore(threshold)
        );
    }

    @Override
    public synchronized boolean incrementAndCheck(String key, int windowSeconds, int maxRequests) {
        RateLimitRecord record = storage.get(key);

        if (record == null) {
            // 새 레코드 생성
            record = RateLimitRecord.builder()
                    .key(key)
                    .requestCount(1)
                    .windowStart(Instant.now())
                    .lastRequest(Instant.now())
                    .build();
            storage.put(key, record);
            return true;
        }

        // 윈도우 만료 확인
        if (record.isWindowExpired(windowSeconds)) {
            record.resetWindow();
            storage.put(key, record);
            return true;
        }

        // 요청 수 확인
        if (record.getRequestCount() >= maxRequests) {
            return false;
        }

        // 요청 수 증가
        record.incrementCount();
        storage.put(key, record);
        return true;
    }

    @Override
    public int getCurrentCount(String key) {
        RateLimitRecord record = storage.get(key);
        return record != null ? record.getRequestCount() : 0;
    }

    @Override
    public int getRemainingRequests(String key, int maxRequests) {
        int currentCount = getCurrentCount(key);
        return Math.max(0, maxRequests - currentCount);
    }

    @Override
    public long getResetTimeSeconds(String key, int windowSeconds) {
        RateLimitRecord record = storage.get(key);
        if (record == null) {
            return windowSeconds;
        }

        Instant windowEnd = record.getWindowStart().plusSeconds(windowSeconds);
        long remaining = windowEnd.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
