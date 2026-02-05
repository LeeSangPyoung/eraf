package com.eraf.kafka.dlq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeadLetterQueuePublisher 테스트
 */
@ExtendWith(MockitoExtension.class)
class DeadLetterQueuePublisherTest {

    private DeadLetterQueuePublisher publisher;
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        publisher = new DeadLetterQueuePublisher(kafkaTemplate, objectMapper, ".dlq");
    }

    @Test
    @DisplayName("실패한 레코드를 DLQ로 발행")
    void shouldPublishFailedRecordToDlq() {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "orders", 0, 100L, "order-123", "{\"orderId\":\"123\"}");
        Exception exception = new RuntimeException("Processing failed");

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, exception);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertEquals("orders.dlq", topicCaptor.getValue());
        assertEquals("order-123", keyCaptor.getValue());
        assertTrue(valueCaptor.getValue().contains("Processing failed"));
    }

    @Test
    @DisplayName("DLQ 메시지에 원본 정보 포함")
    void shouldIncludeOriginalInfoInDlqMessage() throws Exception {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "users", 2, 500L, "user-456", "{\"userId\":\"456\"}");
        Exception exception = new IllegalArgumentException("Invalid user data");

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, exception);

        // Then
        String json = valueCaptor.getValue();
        DeadLetterMessage message = objectMapper.readValue(json, DeadLetterMessage.class);

        assertEquals("users", message.getOriginalTopic());
        assertEquals(2, message.getOriginalPartition());
        assertEquals(500L, message.getOriginalOffset());
        assertEquals("user-456", message.getKey());
        assertEquals("{\"userId\":\"456\"}", message.getPayload());
        assertEquals("Invalid user data", message.getErrorMessage());
        assertEquals("java.lang.IllegalArgumentException", message.getErrorClass());
    }

    @Test
    @DisplayName("커스텀 DLQ 토픽 접미사")
    void shouldUseCustomDlqSuffix() {
        // Given
        publisher = new DeadLetterQueuePublisher(kafkaTemplate, objectMapper, "-dead-letter");

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "events", 0, 0L, "key", "value");

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, new Exception("error"));

        // Then
        verify(kafkaTemplate).send(eq("events-dead-letter"), anyString(), anyString());
    }

    @Test
    @DisplayName("null 키 처리")
    void shouldHandleNullKey() {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, null, "value");

        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, new Exception("error"));

        // Then
        verify(kafkaTemplate).send(eq("topic.dlq"), isNull(), anyString());
    }

    @Test
    @DisplayName("null 값 처리")
    void shouldHandleNullValue() throws Exception {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "key", null);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, new Exception("error"));

        // Then
        DeadLetterMessage message = objectMapper.readValue(valueCaptor.getValue(), DeadLetterMessage.class);
        assertNull(message.getPayload());
    }

    @Test
    @DisplayName("타임스탬프 포함")
    void shouldIncludeTimestamp() throws Exception {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "key", "value");

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, new Exception("error"));

        // Then
        DeadLetterMessage message = objectMapper.readValue(valueCaptor.getValue(), DeadLetterMessage.class);
        assertNotNull(message.getTimestamp());
    }

    @Test
    @DisplayName("재시도 횟수와 함께 발행")
    void shouldPublishWithRetryCount() throws Exception {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "key", "value");

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(record, new Exception("error"), 3);

        // Then
        DeadLetterMessage message = objectMapper.readValue(valueCaptor.getValue(), DeadLetterMessage.class);
        assertEquals(3, message.getRetryCount());
    }

    @Test
    @DisplayName("발행 실패해도 예외 전파 안함")
    void shouldNotPropagatePublishException() {
        // Given
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "topic", 0, 0L, "key", "value");

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka error"));

        // When & Then - 예외가 전파되지 않아야 함
        assertDoesNotThrow(() -> publisher.publish(record, new Exception("error")));
    }
}
