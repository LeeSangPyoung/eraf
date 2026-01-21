package com.eraf.starter.redis;

import com.eraf.core.lock.LockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 락 제공자
 * Lua 스크립트를 사용한 원자적 락 연산
 */
public class RedisLockProvider implements LockProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisLockProvider.class);

    private static final String LOCK_PREFIX = "eraf:lock:";

    // Lua script for acquiring lock atomically
    private static final String ACQUIRE_SCRIPT = """
            if redis.call('exists', KEYS[1]) == 0 then
                redis.call('hset', KEYS[1], 'owner', ARGV[1])
                redis.call('hset', KEYS[1], 'count', 1)
                redis.call('pexpire', KEYS[1], ARGV[2])
                return 1
            elseif redis.call('hget', KEYS[1], 'owner') == ARGV[1] then
                redis.call('hincrby', KEYS[1], 'count', 1)
                redis.call('pexpire', KEYS[1], ARGV[2])
                return 1
            else
                return 0
            end
            """;

    // Lua script for releasing lock atomically
    private static final String RELEASE_SCRIPT = """
            if redis.call('hget', KEYS[1], 'owner') == ARGV[1] then
                local count = redis.call('hincrby', KEYS[1], 'count', -1)
                if count <= 0 then
                    redis.call('del', KEYS[1])
                    return 1
                else
                    return 1
                end
            else
                return 0
            end
            """;

    private final StringRedisTemplate redisTemplate;
    private final String instanceId;
    private final Map<String, String> threadLockOwners = new ConcurrentHashMap<>();

    private final DefaultRedisScript<Long> acquireScript;
    private final DefaultRedisScript<Long> releaseScript;

    public RedisLockProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString();

        this.acquireScript = new DefaultRedisScript<>();
        this.acquireScript.setScriptText(ACQUIRE_SCRIPT);
        this.acquireScript.setResultType(Long.class);

        this.releaseScript = new DefaultRedisScript<>();
        this.releaseScript.setScriptText(RELEASE_SCRIPT);
        this.releaseScript.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        String owner = getOwner();
        long leaseMillis = unit.toMillis(leaseTime);
        long waitMillis = unit.toMillis(waitTime);
        long deadline = System.currentTimeMillis() + waitMillis;

        while (System.currentTimeMillis() < deadline) {
            Long result = redisTemplate.execute(
                    acquireScript,
                    Collections.singletonList(lockKey),
                    owner, String.valueOf(leaseMillis)
            );

            if (result != null && result == 1L) {
                threadLockOwners.put(key, owner);
                log.debug("락 획득 성공: key={}, owner={}", key, owner);
                return true;
            }

            // 짧은 대기 후 재시도
            try {
                Thread.sleep(Math.min(100, Math.max(10, waitMillis / 10)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.debug("락 획득 실패 (타임아웃): key={}", key);
        return false;
    }

    @Override
    public boolean tryLock(String key) {
        return tryLock(key, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String owner = threadLockOwners.get(key);

        if (owner == null) {
            log.warn("락 해제 시도: 소유자 정보 없음, key={}", key);
            return;
        }

        Long result = redisTemplate.execute(
                releaseScript,
                Collections.singletonList(lockKey),
                owner
        );

        if (result != null && result == 1L) {
            threadLockOwners.remove(key);
            log.debug("락 해제 성공: key={}, owner={}", key, owner);
        } else {
            log.warn("락 해제 실패: key={}, owner={}", key, owner);
        }
    }

    @Override
    public boolean isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    @Override
    public boolean isHeldByCurrentThread(String key) {
        String lockKey = LOCK_PREFIX + key;
        String owner = getOwner();

        Object currentOwner = redisTemplate.opsForHash().get(lockKey, "owner");
        return owner.equals(currentOwner);
    }

    private String getOwner() {
        // 인스턴스 ID + 스레드 ID로 고유 소유자 식별
        return instanceId + ":" + Thread.currentThread().getId();
    }
}
