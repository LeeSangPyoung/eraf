package com.eraf.core.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLockProviderTest {

    private InMemoryLockProvider lockProvider;

    @BeforeEach
    void setUp() {
        lockProvider = new InMemoryLockProvider();
    }

    @Test
    @DisplayName("락 획득 성공")
    void testTryLock() {
        // When
        boolean acquired = lockProvider.tryLock("test-lock");

        // Then
        assertTrue(acquired);
        assertTrue(lockProvider.isLocked("test-lock"));
        assertTrue(lockProvider.isHeldByCurrentThread("test-lock"));
    }

    @Test
    @DisplayName("락 해제")
    void testUnlock() {
        // Given
        lockProvider.tryLock("test-lock");

        // When
        lockProvider.unlock("test-lock");

        // Then
        assertFalse(lockProvider.isLocked("test-lock"));
    }

    @Test
    @DisplayName("타임아웃 설정 락 획득")
    void testTryLockWithTimeout() {
        // When
        boolean acquired = lockProvider.tryLock("test-lock", 100, 5000, TimeUnit.MILLISECONDS);

        // Then
        assertTrue(acquired);
        assertTrue(lockProvider.isLocked("test-lock"));

        // Cleanup
        lockProvider.unlock("test-lock");
    }

    @Test
    @DisplayName("이미 락이 걸린 경우 즉시 실패")
    void testTryLockFails() throws InterruptedException {
        // Given
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch testComplete = new CountDownLatch(1);
        AtomicBoolean secondLockResult = new AtomicBoolean(true);

        // 첫 번째 스레드에서 락 획득
        Thread thread1 = new Thread(() -> {
            lockProvider.tryLock("test-lock");
            lockAcquired.countDown();
            try {
                testComplete.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            lockProvider.unlock("test-lock");
        });
        thread1.start();
        lockAcquired.await();

        // When - 메인 스레드에서 락 시도
        boolean acquired = lockProvider.tryLock("test-lock");

        // Then
        assertFalse(acquired);

        // Cleanup
        testComplete.countDown();
        thread1.join();
    }

    @Test
    @DisplayName("타임아웃 후 락 획득 실패")
    void testTryLockWithTimeoutFails() throws InterruptedException {
        // Given
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch testComplete = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            lockProvider.tryLock("test-lock");
            lockAcquired.countDown();
            try {
                testComplete.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            lockProvider.unlock("test-lock");
        });
        thread1.start();
        lockAcquired.await();

        // When
        boolean acquired = lockProvider.tryLock("test-lock", 50, 5000, TimeUnit.MILLISECONDS);

        // Then
        assertFalse(acquired);

        // Cleanup
        testComplete.countDown();
        thread1.join();
    }

    @Test
    @DisplayName("다른 스레드의 락 확인")
    void testIsHeldByCurrentThreadFalse() throws InterruptedException {
        // Given
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch testComplete = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            lockProvider.tryLock("test-lock");
            lockAcquired.countDown();
            try {
                testComplete.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            lockProvider.unlock("test-lock");
        });
        thread1.start();
        lockAcquired.await();

        // When
        boolean heldByCurrentThread = lockProvider.isHeldByCurrentThread("test-lock");

        // Then
        assertTrue(lockProvider.isLocked("test-lock"));
        assertFalse(heldByCurrentThread);

        // Cleanup
        testComplete.countDown();
        thread1.join();
    }

    @Test
    @DisplayName("존재하지 않는 락 상태 확인")
    void testNonExistentLock() {
        // Then
        assertFalse(lockProvider.isLocked("nonexistent"));
        assertFalse(lockProvider.isHeldByCurrentThread("nonexistent"));
    }

    @Test
    @DisplayName("다른 스레드의 락은 해제 불가")
    void testUnlockFromDifferentThread() throws InterruptedException {
        // Given
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch unlockAttempted = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            lockProvider.tryLock("test-lock");
            lockAcquired.countDown();
            try {
                unlockAttempted.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 락은 여전히 유지되어야 함
            assertTrue(lockProvider.isLocked("test-lock"));
            lockProvider.unlock("test-lock");
        });
        thread1.start();
        lockAcquired.await();

        // When - 메인 스레드에서 해제 시도
        lockProvider.unlock("test-lock");

        // Then - 락은 여전히 유지됨
        assertTrue(lockProvider.isLocked("test-lock"));

        // Cleanup
        unlockAttempted.countDown();
        thread1.join();
    }

    @Test
    @DisplayName("동시성 테스트 - 상호 배제")
    void testMutualExclusion() throws InterruptedException {
        // Given
        AtomicInteger counter = new AtomicInteger(0);
        int threadCount = 10;
        int iterationsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                startLatch.await();
                for (int i = 0; i < iterationsPerThread; i++) {
                    while (!lockProvider.tryLock("counter-lock")) {
                        Thread.yield();
                    }
                    try {
                        // 임계 영역
                        int current = counter.get();
                        Thread.yield(); // 경쟁 상황 유도
                        counter.set(current + 1);
                    } finally {
                        lockProvider.unlock("counter-lock");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                endLatch.countDown();
            }
        };

        // When
        for (int i = 0; i < threadCount; i++) {
            new Thread(task).start();
        }
        startLatch.countDown();
        endLatch.await();

        // Then - 락이 제대로 작동했다면 카운터 값이 정확해야 함
        assertEquals(threadCount * iterationsPerThread, counter.get());
    }

    @Test
    @DisplayName("재진입 가능 (Reentrant)")
    void testReentrantLock() {
        // Given
        lockProvider.tryLock("test-lock");

        // When - 같은 스레드에서 다시 락 획득 시도
        boolean reacquired = lockProvider.tryLock("test-lock");

        // Then - ReentrantLock이므로 재획득 가능
        assertTrue(reacquired);

        // 두 번 해제 필요
        lockProvider.unlock("test-lock");
        assertTrue(lockProvider.isLocked("test-lock")); // 아직 한 번 더 잠겨있음
        lockProvider.unlock("test-lock");
        assertFalse(lockProvider.isLocked("test-lock"));
    }
}
