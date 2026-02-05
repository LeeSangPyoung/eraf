package com.eraf.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisLockProviderTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisLockProvider lockProvider;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lockProvider = new RedisLockProvider(redisTemplate, "lock:");
    }

    @Test
    @DisplayName("락 획득 성공")
    void testAcquireLockSuccess() {
        // Given
        String lockKey = "test-lock";
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        // When
        boolean acquired = lockProvider.tryLock(lockKey, Duration.ofSeconds(30));

        // Then
        assertTrue(acquired);
    }

    @Test
    @DisplayName("락 획득 실패")
    void testAcquireLockFailed() {
        // Given
        String lockKey = "test-lock";
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        // When
        boolean acquired = lockProvider.tryLock(lockKey, Duration.ofSeconds(30));

        // Then
        assertFalse(acquired);
    }

    @Test
    @DisplayName("락 해제")
    void testReleaseLock() {
        // Given
        String lockKey = "test-lock";
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // When
        lockProvider.unlock(lockKey);

        // Then
        verify(redisTemplate, times(1)).delete(contains(lockKey));
    }
}
