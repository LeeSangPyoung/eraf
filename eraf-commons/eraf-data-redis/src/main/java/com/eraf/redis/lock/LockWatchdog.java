package com.eraf.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 분산 락 Watchdog
 *
 * <p>락이 유지되는 동안 TTL을 자동으로 갱신하여
 * 장시간 실행되는 작업의 락이 만료되는 것을 방지합니다.</p>
 *
 * <p>동작 원리:</p>
 * <ol>
 *     <li>락 획득 시 Watchdog에 등록</li>
 *     <li>TTL의 1/3 간격으로 자동 갱신</li>
 *     <li>락 해제 시 Watchdog에서 제거</li>
 *     <li>소유자가 아닌 경우 갱신하지 않음</li>
 * </ol>
 */
public class LockWatchdog {

    private static final Logger log = LoggerFactory.getLogger(LockWatchdog.class);

    private static final String RENEW_SCRIPT = """
            if redis.call('hget', KEYS[1], 'owner') == ARGV[1] then
                redis.call('pexpire', KEYS[1], ARGV[2])
                return 1
            else
                return 0
            end
            """;

    private final StringRedisTemplate redisTemplate;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> watchedLocks = new ConcurrentHashMap<>();
    private final DefaultRedisScript<Long> renewScript;

    public LockWatchdog(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r, "lock-watchdog");
            thread.setDaemon(true);
            return thread;
        });

        this.renewScript = new DefaultRedisScript<>();
        this.renewScript.setScriptText(RENEW_SCRIPT);
        this.renewScript.setResultType(Long.class);
    }

    /**
     * 락 감시 등록
     *
     * @param lockKey Redis 락 키
     * @param owner 소유자 식별자
     * @param leaseTimeMs 임대 시간 (밀리초)
     */
    public void watch(String lockKey, String owner, long leaseTimeMs) {
        // 기존 감시가 있으면 중복 등록 방지
        if (watchedLocks.containsKey(lockKey + ":" + owner)) {
            return;
        }

        // 갱신 간격 = 임대 시간의 1/3
        long renewInterval = leaseTimeMs / 3;

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> renewLock(lockKey, owner, leaseTimeMs),
                renewInterval,
                renewInterval,
                TimeUnit.MILLISECONDS
        );

        watchedLocks.put(lockKey + ":" + owner, future);
        log.debug("[LockWatchdog] Watching lock: {} owner: {} renewInterval: {}ms",
                lockKey, owner, renewInterval);
    }

    /**
     * 락 감시 해제
     *
     * @param lockKey Redis 락 키
     * @param owner 소유자 식별자
     */
    public void unwatch(String lockKey, String owner) {
        ScheduledFuture<?> future = watchedLocks.remove(lockKey + ":" + owner);
        if (future != null) {
            future.cancel(false);
            log.debug("[LockWatchdog] Unwatched lock: {} owner: {}", lockKey, owner);
        }
    }

    /**
     * 현재 감시 중인 락 수
     */
    public int getWatchedCount() {
        return watchedLocks.size();
    }

    /**
     * 현재 감시 중인 락 키 목록
     */
    public java.util.Set<String> getWatchedKeys() {
        return Collections.unmodifiableSet(watchedLocks.keySet());
    }

    /**
     * 리소스 정리
     */
    public void shutdown() {
        watchedLocks.values().forEach(f -> f.cancel(false));
        watchedLocks.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[LockWatchdog] Shutdown completed");
    }

    /**
     * 락 TTL 갱신
     */
    private void renewLock(String lockKey, String owner, long leaseTimeMs) {
        try {
            Long result = redisTemplate.execute(
                    renewScript,
                    Collections.singletonList(lockKey),
                    owner, String.valueOf(leaseTimeMs)
            );

            if (result != null && result == 1L) {
                log.trace("[LockWatchdog] Renewed lock: {} owner: {} TTL: {}ms",
                        lockKey, owner, leaseTimeMs);
            } else {
                // 소유자가 아니거나 락이 만료됨 → 감시 해제
                log.warn("[LockWatchdog] Lock renewal failed (no longer owner): {} owner: {}",
                        lockKey, owner);
                unwatch(lockKey, owner);
            }
        } catch (Exception e) {
            log.error("[LockWatchdog] Lock renewal error: {} owner: {}", lockKey, owner, e);
        }
    }
}
