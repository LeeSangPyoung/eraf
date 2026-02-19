package com.eraf.redis.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Redis Streams Consumer 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RedisStreamConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    private RedisStreamConsumer consumer;

    private static final String TEST_STREAM = "test-consumer-stream";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForStream()).thenReturn(streamOps);
        consumer = new RedisStreamConsumer(redisTemplate);
    }

    @Test
    @DisplayName("스트림에서 메시지 읽기")
    void testReadMessages() {
        // Given
        MapRecord<String, Object, Object> record = StreamRecords.newRecord()
                .in(TEST_STREAM)
                .ofMap(Map.of("key", "value1"));
        when(streamOps.read(any(StreamOffset[].class)))
                .thenReturn(List.of(record));

        // When
        List<MapRecord<String, Object, Object>> messages = consumer.read(TEST_STREAM, "0");

        // Then
        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(1);
    }

    @Test
    @DisplayName("컨슈머 그룹 생성")
    void testCreateConsumerGroup() {
        // Given
        when(streamOps.createGroup(TEST_STREAM, "test-group")).thenReturn("OK");

        // When
        consumer.createGroup(TEST_STREAM, "test-group");

        // Then
        verify(streamOps).createGroup(TEST_STREAM, "test-group");
    }

    @Test
    @DisplayName("컨슈머 그룹으로 메시지 읽기")
    void testReadFromConsumerGroup() {
        // Given
        when(streamOps.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset[].class)))
                .thenReturn(Collections.emptyList());

        // When
        List<MapRecord<String, Object, Object>> messages =
                consumer.readGroup(TEST_STREAM, "test-group", "consumer-1");

        // Then
        assertThat(messages).isNotNull();
    }

    @Test
    @DisplayName("메시지 확인 (ACK)")
    void testAcknowledge() {
        // Given
        when(streamOps.acknowledge(eq(TEST_STREAM), eq("test-group"), any(RecordId[].class)))
                .thenReturn(1L);

        // When
        Long acked = consumer.acknowledge(TEST_STREAM, "test-group", "1234567890-0");

        // Then
        assertThat(acked).isEqualTo(1L);
    }

    @Test
    @DisplayName("컨슈머 그룹 삭제")
    void testDestroyGroup() {
        // When
        consumer.destroyGroup(TEST_STREAM, "test-group");

        // Then
        verify(streamOps).destroyGroup(TEST_STREAM, "test-group");
    }
}
