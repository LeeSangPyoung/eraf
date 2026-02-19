package com.eraf.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis Pub/Sub 메시지 발행자
 *
 * <p>Redis 채널에 메시지를 발행합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 문자열 메시지 발행
 * publisher.publish("notifications", "User logged in");
 *
 * // 객체 메시지 발행
 * Event event = new Event("ORDER_CREATED", orderId);
 * publisher.publishObject("events", event);
 * </pre>
 */
public class RedisMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisMessagePublisher.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 문자열 메시지 발행
     *
     * @param channel 채널 이름
     * @param message 메시지
     */
    public void publish(String channel, String message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("[PubSub] Published message to channel '{}': {}", channel, message);
        } catch (Exception e) {
            log.error("[PubSub] Failed to publish message to channel '{}'", channel, e);
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    /**
     * 객체 메시지 발행 (JSON 직렬화)
     *
     * @param channel 채널 이름
     * @param object 발행할 객체
     */
    public void publishObject(String channel, Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            redisTemplate.convertAndSend(channel, json);
            log.debug("[PubSub] Published object to channel '{}': {}", channel, object.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[PubSub] Failed to publish object to channel '{}'", channel, e);
            throw new RuntimeException("Failed to publish object", e);
        }
    }

    /**
     * 패턴 기반 메시지 발행 (여러 채널에 동시 발행)
     *
     * @param channelPattern 채널 패턴 (예: "notifications:*")
     * @param message 메시지
     */
    public void publishToPattern(String channelPattern, String message) {
        try {
            // Redis는 패턴 발행을 직접 지원하지 않으므로,
            // 패턴 매칭 채널을 검색하여 각각 발행
            var keys = redisTemplate.keys(channelPattern);
            if (keys != null && !keys.isEmpty()) {
                for (String channel : keys) {
                    publish(channel, message);
                }
                log.info("[PubSub] Published to {} channels matching pattern '{}'", keys.size(), channelPattern);
            } else {
                log.warn("[PubSub] No channels found matching pattern '{}'", channelPattern);
            }
        } catch (Exception e) {
            log.error("[PubSub] Failed to publish to pattern '{}'", channelPattern, e);
            throw new RuntimeException("Failed to publish to pattern", e);
        }
    }
}
