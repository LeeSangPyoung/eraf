package com.eraf.kafka.dlq;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        objectMapper.findAndRegisterModules();
        publisher = new DeadLetterQueuePublisher(kafkaTemplate, objectMapper, ".dlq");
    }

    @Test
    @DisplayName("DeadLetterMessage를 기본 DLQ 토픽으로 발행")
    void shouldPublishMessageToDefaultDlqTopic() {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("orders")
                .originalKey("order-123")
                .originalMessage("{\"orderId\":\"123\"}")
                .error(new RuntimeException("Processing failed"))
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(message);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eq("order-123"), anyString());
        assertEquals("orders.dlq", topicCaptor.getValue());
    }

    @Test
    @DisplayName("DLQ 메시지에 원본 정보 포함")
    void shouldIncludeOriginalInfoInDlqMessage() throws Exception {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("users")
                .originalPartition(2)
                .originalOffset(500L)
                .originalKey("user-456")
                .originalMessage("{\"userId\":\"456\"}")
                .error(new IllegalArgumentException("Invalid user data"))
                .build();

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(message);

        // Then
        String json = valueCaptor.getValue();
        DeadLetterMessage deserialized = objectMapper.readValue(json, DeadLetterMessage.class);

        assertEquals("users", deserialized.getOriginalTopic());
        assertEquals(2, deserialized.getOriginalPartition());
        assertEquals(500L, deserialized.getOriginalOffset());
        assertEquals("user-456", deserialized.getOriginalKey());
        assertEquals("{\"userId\":\"456\"}", deserialized.getOriginalMessage());
        assertEquals("Invalid user data", deserialized.getErrorMessage());
        assertEquals("java.lang.IllegalArgumentException", deserialized.getErrorClass());
    }

    @Test
    @DisplayName("커스텀 DLQ 토픽으로 발행")
    void shouldPublishToCustomDlqTopic() {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("events")
                .originalKey("key")
                .originalMessage("value")
                .error(new Exception("error"))
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish("events-dead-letter", message);

        // Then
        verify(kafkaTemplate).send(eq("events-dead-letter"), eq("key"), anyString());
    }

    @Test
    @DisplayName("커스텀 DLQ 접미사 설정")
    void shouldUseCustomDlqSuffix() {
        // Given
        publisher = new DeadLetterQueuePublisher(kafkaTemplate, objectMapper, "-dead-letter");
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("events")
                .originalKey("key")
                .originalMessage("value")
                .error(new Exception("error"))
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(message);

        // Then
        verify(kafkaTemplate).send(eq("events-dead-letter"), anyString(), anyString());
    }

    @Test
    @DisplayName("null 키 처리")
    void shouldHandleNullKey() {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("topic")
                .originalMessage("value")
                .error(new Exception("error"))
                .build();

        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(message);

        // Then
        verify(kafkaTemplate).send(eq("topic.dlq"), isNull(), anyString());
    }

    @Test
    @DisplayName("reprocess로 원본 토픽 재발행")
    void shouldReprocessToOriginalTopic() {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("orders")
                .originalKey("order-123")
                .originalMessage("{\"orderId\":\"123\"}")
                .error(new Exception("error"))
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.reprocess(message);

        // Then
        verify(kafkaTemplate).send("orders", "order-123", "{\"orderId\":\"123\"}");
    }

    @Test
    @DisplayName("재시도 횟수 포함")
    void shouldIncludeRetryCount() throws Exception {
        // Given
        DeadLetterMessage message = DeadLetterMessage.builder()
                .originalTopic("topic")
                .originalKey("key")
                .originalMessage("value")
                .error(new Exception("error"))
                .retryCount(3)
                .build();

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), valueCaptor.capture()))
                .thenReturn(new CompletableFuture<>());

        // When
        publisher.publish(message);

        // Then
        DeadLetterMessage deserialized = objectMapper.readValue(valueCaptor.getValue(), DeadLetterMessage.class);
        assertEquals(3, deserialized.getRetryCount());
    }
}
