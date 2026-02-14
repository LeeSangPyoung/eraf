package com.eraf.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErafKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ErafKafkaProducer producer;
    private ErafKafkaProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        properties = new ErafKafkaProperties();
        properties.setTopicPrefix("test-");
        producer = new ErafKafkaProducer(kafkaTemplate, objectMapper, properties);
    }

    @Test
    @DisplayName("비동기 이벤트 발행")
    void testSendAsync() {
        // Given
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("TEST_EVENT", "payload");
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        CompletableFuture<SendResult<String, String>> future = producer.sendAsync("topic", event);

        // Then
        assertNotNull(future);
        assertTrue(future.isDone());
        verify(kafkaTemplate).send(eq("test-topic"), isNull(), anyString());
    }

    @Test
    @DisplayName("키와 함께 이벤트 발행")
    void testSendAsyncWithKey() {
        // Given
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("TEST_EVENT", "payload");
        String key = "event-key";
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), eq(key), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        CompletableFuture<SendResult<String, String>> future = producer.sendAsync("topic", key, event);

        // Then
        assertNotNull(future);
        verify(kafkaTemplate).send(eq("test-topic"), eq(key), anyString());
    }

    @Test
    @DisplayName("토픽 접두사 적용")
    void testTopicPrefix() {
        // Given
        properties.setTopicPrefix("myapp-");
        producer = new ErafKafkaProducer(kafkaTemplate, objectMapper, properties);
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("TEST_EVENT", "payload");

        RecordMetadata metadata = new RecordMetadata(new TopicPartition("myapp-orders", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        producer.sendAsync("orders", event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), any(), anyString());
        assertEquals("myapp-orders", topicCaptor.getValue());
    }

    @Test
    @DisplayName("빈 토픽 접두사")
    void testEmptyTopicPrefix() {
        // Given
        properties.setTopicPrefix("");
        producer = new ErafKafkaProducer(kafkaTemplate, objectMapper, properties);
        ErafKafkaEvent<String> event = ErafKafkaEvent.of("TEST_EVENT", "payload");

        RecordMetadata metadata = new RecordMetadata(new TopicPartition("orders", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        producer.sendAsync("orders", event);

        // Then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), any(), anyString());
        assertEquals("orders", topicCaptor.getValue());
    }

    @Test
    @DisplayName("간단한 메시지 발행")
    void testSendSimple() {
        // Given
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), any(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        producer.send("topic", "ORDER_CREATED", new OrderData("ORD-123", 10000));

        // Then
        verify(kafkaTemplate).send(eq("test-topic"), isNull(), anyString());
    }

    @Test
    @DisplayName("키와 함께 간단한 메시지 발행")
    void testSendSimpleWithKey() {
        // Given
        String key = "order-key";
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("test-topic", 0), 100, 0, 0, 0, 0);
        SendResult<String, String> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(anyString(), eq(key), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        producer.send("topic", key, "ORDER_CREATED", new OrderData("ORD-456", 20000));

        // Then
        verify(kafkaTemplate).send(eq("test-topic"), eq(key), anyString());
    }

    @Test
    @DisplayName("직렬화 실패 시 예외 처리")
    void testSerializationFailure() {
        // Given
        ErafKafkaEvent<Object> event = ErafKafkaEvent.of("TEST", new UnserializableObject());

        // When
        CompletableFuture<SendResult<String, String>> future = producer.sendAsync("topic", event);

        // Then
        assertTrue(future.isCompletedExceptionally());
    }

    // Test classes
    record OrderData(String orderId, int amount) {}

    // Unserializable object to test failure
    static class UnserializableObject {
        private final Object selfReference = this;
    }
}
