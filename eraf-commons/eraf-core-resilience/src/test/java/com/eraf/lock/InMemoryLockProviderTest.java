package com.eraf.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * In-Memory Lock Provider Unit Tests
 */
class InMemoryLockProviderTest {

    private InMemoryLockProvider lockProvider;

    @BeforeEach
    void setUp() {
        lockProvider = new InMemoryLockProvider();
    }

    @Test
    void shouldAcquireLockSuccessfully() {
        // when
        boolean acquired = lockProvider.tryLock("lock1");

        // then
        assertThat(acquired).isTrue();
        assertThat(lockProvider.isLocked("lock1")).isTrue();
        assertThat(lockProvider.isHeldByCurrentThread("lock1")).isTrue();
    }

    @Test
    void shouldFailToAcquireAlreadyLockedLock() throws InterruptedException {
        // given
        lockProvider.tryLock("lock1");

        // when - Try from a different thread
        AtomicBoolean acquired = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            acquired.set(lockProvider.tryLock("lock1"));
        });
        thread.start();
        thread.join();

        // then
        assertThat(acquired.get()).isFalse();
    }

    @Test
    void shouldUnlockSuccessfully() {
        // given
        lockProvider.tryLock("lock1");

        // when
        lockProvider.unlock("lock1");

        // then
        assertThat(lockProvider.isLocked("lock1")).isFalse();
    }

    @Test
    void shouldAcquireLockWithTimeout() {
        // when
        boolean acquired = lockProvider.tryLock("lock1", 1, 10, TimeUnit.SECONDS);

        // then
        assertThat(acquired).isTrue();
        assertThat(lockProvider.isLocked("lock1")).isTrue();
    }

    @Test
    void shouldTimeoutWhenWaitingForLock() throws InterruptedException {
        // given
        lockProvider.tryLock("lock1");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean acquired = new AtomicBoolean(false);

        // when
        Thread thread = new Thread(() -> {
            latch.countDown();
            acquired.set(lockProvider.tryLock("lock1", 100, 10, TimeUnit.MILLISECONDS));
        });
        thread.start();
        latch.await();
        thread.join();

        // then
        assertThat(acquired.get()).isFalse();
    }

    @Test
    void shouldAllowLockAcquisitionAfterUnlock() {
        // given
        lockProvider.tryLock("lock1");
        lockProvider.unlock("lock1");

        // when
        boolean acquired = lockProvider.tryLock("lock1");

        // then
        assertThat(acquired).isTrue();
    }

    @Test
    void shouldCheckIfLockIsHeldByCurrentThread() throws InterruptedException {
        // given
        lockProvider.tryLock("lock1");
        AtomicBoolean heldByOtherThread = new AtomicBoolean(false);

        // when
        Thread thread = new Thread(() -> {
            heldByOtherThread.set(lockProvider.isHeldByCurrentThread("lock1"));
        });
        thread.start();
        thread.join();

        // then
        assertThat(lockProvider.isHeldByCurrentThread("lock1")).isTrue();
        assertThat(heldByOtherThread.get()).isFalse();
    }

    @Test
    void shouldHandleMultipleDifferentLocks() {
        // when
        boolean lock1 = lockProvider.tryLock("lock1");
        boolean lock2 = lockProvider.tryLock("lock2");
        boolean lock3 = lockProvider.tryLock("lock3");

        // then
        assertThat(lock1).isTrue();
        assertThat(lock2).isTrue();
        assertThat(lock3).isTrue();
        assertThat(lockProvider.isLocked("lock1")).isTrue();
        assertThat(lockProvider.isLocked("lock2")).isTrue();
        assertThat(lockProvider.isLocked("lock3")).isTrue();
    }

    @Test
    void shouldReturnFalseForNonExistentLock() {
        // when & then
        assertThat(lockProvider.isLocked("nonexistent")).isFalse();
        assertThat(lockProvider.isHeldByCurrentThread("nonexistent")).isFalse();
    }
}
