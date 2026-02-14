package com.eraf.redis.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LockWatchdog 테스트
 */
class LockWatchdogTest {

    private LockWatchdog watchdog;
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(connectionFactory.getConnection()).thenReturn(connection);

        redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        watchdog = new LockWatchdog(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        watchdog.shutdown();
    }

    @Test
    @DisplayName("락 감시 등록")
    void testWatch() {
        // When
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(1);
        assertThat(watchdog.getWatchedKeys()).contains("eraf:lock:order:1:owner-1");
    }

    @Test
    @DisplayName("중복 감시 등록 방지")
    void testWatchDuplicate() {
        // When
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 소유자는 별도 감시")
    void testWatchDifferentOwners() {
        // When
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);
        watchdog.watch("eraf:lock:order:1", "owner-2", 30000L);

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("감시 해제")
    void testUnwatch() {
        // Given
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);
        assertThat(watchdog.getWatchedCount()).isEqualTo(1);

        // When
        watchdog.unwatch("eraf:lock:order:1", "owner-1");

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 감시 해제 - 에러 없음")
    void testUnwatchNonExistent() {
        // When & Then
        assertThatCode(() -> watchdog.unwatch("nonexistent", "owner"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다중 락 감시")
    void testMultipleWatches() {
        // When
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);
        watchdog.watch("eraf:lock:order:2", "owner-2", 60000L);
        watchdog.watch("eraf:lock:order:3", "owner-3", 10000L);

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Shutdown 시 모든 감시 해제")
    void testShutdown() {
        // Given
        watchdog.watch("eraf:lock:order:1", "owner-1", 30000L);
        watchdog.watch("eraf:lock:order:2", "owner-2", 60000L);

        // When
        watchdog.shutdown();

        // Then
        assertThat(watchdog.getWatchedCount()).isEqualTo(0);
    }
}
