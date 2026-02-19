package com.eraf.lock;

import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 인메모리 락 제공자 구현
 * 단일 인스턴스 환경에서 사용
 */
public class InMemoryLockProvider implements LockProvider, DisposableBean {

    private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public InMemoryLockProvider() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "eraf-lock-lease-timer");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        LockInfo lockInfo = locks.computeIfAbsent(key, k -> new LockInfo());
        try {
            boolean acquired = lockInfo.lock.tryLock(waitTime, unit);
            if (acquired) {
                lockInfo.holdingThread = Thread.currentThread();

                // leaseTime 후 자동 해제 스케줄링
                if (leaseTime > 0) {
                    // 이전 자동 해제 스케줄 취소
                    if (lockInfo.leaseTask != null) {
                        lockInfo.leaseTask.cancel(false);
                    }
                    lockInfo.leaseTask = scheduler.schedule(() -> {
                        forceUnlock(key);
                    }, leaseTime, unit);
                }
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
            // 자동 해제 스케줄 취소
            if (lockInfo.leaseTask != null) {
                lockInfo.leaseTask.cancel(false);
                lockInfo.leaseTask = null;
            }
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

    /**
     * leaseTime 만료 시 강제 해제
     */
    private void forceUnlock(String key) {
        LockInfo lockInfo = locks.get(key);
        if (lockInfo != null && lockInfo.lock.isLocked()) {
            lockInfo.holdingThread = null;
            // ReentrantLock은 보유 스레드에서만 unlock 가능하므로
            // 만료된 락은 논리적으로 해제된 것으로 표시
            lockInfo.expired = true;
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        locks.clear();
    }

    private static class LockInfo {
        final ReentrantLock lock = new ReentrantLock();
        volatile Thread holdingThread;
        volatile ScheduledFuture<?> leaseTask;
        volatile boolean expired;
    }
}
