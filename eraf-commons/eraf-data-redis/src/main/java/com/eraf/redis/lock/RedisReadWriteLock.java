package com.eraf.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 ReadWrite Lock
 *
 * <p>읽기 락은 여러 스레드가 동시에 획득할 수 있으며,
 * 쓰기 락은 독점적으로 하나의 스레드만 획득할 수 있습니다.</p>
 *
 * <p>구조:</p>
 * <ul>
 *     <li>Write Lock: Hash {owner, mode='write'}</li>
 *     <li>Read Lock: Hash {readers (count), mode='read'}</li>
 * </ul>
 *
 * <p>사용법:</p>
 * <pre>
 * RedisReadWriteLock rwLock = new RedisReadWriteLock(redisTemplate);
 *
 * // 읽기 락
 * if (rwLock.tryReadLock("resource:123", 5, 30, TimeUnit.SECONDS)) {
 *     try {
 *         // 읽기 작업
 *     } finally {
 *         rwLock.readUnlock("resource:123");
 *     }
 * }
 *
 * // 쓰기 락
 * if (rwLock.tryWriteLock("resource:123", 5, 30, TimeUnit.SECONDS)) {
 *     try {
 *         // 쓰기 작업
 *     } finally {
 *         rwLock.writeUnlock("resource:123");
 *     }
 * }
 * </pre>
 */
public class RedisReadWriteLock {

    private static final Logger log = LoggerFactory.getLogger(RedisReadWriteLock.class);

    private static final String LOCK_PREFIX = "eraf:rwlock:";

    // 읽기 락 획득: 쓰기 락이 없을 때만 가능
    private static final String READ_LOCK_SCRIPT = """
            local mode = redis.call('hget', KEYS[1], 'mode')
            if mode == false then
                redis.call('hset', KEYS[1], 'mode', 'read')
                redis.call('hset', KEYS[1], 'readers', 1)
                redis.call('pexpire', KEYS[1], ARGV[1])
                return 1
            elseif mode == 'read' then
                redis.call('hincrby', KEYS[1], 'readers', 1)
                redis.call('pexpire', KEYS[1], ARGV[1])
                return 1
            else
                return 0
            end
            """;

    // 읽기 락 해제
    private static final String READ_UNLOCK_SCRIPT = """
            local mode = redis.call('hget', KEYS[1], 'mode')
            if mode == 'read' then
                local readers = redis.call('hincrby', KEYS[1], 'readers', -1)
                if readers <= 0 then
                    redis.call('del', KEYS[1])
                end
                return 1
            else
                return 0
            end
            """;

    // 쓰기 락 획득: 아무 락도 없을 때만 가능
    private static final String WRITE_LOCK_SCRIPT = """
            local mode = redis.call('hget', KEYS[1], 'mode')
            if mode == false then
                redis.call('hset', KEYS[1], 'mode', 'write')
                redis.call('hset', KEYS[1], 'owner', ARGV[2])
                redis.call('pexpire', KEYS[1], ARGV[1])
                return 1
            elseif mode == 'write' and redis.call('hget', KEYS[1], 'owner') == ARGV[2] then
                redis.call('pexpire', KEYS[1], ARGV[1])
                return 1
            else
                return 0
            end
            """;

    // 쓰기 락 해제
    private static final String WRITE_UNLOCK_SCRIPT = """
            local mode = redis.call('hget', KEYS[1], 'mode')
            if mode == 'write' and redis.call('hget', KEYS[1], 'owner') == ARGV[1] then
                redis.call('del', KEYS[1])
                return 1
            else
                return 0
            end
            """;

    // 락 정보 조회
    private static final String INFO_SCRIPT = """
            local mode = redis.call('hget', KEYS[1], 'mode')
            if mode == false then
                return 'NONE'
            elseif mode == 'read' then
                local readers = redis.call('hget', KEYS[1], 'readers')
                return 'READ:' .. readers
            else
                local owner = redis.call('hget', KEYS[1], 'owner')
                return 'WRITE:' .. owner
            end
            """;

    private final StringRedisTemplate redisTemplate;
    private final String instanceId;

    private final DefaultRedisScript<Long> readLockScript;
    private final DefaultRedisScript<Long> readUnlockScript;
    private final DefaultRedisScript<Long> writeLockScript;
    private final DefaultRedisScript<Long> writeUnlockScript;
    private final DefaultRedisScript<String> infoScript;

    public RedisReadWriteLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.instanceId = UUID.randomUUID().toString();

        this.readLockScript = createScript(READ_LOCK_SCRIPT, Long.class);
        this.readUnlockScript = createScript(READ_UNLOCK_SCRIPT, Long.class);
        this.writeLockScript = createScript(WRITE_LOCK_SCRIPT, Long.class);
        this.writeUnlockScript = createScript(WRITE_UNLOCK_SCRIPT, Long.class);
        this.infoScript = createScript(INFO_SCRIPT, String.class);
    }

    /**
     * 읽기 락 획득 시도
     *
     * @param key 락 키
     * @param waitTime 대기 시간
     * @param leaseTime 임대 시간
     * @param unit 시간 단위
     * @return 획득 성공 여부
     */
    public boolean tryReadLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        long leaseMillis = unit.toMillis(leaseTime);
        long waitMillis = unit.toMillis(waitTime);
        long deadline = System.currentTimeMillis() + waitMillis;

        do {
            Long result = redisTemplate.execute(
                    readLockScript,
                    Collections.singletonList(lockKey),
                    String.valueOf(leaseMillis)
            );

            if (result != null && result == 1L) {
                log.debug("[RWLock] Read lock acquired: {}", key);
                return true;
            }

            if (waitMillis <= 0) break;

            try {
                Thread.sleep(Math.min(100, Math.max(10, waitMillis / 10)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (System.currentTimeMillis() < deadline);

        log.debug("[RWLock] Read lock timeout: {}", key);
        return false;
    }

    /**
     * 읽기 락 해제
     */
    public void readUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;

        Long result = redisTemplate.execute(
                readUnlockScript,
                Collections.singletonList(lockKey)
        );

        if (result != null && result == 1L) {
            log.debug("[RWLock] Read lock released: {}", key);
        } else {
            log.warn("[RWLock] Read lock release failed: {}", key);
        }
    }

    /**
     * 쓰기 락 획득 시도
     *
     * @param key 락 키
     * @param waitTime 대기 시간
     * @param leaseTime 임대 시간
     * @param unit 시간 단위
     * @return 획득 성공 여부
     */
    public boolean tryWriteLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        String owner = getOwner();
        long leaseMillis = unit.toMillis(leaseTime);
        long waitMillis = unit.toMillis(waitTime);
        long deadline = System.currentTimeMillis() + waitMillis;

        do {
            Long result = redisTemplate.execute(
                    writeLockScript,
                    Collections.singletonList(lockKey),
                    String.valueOf(leaseMillis), owner
            );

            if (result != null && result == 1L) {
                log.debug("[RWLock] Write lock acquired: {} owner: {}", key, owner);
                return true;
            }

            if (waitMillis <= 0) break;

            try {
                Thread.sleep(Math.min(100, Math.max(10, waitMillis / 10)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } while (System.currentTimeMillis() < deadline);

        log.debug("[RWLock] Write lock timeout: {}", key);
        return false;
    }

    /**
     * 쓰기 락 해제
     */
    public void writeUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        String owner = getOwner();

        Long result = redisTemplate.execute(
                writeUnlockScript,
                Collections.singletonList(lockKey),
                owner
        );

        if (result != null && result == 1L) {
            log.debug("[RWLock] Write lock released: {} owner: {}", key, owner);
        } else {
            log.warn("[RWLock] Write lock release failed: {} owner: {}", key, owner);
        }
    }

    /**
     * 락 정보 조회
     *
     * @param key 락 키
     * @return 락 상태 ("NONE", "READ:count", "WRITE:owner")
     */
    public String getLockInfo(String key) {
        String lockKey = LOCK_PREFIX + key;
        return redisTemplate.execute(
                infoScript,
                Collections.singletonList(lockKey)
        );
    }

    /**
     * 강제 락 해제 (관리 용도)
     */
    public boolean forceUnlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        Boolean deleted = redisTemplate.delete(lockKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.warn("[RWLock] Force unlocked: {}", key);
            return true;
        }
        return false;
    }

    private String getOwner() {
        return instanceId + ":" + Thread.currentThread().getId();
    }

    @SuppressWarnings("unchecked")
    private <T> DefaultRedisScript<T> createScript(String scriptText, Class<T> resultType) {
        DefaultRedisScript<T> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(resultType);
        return script;
    }
}
