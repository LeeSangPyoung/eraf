package com.eraf.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisLockProviderTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisLockProvider lockProvider;

    @BeforeEach
    void setUp() {
        lockProvider = new RedisLockProvider(redisTemplate);
    }

    @Test
    @DisplayName("락 획득 성공")
    @SuppressWarnings("unchecked")
    void testAcquireLockSuccess() {
        // Given - waitTime > 0이어야 while 루프 진입
        // execute(script, keys, owner, leaseMillis) - 4 args (2 varargs)
        String lockKey = "test-lock";
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), anyString(), anyString());

        // When
        boolean acquired = lockProvider.tryLock(lockKey, 100, 30000, TimeUnit.MILLISECONDS);

        // Then
        assertTrue(acquired);
    }

    @Test
    @DisplayName("락 획득 실패 - 타임아웃")
    @SuppressWarnings("unchecked")
    void testAcquireLockFailed() {
        // Given - waitTime > 0이지만 항상 0 반환 → 타임아웃
        String lockKey = "test-lock";
        doReturn(0L).when(redisTemplate).execute(any(RedisScript.class), anyList(), anyString(), anyString());

        // When
        boolean acquired = lockProvider.tryLock(lockKey, 50, 30000, TimeUnit.MILLISECONDS);

        // Then
        assertFalse(acquired);
    }

    @Test
    @DisplayName("락 해제")
    @SuppressWarnings("unchecked")
    void testReleaseLock() {
        // Given - 먼저 락을 획득하여 threadLockOwners에 등록
        // acquire: execute(script, keys, owner, leaseMillis) - 4 args
        String lockKey = "test-lock";
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), anyString(), anyString());

        lockProvider.tryLock(lockKey, 100, 30000, TimeUnit.MILLISECONDS);

        // release: execute(script, keys, owner) - 3 args (1 vararg)
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());

        // When
        lockProvider.unlock(lockKey);

        // Then - acquire (4 args) 호출 확인
        verify(redisTemplate, atLeastOnce()).execute(any(RedisScript.class), anyList(), anyString(), anyString());
        // release (3 args) 호출 확인
        verify(redisTemplate, atLeastOnce()).execute(any(RedisScript.class), anyList(), anyString());
    }

    @Test
    @DisplayName("락 보유 여부 확인")
    void testIsLocked() {
        // Given
        String lockKey = "test-lock";
        when(redisTemplate.hasKey(contains(lockKey))).thenReturn(true);

        // When
        boolean locked = lockProvider.isLocked(lockKey);

        // Then
        assertTrue(locked);
    }

    @Test
    @DisplayName("waitTime=0이면 while 루프 미진입으로 false 반환")
    void testTryLockZeroWaitReturnsImmediately() {
        // Given - waitTime=0이면 while 루프 조건이 false
        String lockKey = "test-lock";

        // When - tryLock(key) delegates to tryLock(key, 0, 30, SECONDS)
        boolean acquired = lockProvider.tryLock(lockKey);

        // Then - while 루프가 실행되지 않으므로 false 반환
        assertFalse(acquired);
    }
}
