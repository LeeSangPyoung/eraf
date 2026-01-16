package com.eraf.gateway.store.memory;

import com.eraf.gateway.cache.CachedResponse;
import com.eraf.gateway.cache.ResponseCacheRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 인메모리 Response Cache Repository 구현
 */
@Slf4j
public class InMemoryResponseCacheRepository implements ResponseCacheRepository {

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private final int maxEntries;
    private final ScheduledExecutorService cleanupExecutor;

    public InMemoryResponseCacheRepository() {
        this(10000); // 기본 1만 건
    }

    public InMemoryResponseCacheRepository(int maxEntries) {
        this.maxEntries = maxEntries;

        // 주기적 만료 항목 정리 (1분마다)
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public Optional<CachedResponse> get(String key) {
        CachedResponse cached = cache.get(key);
        if (cached == null) {
            return Optional.empty();
        }

        // 만료 확인
        if (cached.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }

        return Optional.of(cached);
    }

    @Override
    public void put(String key, CachedResponse response) {
        // 최대 개수 초과 시 오래된 것들 정리
        if (cache.size() >= maxEntries) {
            evictOldest();
        }

        cache.put(key, response);
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void evictByPattern(String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");

        cache.keySet().removeIf(key -> key.matches(regex));
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void cleanupExpired() {
        int removed = 0;
        Iterator<Map.Entry<String, CachedResponse>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CachedResponse> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Cleaned up {} expired cache entries", removed);
        }
    }

    private void evictOldest() {
        // 가장 오래 전에 캐시된 항목 찾기
        String oldestKey = null;
        Instant oldestTime = Instant.MAX;

        for (Map.Entry<String, CachedResponse> entry : cache.entrySet()) {
            if (entry.getValue().getCachedAt().isBefore(oldestTime)) {
                oldestTime = entry.getValue().getCachedAt();
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            cache.remove(oldestKey);
            log.debug("Evicted oldest cache entry: {}", oldestKey);
        }
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
