package com.eraf.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis Pub/Sub 메시지 구독자
 *
 * <p>Redis 채널을 구독하고 메시지를 처리합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 문자열 메시지 핸들러 등록
 * subscriber.subscribe("notifications", (channel, message) -> {
 *     System.out.println("Received: " + message);
 * });
 *
 * // 객체 메시지 핸들러 등록
 * subscriber.subscribeObject("events", Event.class, (channel, event) -> {
 *     System.out.println("Event: " + event.getType());
 * });
 *
 * // 패턴 구독
 * subscriber.subscribePattern("user:*", (channel, message) -> {
 *     System.out.println("User event from " + channel);
 * });
 * </pre>
 */
public class RedisMessageSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();

    public RedisMessageSubscriber(RedisMessageListenerContainer listenerContainer) {
        this.listenerContainer = listenerContainer;
        this.objectMapper = new ObjectMapper();
    }

    public RedisMessageSubscriber(RedisMessageListenerContainer listenerContainer, ObjectMapper objectMapper) {
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
    }

    /**
     * 채널 구독 (문자열 메시지)
     *
     * @param channel 채널 이름
     * @param handler 메시지 핸들러
     */
    public void subscribe(String channel, MessageHandler handler) {
        MessageListener listener = (message, pattern) -> {
            try {
                String body = new String(message.getBody());
                handler.handle(channel, body);
            } catch (Exception e) {
                log.error("[PubSub] Error handling message from channel '{}'", channel, e);
            }
        };

        listenerContainer.addMessageListener(listener, new ChannelTopic(channel));
        listeners.put(channel, listener);
        log.info("[PubSub] Subscribed to channel: {}", channel);
    }

    /**
     * 채널 구독 (객체 메시지, JSON 역직렬화)
     *
     * @param channel 채널 이름
     * @param type 객체 타입
     * @param handler 메시지 핸들러
     */
    public <T> void subscribeObject(String channel, Class<T> type, ObjectMessageHandler<T> handler) {
        MessageListener listener = (message, pattern) -> {
            try {
                String body = new String(message.getBody());
                T object = objectMapper.readValue(body, type);
                handler.handle(channel, object);
            } catch (Exception e) {
                log.error("[PubSub] Error deserializing message from channel '{}'", channel, e);
            }
        };

        listenerContainer.addMessageListener(listener, new ChannelTopic(channel));
        listeners.put(channel, listener);
        log.info("[PubSub] Subscribed to channel '{}' with type {}", channel, type.getSimpleName());
    }

    /**
     * 패턴 구독 (와일드카드 지원)
     *
     * @param pattern 패턴 (예: "user:*", "notification:*")
     * @param handler 메시지 핸들러
     */
    public void subscribePattern(String pattern, MessageHandler handler) {
        MessageListener listener = (message, patternBytes) -> {
            try {
                String channel = new String(message.getChannel());
                String body = new String(message.getBody());
                handler.handle(channel, body);
            } catch (Exception e) {
                log.error("[PubSub] Error handling message from pattern '{}'", pattern, e);
            }
        };

        listenerContainer.addMessageListener(listener, new PatternTopic(pattern));
        listeners.put(pattern, listener);
        log.info("[PubSub] Subscribed to pattern: {}", pattern);
    }

    /**
     * 패턴 구독 (객체 메시지)
     *
     * @param pattern 패턴
     * @param type 객체 타입
     * @param handler 메시지 핸들러
     */
    public <T> void subscribePatternObject(String pattern, Class<T> type, ObjectMessageHandler<T> handler) {
        MessageListener listener = (message, patternBytes) -> {
            try {
                String channel = new String(message.getChannel());
                String body = new String(message.getBody());
                T object = objectMapper.readValue(body, type);
                handler.handle(channel, object);
            } catch (Exception e) {
                log.error("[PubSub] Error deserializing message from pattern '{}'", pattern, e);
            }
        };

        listenerContainer.addMessageListener(listener, new PatternTopic(pattern));
        listeners.put(pattern, listener);
        log.info("[PubSub] Subscribed to pattern '{}' with type {}", pattern, type.getSimpleName());
    }

    /**
     * 구독 해제
     *
     * @param channelOrPattern 채널 또는 패턴 이름
     */
    public void unsubscribe(String channelOrPattern) {
        MessageListener listener = listeners.remove(channelOrPattern);
        if (listener != null) {
            Topic topic = channelOrPattern.contains("*") || channelOrPattern.contains("?")
                    ? new PatternTopic(channelOrPattern)
                    : new ChannelTopic(channelOrPattern);
            listenerContainer.removeMessageListener(listener, topic);
            log.info("[PubSub] Unsubscribed from: {}", channelOrPattern);
        }
    }

    /**
     * 모든 구독 해제
     */
    public void unsubscribeAll() {
        listeners.keySet().forEach(this::unsubscribe);
        log.info("[PubSub] Unsubscribed from all channels");
    }

    /**
     * 문자열 메시지 핸들러 인터페이스
     */
    @FunctionalInterface
    public interface MessageHandler {
        void handle(String channel, String message);
    }

    /**
     * 객체 메시지 핸들러 인터페이스
     */
    @FunctionalInterface
    public interface ObjectMessageHandler<T> {
        void handle(String channel, T object);
    }
}
