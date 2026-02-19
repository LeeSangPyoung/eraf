package com.eraf.redis.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Redis Streams Producer 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RedisStreamProducerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    private RedisStreamProducer producer;

    private static final String TEST_STREAM = "test-stream";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForStream()).thenReturn(streamOps);
        producer = new RedisStreamProducer(redisTemplate);
    }

    @Test
    @DisplayName("메시지 추가 성공")
    void testAddMessage() {
        // Given
        Map<String, String> fields = Map.of("orderId", "12345", "status", "CREATED");
        RecordId recordId = RecordId.of("1234567890-0");
        when(streamOps.add(any())).thenReturn(recordId);

        // When
        String messageId = producer.add(TEST_STREAM, fields);

        // Then
        assertThat(messageId).isEqualTo("1234567890-0");
        verify(streamOps).add(any());
    }

    @Test
    @DisplayName("객체를 스트림에 추가")
    void testAddObject() {
        // Given
        RecordId recordId = RecordId.of("1234567890-0");
        when(streamOps.add(any())).thenReturn(recordId);

        // When
        String messageId = producer.addObject(TEST_STREAM, Map.of("key", "value"));

        // Then
        assertThat(messageId).isNotNull();
        verify(streamOps).add(any());
    }

    @Test
    @DisplayName("최대 길이 제한과 함께 메시지 추가")
    void testAddWithMaxLen() {
        // Given
        Map<String, String> fields = Map.of("key", "value");
        RecordId recordId = RecordId.of("1234567890-0");
        when(streamOps.add(any())).thenReturn(recordId);

        // When
        String messageId = producer.addWithMaxLen(TEST_STREAM, fields, 10);

        // Then
        assertThat(messageId).isEqualTo("1234567890-0");
        verify(streamOps).add(any());
        verify(streamOps).trim(eq(TEST_STREAM), eq(10L), eq(true));
    }

    @Test
    @DisplayName("스트림 길이 조회")
    void testGetStreamLength() {
        // Given
        when(streamOps.size(TEST_STREAM)).thenReturn(5L);

        // When
        Long length = producer.getStreamLength(TEST_STREAM);

        // Then
        assertThat(length).isEqualTo(5L);
    }

    @Test
    @DisplayName("스트림 삭제")
    void testDeleteStream() {
        // Given
        when(redisTemplate.delete(TEST_STREAM)).thenReturn(true);

        // When
        producer.deleteStream(TEST_STREAM);

        // Then
        verify(redisTemplate).delete(TEST_STREAM);
    }

    @Test
    @DisplayName("특정 메시지 삭제")
    void testDeleteMessages() {
        // Given
        when(streamOps.delete(eq(TEST_STREAM), any(RecordId[].class))).thenReturn(1L);

        // When
        Long deleted = producer.deleteMessages(TEST_STREAM, "1234567890-0");

        // Then
        assertThat(deleted).isEqualTo(1L);
    }
}
