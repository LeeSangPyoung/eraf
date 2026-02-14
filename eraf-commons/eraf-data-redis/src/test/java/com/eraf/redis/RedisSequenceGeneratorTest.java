package com.eraf.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSequenceGeneratorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisSequenceGenerator sequenceGenerator;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        sequenceGenerator = new RedisSequenceGenerator(redisTemplate);
    }

    @Test
    @DisplayName("시퀀스 생성")
    void testNextValue() {
        // Given
        String sequenceName = "order";
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        long value = sequenceGenerator.nextValue(sequenceName);

        // Then
        assertEquals(1L, value);
    }

    @Test
    @DisplayName("연속 시퀀스 생성")
    void testConsecutiveValues() {
        // Given
        String sequenceName = "order";
        when(valueOperations.increment(anyString()))
                .thenReturn(1L)
                .thenReturn(2L)
                .thenReturn(3L);

        // When
        long first = sequenceGenerator.nextValue(sequenceName);
        long second = sequenceGenerator.nextValue(sequenceName);
        long third = sequenceGenerator.nextValue(sequenceName);

        // Then
        assertEquals(1L, first);
        assertEquals(2L, second);
        assertEquals(3L, third);
    }

    @Test
    @DisplayName("포맷된 시퀀스 생성")
    void testNextFormatted() {
        // Given
        String sequenceName = "order";
        when(valueOperations.increment(anyString())).thenReturn(42L);

        // When
        String formatted = sequenceGenerator.nextFormatted(sequenceName, 5);

        // Then
        assertEquals("00042", formatted);
    }
}
