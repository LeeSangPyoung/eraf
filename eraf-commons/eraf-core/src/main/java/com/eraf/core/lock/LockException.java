package com.eraf.core.lock;

/**
 * 락 관련 예외
 */
public class LockException extends RuntimeException {

    private final String lockKey;

    public LockException(String message) {
        super(message);
        this.lockKey = null;
    }

    public LockException(String message, String lockKey) {
        super(message);
        this.lockKey = lockKey;
    }

    public LockException(String message, String lockKey, Throwable cause) {
        super(message, cause);
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }

    public static LockException timeout(String key, String message) {
        return new LockException(message, key);
    }

    public static LockException acquireFailed(String key) {
        return new LockException("락 획득에 실패했습니다: " + key, key);
    }

    public static LockException releaseFailed(String key) {
        return new LockException("락 해제에 실패했습니다: " + key, key);
    }
}
