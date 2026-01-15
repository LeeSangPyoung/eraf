package com.eraf.core.idempotent;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 멱등성 저장소 구현
 * 단일 인스턴스 환경에서 사용
 */
@Component
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public boolean setIfAbsent(String key, Duration timeout) {
        cleanup();
        Entry entry = new Entry(null, Instant.now().plus(timeout));
        Entry existing = store.putIfAbsent(key, entry);
        return existing == null;
    }

    @Override
    public boolean exists(String key) {
        Entry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public void saveResult(String key, Object result, Duration timeout) {
        store.put(key, new Entry(result, Instant.now().plus(timeout)));
    }

    @Override
    public Optional<Object> getResult(String key) {
        Entry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            return Optional.empty();
        }
        return Optional.ofNullable(entry.result);
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    /**
     * 만료된 엔트리 정리
     */
    private void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(entry -> entry.getValue().expireAt.isBefore(now));
    }

    private record Entry(Object result, Instant expireAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expireAt);
        }
    }
}
