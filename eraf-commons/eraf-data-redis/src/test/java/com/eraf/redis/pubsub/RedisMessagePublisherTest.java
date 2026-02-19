package com.eraf.redis.pubsub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Redis Pub/Sub Publisher 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RedisMessagePublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RedisMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RedisMessagePublisher(redisTemplate);
    }

    @Test
    @DisplayName("문자열 메시지 발행")
    void testPublishString() {
        // Given
        String channel = "test-channel";
        String message = "Hello Redis!";

        // When
        publisher.publish(channel, message);

        // Then
        verify(redisTemplate).convertAndSend(eq(channel), eq(message));
    }

    @Test
    @DisplayName("객체 메시지 발행 (JSON 직렬화)")
    void testPublishObject() {
        // Given
        String channel = "test-events";
        TestEvent event = new TestEvent("ORDER_CREATED", "12345");

        // When
        publisher.publishObject(channel, event);

        // Then
        verify(redisTemplate).convertAndSend(eq(channel), any(String.class));
    }

    @Test
    @DisplayName("발행 실패 시 예외 전파")
    void testPublishFailure() {
        // Given
        String channel = "test-channel";
        String message = "Hello";
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).convertAndSend(channel, message);

        // When & Then
        assertThatThrownBy(() -> publisher.publish(channel, message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to publish message");
    }

    static class TestEvent {
        private String type;
        private String orderId;

        public TestEvent() {
        }

        public TestEvent(String type, String orderId) {
            this.type = type;
            this.orderId = orderId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }
}
