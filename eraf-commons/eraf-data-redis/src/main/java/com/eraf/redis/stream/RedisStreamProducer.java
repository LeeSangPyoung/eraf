package com.eraf.redis.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Streams 메시지 생산자
 *
 * <p>Redis Streams에 메시지를 추가합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 단순 메시지 추가
 * String messageId = producer.add("orders", Map.of("orderId", "12345", "status", "CREATED"));
 *
 * // 객체를 스트림에 추가
 * Order order = new Order("12345", "CREATED");
 * String messageId = producer.addObject("orders", order);
 *
 * // 최대 길이 제한 (MAXLEN)
 * producer.addWithMaxLen("events", event, 10000);
 * </pre>
 */
public class RedisStreamProducer {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamProducer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamProducer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public RedisStreamProducer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 스트림에 메시지 추가
     *
     * @param streamKey 스트림 키
     * @param fields 메시지 필드 맵
     * @return 생성된 메시지 ID
     */
    public String add(String streamKey, Map<String, String> fields) {
        try {
            var record = StreamRecords.string(fields).withStreamKey(streamKey);
            RecordId recordId = redisTemplate.opsForStream().add(record);
            log.debug("[Stream] Added message to stream '{}': {}", streamKey, recordId);
            return recordId.getValue();
        } catch (Exception e) {
            log.error("[Stream] Failed to add message to stream '{}'", streamKey, e);
            throw new RuntimeException("Failed to add message to stream", e);
        }
    }

    /**
     * 객체를 JSON으로 직렬화하여 스트림에 추가
     *
     * @param streamKey 스트림 키
     * @param object 추가할 객체
     * @return 생성된 메시지 ID
     */
    public String addObject(String streamKey, Object object) {
        try {
            Map<String, String> fields = convertToMap(object);
            return add(streamKey, fields);
        } catch (Exception e) {
            log.error("[Stream] Failed to add object to stream '{}'", streamKey, e);
            throw new RuntimeException("Failed to add object to stream", e);
        }
    }

    /**
     * 최대 길이 제한과 함께 메시지 추가 (MAXLEN)
     *
     * @param streamKey 스트림 키
     * @param fields 메시지 필드
     * @param maxLen 최대 메시지 개수 (approximate)
     * @return 생성된 메시지 ID
     */
    public String addWithMaxLen(String streamKey, Map<String, String> fields, long maxLen) {
        try {
            // Redis Streams MAXLEN을 사용하여 메시지 추가
            // Spring Data Redis는 XADD 명령의 MAXLEN 옵션을 직접 지원하지 않으므로
            // 일반 add를 사용하고, 수동으로 trim 수행
            var record = StreamRecords.string(fields).withStreamKey(streamKey);
            RecordId recordId = redisTemplate.opsForStream().add(record);

            // 스트림 길이 제한 (approximate trim)
            redisTemplate.opsForStream().trim(streamKey, maxLen, true);

            log.debug("[Stream] Added message to stream '{}' with maxlen {}: {}", streamKey, maxLen, recordId);
            return recordId != null ? recordId.getValue() : "";
        } catch (Exception e) {
            log.error("[Stream] Failed to add message with maxlen to stream '{}'", streamKey, e);
            throw new RuntimeException("Failed to add message to stream", e);
        }
    }

    /**
     * 객체를 최대 길이 제한과 함께 추가
     *
     * @param streamKey 스트림 키
     * @param object 추가할 객체
     * @param maxLen 최대 메시지 개수
     * @return 생성된 메시지 ID
     */
    public String addObjectWithMaxLen(String streamKey, Object object, long maxLen) {
        try {
            Map<String, String> fields = convertToMap(object);
            return addWithMaxLen(streamKey, fields, maxLen);
        } catch (Exception e) {
            log.error("[Stream] Failed to add object with maxlen to stream '{}'", streamKey, e);
            throw new RuntimeException("Failed to add object to stream", e);
        }
    }

    /**
     * 스트림 길이 조회
     *
     * @param streamKey 스트림 키
     * @return 스트림의 메시지 개수
     */
    public Long getStreamLength(String streamKey) {
        try {
            return redisTemplate.opsForStream().size(streamKey);
        } catch (Exception e) {
            log.error("[Stream] Failed to get stream length for '{}'", streamKey, e);
            return 0L;
        }
    }

    /**
     * 스트림 삭제
     *
     * @param streamKey 스트림 키
     */
    public void deleteStream(String streamKey) {
        try {
            redisTemplate.delete(streamKey);
            log.info("[Stream] Deleted stream: {}", streamKey);
        } catch (Exception e) {
            log.error("[Stream] Failed to delete stream '{}'", streamKey, e);
        }
    }

    /**
     * 특정 메시지 삭제
     *
     * @param streamKey 스트림 키
     * @param messageIds 메시지 ID 목록
     * @return 삭제된 메시지 수
     */
    public Long deleteMessages(String streamKey, String... messageIds) {
        try {
            RecordId[] recordIds = new RecordId[messageIds.length];
            for (int i = 0; i < messageIds.length; i++) {
                recordIds[i] = RecordId.of(messageIds[i]);
            }
            Long deleted = redisTemplate.opsForStream().delete(streamKey, recordIds);
            log.debug("[Stream] Deleted {} messages from stream '{}'", deleted, streamKey);
            return deleted;
        } catch (Exception e) {
            log.error("[Stream] Failed to delete messages from stream '{}'", streamKey, e);
            return 0L;
        }
    }

    /**
     * 객체를 Map<String, String>으로 변환
     */
    private Map<String, String> convertToMap(Object object) throws Exception {
        String json = objectMapper.writeValueAsString(object);
        Map<String, String> fields = new HashMap<>();
        fields.put("_type", object.getClass().getName());
        fields.put("_data", json);
        return fields;
    }
}
