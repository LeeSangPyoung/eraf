package com.eraf.redis.warming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.DisposableBean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 캐시 워밍 (Cache Warming)
 *
 * <p>애플리케이션 시작 시 자주 사용되는 데이터를 미리 캐시에 로드하여
 * 초기 요청의 응답 시간을 개선합니다.</p>
 *
 * <p>사용법:</p>
 * <pre>
 * {@code @Component}
 * public class MyCacheWarmer {
 *     {@code @Autowired}
 *     private CacheWarmer cacheWarmer;
 *
 *     {@code @PostConstruct}
 *     public void registerWarmers() {
 *         cacheWarmer.register("users", () -> userRepository.findAll());
 *         cacheWarmer.register("products", () -> productRepository.findTop100());
 *     }
 * }
 * </pre>
 */
@Component
public class CacheWarmer implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final List<WarmupTask> warmupTasks = new CopyOnWriteArrayList<>();
    private ExecutorService executorService;

    private boolean warmed = false;

    public CacheWarmer(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, 4);
    }

    public CacheWarmer(RedisTemplate<String, Object> redisTemplate, int parallelThreads) {
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newFixedThreadPool(parallelThreads);
    }

    /**
     * 병렬 스레드 수 설정
     *
     * @param parallelThreads 병렬 스레드 수
     */
    public void setParallelThreads(int parallelThreads) {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        this.executorService = Executors.newFixedThreadPool(parallelThreads);
    }

    @Override
    public void destroy() {
        log.info("[CacheWarmer] Shutting down executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 워밍 작업 등록
     *
     * @param cacheName 캐시 이름
     * @param dataLoader 데이터 로드 함수
     */
    public void register(String cacheName, Supplier<List<?>> dataLoader) {
        warmupTasks.add(new WarmupTask(cacheName, dataLoader, null));
        log.debug("[CacheWarmer] Registered warmup task for cache: {}", cacheName);
    }

    /**
     * 워밍 작업 등록 (TTL 지정)
     *
     * @param cacheName 캐시 이름
     * @param dataLoader 데이터 로드 함수
     * @param ttl TTL (Time To Live)
     */
    public void register(String cacheName, Supplier<List<?>> dataLoader, Duration ttl) {
        warmupTasks.add(new WarmupTask(cacheName, dataLoader, ttl));
        log.debug("[CacheWarmer] Registered warmup task for cache: {} (TTL: {})", cacheName, ttl);
    }

    /**
     * 애플리케이션 시작 시 자동 워밍
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmOnStartup() {
        if (warmed || warmupTasks.isEmpty()) {
            return;
        }

        log.info("[CacheWarmer] Starting cache warming for {} tasks", warmupTasks.size());
        long startTime = System.currentTimeMillis();

        try {
            warmAll();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[CacheWarmer] Cache warming completed in {}ms", duration);
            warmed = true;
        } catch (Exception e) {
            log.error("[CacheWarmer] Cache warming failed", e);
        }
    }

    /**
     * 수동 워밍 (모든 캐시)
     */
    public WarmupResult warmAll() {
        WarmupResult result = new WarmupResult();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (WarmupTask task : warmupTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    warmCache(task);
                    result.recordSuccess(task.cacheName);
                } catch (Exception e) {
                    log.error("[CacheWarmer] Failed to warm cache: {}", task.cacheName, e);
                    result.recordFailure(task.cacheName, e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return result;
    }

    /**
     * 수동 워밍 (특정 캐시)
     */
    public void warmCache(String cacheName) {
        warmupTasks.stream()
                .filter(task -> task.cacheName.equals(cacheName))
                .findFirst()
                .ifPresent(this::warmCache);
    }

    private void warmCache(WarmupTask task) {
        long startTime = System.currentTimeMillis();
        log.info("[CacheWarmer] Warming cache: {}", task.cacheName);

        try {
            List<?> data = task.dataLoader.get();
            if (data == null || data.isEmpty()) {
                log.warn("[CacheWarmer] No data to warm for cache: {}", task.cacheName);
                return;
            }

            // 각 데이터를 캐시에 저장
            for (Object item : data) {
                String key = task.cacheName + "::" + extractKey(item);
                if (task.ttl != null) {
                    redisTemplate.opsForValue().set(key, item, task.ttl);
                } else {
                    redisTemplate.opsForValue().set(key, item);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[CacheWarmer] Warmed cache: {} ({} items in {}ms)",
                    task.cacheName, data.size(), duration);

        } catch (Exception e) {
            log.error("[CacheWarmer] Failed to warm cache: {}", task.cacheName, e);
            throw e;
        }
    }

    /**
     * 객체에서 키 추출 (toString 사용, 커스터마이징 가능)
     */
    private String extractKey(Object item) {
        if (item == null) {
            return "null";
        }

        // ID 필드를 우선적으로 찾기 (리플렉션)
        try {
            var idField = item.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(item);
            return id != null ? id.toString() : item.hashCode() + "";
        } catch (Exception e) {
            // ID 필드 없으면 hashCode 사용
            return String.valueOf(item.hashCode());
        }
    }

    /**
     * 워밍 작업 클래스
     */
    private static class WarmupTask {
        private final String cacheName;
        private final Supplier<List<?>> dataLoader;
        private final Duration ttl;

        public WarmupTask(String cacheName, Supplier<List<?>> dataLoader, Duration ttl) {
            this.cacheName = cacheName;
            this.dataLoader = dataLoader;
            this.ttl = ttl;
        }
    }

    /**
     * 워밍 결과 클래스
     */
    public static class WarmupResult {
        private final List<String> successCaches = new ArrayList<>();
        private final List<String> failedCaches = new ArrayList<>();
        private final List<String> failureReasons = new ArrayList<>();

        public void recordSuccess(String cacheName) {
            successCaches.add(cacheName);
        }

        public void recordFailure(String cacheName, String reason) {
            failedCaches.add(cacheName);
            failureReasons.add(reason);
        }

        public int getSuccessCount() {
            return successCaches.size();
        }

        public int getFailureCount() {
            return failedCaches.size();
        }

        public List<String> getSuccessCaches() {
            return new ArrayList<>(successCaches);
        }

        public List<String> getFailedCaches() {
            return new ArrayList<>(failedCaches);
        }

        @Override
        public String toString() {
            return String.format("WarmupResult{success=%d, failed=%d}",
                    successCaches.size(), failedCaches.size());
        }
    }
}
