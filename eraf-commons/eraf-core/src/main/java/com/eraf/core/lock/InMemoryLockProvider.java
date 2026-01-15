package com.eraf.core.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 인메모리 락 제공자 구현
 * 단일 인스턴스 환경에서 사용
 */
public class InMemoryLockProvider implements LockProvider {

    private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        LockInfo lockInfo = locks.computeIfAbsent(key, k -> new LockInfo());
        try {
            boolean acquired = lockInfo.lock.tryLock(waitTime, unit);
            if (acquired) {
                lockInfo.holdingThread = Thread.currentThread();
                // leaseTime 후 자동 해제를 위한 스케줄링은 생략 (단순화)
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean tryLock(String key) {
        LockInfo lockInfo = locks.computeIfAbsent(key, k -> new LockInfo());
        boolean acquired = lockInfo.lock.tryLock();
        if (acquired) {
            lockInfo.holdingThread = Thread.currentThread();
        }
        return acquired;
    }

    @Override
    public void unlock(String key) {
        LockInfo lockInfo = locks.get(key);
        if (lockInfo != null && lockInfo.lock.isHeldByCurrentThread()) {
            lockInfo.holdingThread = null;
            lockInfo.lock.unlock();
        }
    }

    @Override
    public boolean isLocked(String key) {
        LockInfo lockInfo = locks.get(key);
        return lockInfo != null && lockInfo.lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String key) {
        LockInfo lockInfo = locks.get(key);
        return lockInfo != null && lockInfo.lock.isHeldByCurrentThread();
    }

    private static class LockInfo {
        final ReentrantLock lock = new ReentrantLock();
        volatile Thread holdingThread;
    }
}
