package com.eraf.redis.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisReadWriteLock 테스트
 */
class RedisReadWriteLockTest {

    private RedisReadWriteLock rwLock;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        rwLock = new RedisReadWriteLock(redisTemplate);
    }

    @Test
    @DisplayName("읽기 락 획득 성공")
    void testReadLockAcquire() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        // When
        boolean acquired = rwLock.tryReadLock("resource:1", 0, 30, TimeUnit.SECONDS);

        // Then
        assertThat(acquired).isTrue();
    }

    @Test
    @DisplayName("읽기 락 획득 실패 (쓰기 락 존재)")
    void testReadLockFailWhenWriteLocked() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(0L);

        // When
        boolean acquired = rwLock.tryReadLock("resource:1", 0, 30, TimeUnit.SECONDS);

        // Then
        assertThat(acquired).isFalse();
    }

    @Test
    @DisplayName("쓰기 락 획득 성공")
    void testWriteLockAcquire() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(1L);

        // When
        boolean acquired = rwLock.tryWriteLock("resource:1", 0, 30, TimeUnit.SECONDS);

        // Then
        assertThat(acquired).isTrue();
    }

    @Test
    @DisplayName("쓰기 락 획득 실패 (읽기 락 존재)")
    void testWriteLockFailWhenReadLocked() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(0L);

        // When
        boolean acquired = rwLock.tryWriteLock("resource:1", 0, 30, TimeUnit.SECONDS);

        // Then
        assertThat(acquired).isFalse();
    }

    @Test
    @DisplayName("읽기 락 해제")
    void testReadUnlock() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                .thenReturn(1L);

        // When & Then
        assertThatCode(() -> rwLock.readUnlock("resource:1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("쓰기 락 해제")
    void testWriteUnlock() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        // When & Then
        assertThatCode(() -> rwLock.writeUnlock("resource:1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("락 정보 조회 - 없음")
    void testGetLockInfoNone() {
        // Given
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                .thenReturn("NONE");

        // When
        String info = rwLock.getLockInfo("resource:1");

        // Then
        assertThat(info).isEqualTo("NONE");
    }

    @Test
    @DisplayName("강제 락 해제")
    void testForceUnlock() {
        // Given
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // When
        boolean result = rwLock.forceUnlock("resource:1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("강제 락 해제 - 없는 키")
    void testForceUnlockNonExistent() {
        // Given
        when(redisTemplate.delete(anyString())).thenReturn(false);

        // When
        boolean result = rwLock.forceUnlock("nonexistent");

        // Then
        assertThat(result).isFalse();
    }
}
