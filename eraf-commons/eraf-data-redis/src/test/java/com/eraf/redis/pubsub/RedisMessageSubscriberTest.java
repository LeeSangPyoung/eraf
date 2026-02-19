package com.eraf.redis.pubsub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Redis Pub/Sub Subscriber 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RedisMessageSubscriberTest {

    @Mock
    private RedisMessageListenerContainer listenerContainer;

    private RedisMessageSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new RedisMessageSubscriber(listenerContainer);
    }

    @Test
    @DisplayName("채널 구독")
    void testSubscribe() {
        // Given
        String channel = "test-subscribe";

        // When
        subscriber.subscribe(channel, (ch, msg) -> {});

        // Then
        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(listenerContainer).addMessageListener(any(MessageListener.class), topicCaptor.capture());
        assertThat(topicCaptor.getValue()).isInstanceOf(ChannelTopic.class);
        assertThat(topicCaptor.getValue().getTopic()).isEqualTo(channel);
    }

    @Test
    @DisplayName("패턴 구독")
    void testSubscribePattern() {
        // Given
        String pattern = "user:*";

        // When
        subscriber.subscribePattern(pattern, (ch, msg) -> {});

        // Then
        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(listenerContainer).addMessageListener(any(MessageListener.class), topicCaptor.capture());
        assertThat(topicCaptor.getValue()).isInstanceOf(PatternTopic.class);
        assertThat(topicCaptor.getValue().getTopic()).isEqualTo(pattern);
    }

    @Test
    @DisplayName("구독 해제")
    void testUnsubscribe() {
        // Given
        String channel = "test-unsubscribe";
        subscriber.subscribe(channel, (ch, msg) -> {});

        // When
        subscriber.unsubscribe(channel);

        // Then
        verify(listenerContainer).removeMessageListener(any(MessageListener.class), any(Topic.class));
    }
}
