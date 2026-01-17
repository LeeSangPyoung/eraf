package com.eraf.gateway.ratelimit.repository;

import com.eraf.gateway.ratelimit.domain.RateLimitRecord;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 Rate Limit Record Repository 구현체
 */
@Slf4j
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
        // 기본적으로 1시간 이상된 레코드 정리
        Instant threshold = Instant.now().minusSeconds(3600);
        int removed = 0;

        Iterator<Map.Entry<String, RateLimitRecord>> iterator = storage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RateLimitRecord> entry = iterator.next();
            if (entry.getValue().getLastRequest().isBefore(threshold)) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Cleaned up {} expired rate limit records", removed);
        }
    }

    @Override
    public synchronized boolean incrementAndCheck(String key, int windowSeconds, int maxRequests) {
        RateLimitRecord record = storage.get(key);
        Instant now = Instant.now();

        if (record == null) {
            // 새로운 레코드 생성
            record = RateLimitRecord.builder()
                    .key(key)
                    .requestCount(1)
                    .windowStart(now)
                    .lastRequest(now)
                    .build();
            storage.put(key, record);
            return true;
        }

        // 윈도우가 만료되었으면 리셋
        if (record.isWindowExpired(windowSeconds)) {
            record.resetWindow();
            storage.put(key, record);
            return true;
        }

        // 최대 요청 수 초과 체크
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
