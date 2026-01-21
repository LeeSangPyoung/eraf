package com.eraf.starter.statemachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 상태 저장소 (분산 환경용)
 * Redis가 있는 경우 자동으로 사용됨
 */
public class RedisStateStore implements StateStore {

    private static final Logger log = LoggerFactory.getLogger(RedisStateStore.class);
    private static final String KEY_PREFIX = "eraf:statemachine:state:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisStateStore(RedisTemplate<String, String> redisTemplate) {
        this(redisTemplate, Duration.ofDays(7)); // 기본 7일 TTL
    }

    public RedisStateStore(RedisTemplate<String, String> redisTemplate, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(String machineId, String entityId, StateInfo stateInfo) {
        String key = createRedisKey(machineId, entityId);
        try {
            String json = objectMapper.writeValueAsString(stateInfo);
            if (ttl != null && !ttl.isZero()) {
                redisTemplate.opsForValue().set(key, json, ttl);
            } else {
                redisTemplate.opsForValue().set(key, json);
            }
            log.debug("State saved to Redis: key={}", key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize StateInfo: {}", e.getMessage());
            throw new StateMachineException("Failed to save state to Redis", e);
        }
    }

    @Override
    public Optional<StateInfo> find(String machineId, String entityId) {
        String key = createRedisKey(machineId, entityId);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return Optional.empty();
            }
            StateInfo stateInfo = objectMapper.readValue(json, StateInfo.class);
            log.debug("State found in Redis: key={}", key);
            return Optional.of(stateInfo);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize StateInfo: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void remove(String machineId, String entityId) {
        String key = createRedisKey(machineId, entityId);
        redisTemplate.delete(key);
        log.debug("State removed from Redis: key={}", key);
    }

    @Override
    public boolean exists(String machineId, String entityId) {
        String key = createRedisKey(machineId, entityId);
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * TTL 갱신
     */
    public void refreshTtl(String machineId, String entityId) {
        String key = createRedisKey(machineId, entityId);
        if (ttl != null && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
        }
    }

    private String createRedisKey(String machineId, String entityId) {
        return KEY_PREFIX + createKey(machineId, entityId);
    }
}
