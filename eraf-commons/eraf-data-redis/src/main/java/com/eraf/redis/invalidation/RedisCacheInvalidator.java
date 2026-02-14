package com.eraf.redis.invalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Redis Pub/Sub 기반 분산 캐시 무효화
 *
 * <p>멀티 인스턴스 환경에서 한 인스턴스가 캐시를 무효화하면
 * 다른 모든 인스턴스의 로컬 캐시도 함께 무효화됩니다.</p>
 *
 * <p>사용법:</p>
 * <pre>
 * // 단일 키 무효화 (모든 인스턴스에 전파)
 * cacheInvalidator.invalidate("users", "user:123");
 *
 * // 캐시 전체 무효화
 * cacheInvalidator.invalidateAll("users");
 * </pre>
 */
@Component
public class RedisCacheInvalidator implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheInvalidator.class);

    private static final String CHANNEL_PREFIX = "cache:invalidation:";
    private static final String INVALIDATE_ALL_SUFFIX = ":all";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisCacheInvalidator(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            CacheManager cacheManager) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.cacheManager = cacheManager;

        // 전체 무효화 채널 구독
        subscribeToAllChannels();
    }

    /**
     * 특정 키 무효화 (모든 인스턴스에 전파)
     *
     * @param cacheName 캐시 이름
     * @param key 무효화할 키
     */
    public void invalidate(String cacheName, Object key) {
        log.debug("[CacheInvalidator] Invalidating cache: {} key: {}", cacheName, key);

        // 로컬 캐시 무효화
        evictLocal(cacheName, key);

        // Pub/Sub로 다른 인스턴스에 전파
        publishInvalidation(cacheName, key);
    }

    /**
     * 캐시 전체 무효화 (모든 인스턴스에 전파)
     *
     * @param cacheName 캐시 이름
     */
    public void invalidateAll(String cacheName) {
        log.info("[CacheInvalidator] Invalidating all cache: {}", cacheName);

        // 로컬 캐시 무효화
        clearLocal(cacheName);

        // Pub/Sub로 다른 인스턴스에 전파
        publishInvalidationAll(cacheName);
    }

    /**
     * 패턴 기반 무효화 (와일드카드 지원)
     *
     * @param cacheName 캐시 이름
     * @param keyPattern 키 패턴 (예: "user:*", "*:active")
     */
    public void invalidatePattern(String cacheName, String keyPattern) {
        log.info("[CacheInvalidator] Invalidating cache: {} pattern: {}", cacheName, keyPattern);

        // Redis에서 패턴 매칭 키 검색
        String fullPattern = cacheName + "::" + keyPattern;
        var keys = redisTemplate.keys(fullPattern);

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String shortKey = key.substring((cacheName + "::").length());
                invalidate(cacheName, shortKey);
            }
            log.info("[CacheInvalidator] Invalidated {} keys matching pattern: {}", keys.size(), keyPattern);
        }
    }

    /**
     * Redis Pub/Sub 메시지 수신 (다른 인스턴스로부터)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            log.debug("[CacheInvalidator] Received invalidation message from channel: {}", channel);

            if (channel.endsWith(INVALIDATE_ALL_SUFFIX)) {
                // 전체 무효화 메시지
                String cacheName = extractCacheName(channel, INVALIDATE_ALL_SUFFIX);
                clearLocal(cacheName);
            } else {
                // 단일 키 무효화 메시지
                String cacheName = extractCacheName(channel, "");
                InvalidationMessage msg = objectMapper.readValue(body, InvalidationMessage.class);
                evictLocal(cacheName, msg.key);
            }

        } catch (Exception e) {
            log.error("[CacheInvalidator] Failed to process invalidation message", e);
        }
    }

    /**
     * 로컬 캐시에서 키 제거
     */
    private void evictLocal(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("[CacheInvalidator] Evicted local cache: {} key: {}", cacheName, key);
        }
    }

    /**
     * 로컬 캐시 전체 제거
     */
    private void clearLocal(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("[CacheInvalidator] Cleared local cache: {}", cacheName);
        }
    }

    /**
     * 무효화 메시지 발행 (단일 키)
     */
    private void publishInvalidation(String cacheName, Object key) {
        try {
            String channel = CHANNEL_PREFIX + cacheName;
            InvalidationMessage message = new InvalidationMessage(key, Instant.now());
            String json = objectMapper.writeValueAsString(message);

            redisTemplate.convertAndSend(channel, json);
            log.debug("[CacheInvalidator] Published invalidation: {} key: {}", cacheName, key);

        } catch (Exception e) {
            log.error("[CacheInvalidator] Failed to publish invalidation message", e);
        }
    }

    /**
     * 무효화 메시지 발행 (전체)
     */
    private void publishInvalidationAll(String cacheName) {
        String channel = CHANNEL_PREFIX + cacheName + INVALIDATE_ALL_SUFFIX;
        redisTemplate.convertAndSend(channel, "INVALIDATE_ALL");
        log.info("[CacheInvalidator] Published invalidation-all: {}", cacheName);
    }

    /**
     * 전체 무효화 채널 구독 (애플리케이션 시작 시)
     */
    private void subscribeToAllChannels() {
        // 패턴 구독: cache:invalidation:*
        String pattern = CHANNEL_PREFIX + "*";
        listenerContainer.addMessageListener(this, new ChannelTopic(pattern));
        log.info("[CacheInvalidator] Subscribed to cache invalidation channels: {}", pattern);
    }

    /**
     * 채널 이름에서 캐시 이름 추출
     */
    private String extractCacheName(String channel, String suffix) {
        String cacheName = channel.substring(CHANNEL_PREFIX.length());
        if (!suffix.isEmpty() && cacheName.endsWith(suffix)) {
            cacheName = cacheName.substring(0, cacheName.length() - suffix.length());
        }
        return cacheName;
    }

    /**
     * 무효화 메시지 DTO
     */
    private static class InvalidationMessage {
        public Object key;
        public Instant timestamp;

        public InvalidationMessage() {
        }

        public InvalidationMessage(Object key, Instant timestamp) {
            this.key = key;
            this.timestamp = timestamp;
        }
    }
}
