package com.eraf.redis.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Redis Streams 메시지 소비자
 *
 * <p>Redis Streams에서 메시지를 읽고 처리합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 특정 ID 이후의 메시지 읽기
 * List&lt;Map&lt;String, String&gt;&gt; messages = consumer.read("orders", "0");
 *
 * // 컨슈머 그룹으로 메시지 읽기
 * consumer.createGroup("orders", "order-processors");
 * List&lt;Map&lt;String, String&gt;&gt; messages = consumer.readGroup("orders", "order-processors", "consumer-1");
 *
 * // 메시지 ACK
 * consumer.acknowledge("orders", "order-processors", messageId);
 * </pre>
 */
public class RedisStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamConsumer.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public RedisStreamConsumer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 스트림에서 메시지 읽기
     *
     * @param streamKey 스트림 키
     * @param offset 시작 ID ("0"은 처음부터, "$"는 새 메시지만)
     * @return 메시지 목록
     */
    public List<MapRecord<String, Object, Object>> read(String streamKey, String offset) {
        try {
            var records = redisTemplate.opsForStream().read(
                StreamOffset.create(streamKey, ReadOffset.from(offset))
            );
            log.debug("[Stream] Read {} messages from stream '{}'", records != null ? records.size() : 0, streamKey);
            return records;
        } catch (Exception e) {
            log.error("[Stream] Failed to read from stream '{}'", streamKey, e);
            throw new RuntimeException("Failed to read from stream", e);
        }
    }

    /**
     * 여러 스트림에서 메시지 읽기
     *
     * @param streamOffsets 스트림과 오프셋 맵
     * @return 메시지 목록
     */
    @SuppressWarnings("unchecked")
    public List<MapRecord<String, Object, Object>> readMultiple(Map<String, String> streamOffsets) {
        try {
            StreamOffset<String>[] offsets = streamOffsets.entrySet().stream()
                .map(entry -> StreamOffset.create(entry.getKey(), ReadOffset.from(entry.getValue())))
                .toArray(StreamOffset[]::new);

            var records = redisTemplate.opsForStream().read(offsets);
            log.debug("[Stream] Read {} messages from {} streams", records != null ? records.size() : 0, streamOffsets.size());
            return records;
        } catch (Exception e) {
            log.error("[Stream] Failed to read from multiple streams", e);
            throw new RuntimeException("Failed to read from streams", e);
        }
    }

    /**
     * 컨슈머 그룹 생성
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     */
    public void createGroup(String streamKey, String groupName) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, groupName);
            log.info("[Stream] Created consumer group '{}' for stream '{}'", groupName, streamKey);
        } catch (Exception e) {
            // 그룹이 이미 존재하면 무시
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("[Stream] Consumer group '{}' already exists for stream '{}'", groupName, streamKey);
            } else {
                log.error("[Stream] Failed to create consumer group '{}' for stream '{}'", groupName, streamKey, e);
                throw new RuntimeException("Failed to create consumer group", e);
            }
        }
    }

    /**
     * 컨슈머 그룹으로 메시지 읽기
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     * @param consumerName 컨슈머 이름
     * @return 메시지 목록
     */
    public List<MapRecord<String, Object, Object>> readGroup(String streamKey, String groupName, String consumerName) {
        try {
            var records = redisTemplate.opsForStream().read(
                Consumer.from(groupName, consumerName),
                StreamReadOptions.empty().count(10),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );
            log.debug("[Stream] Consumer '{}' read {} messages from group '{}'",
                consumerName, records != null ? records.size() : 0, groupName);
            return records;
        } catch (Exception e) {
            log.error("[Stream] Failed to read from group '{}' with consumer '{}'", groupName, consumerName, e);
            throw new RuntimeException("Failed to read from consumer group", e);
        }
    }

    /**
     * 블로킹 방식으로 메시지 읽기
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     * @param consumerName 컨슈머 이름
     * @param timeout 타임아웃
     * @return 메시지 목록
     */
    public List<MapRecord<String, Object, Object>> readGroupBlocking(
            String streamKey, String groupName, String consumerName, Duration timeout) {
        try {
            var records = redisTemplate.opsForStream().read(
                Consumer.from(groupName, consumerName),
                StreamReadOptions.empty().count(10).block(timeout),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );
            log.debug("[Stream] Consumer '{}' read {} messages (blocking) from group '{}'",
                consumerName, records != null ? records.size() : 0, groupName);
            return records;
        } catch (Exception e) {
            log.error("[Stream] Failed to read (blocking) from group '{}' with consumer '{}'", groupName, consumerName, e);
            throw new RuntimeException("Failed to read from consumer group (blocking)", e);
        }
    }

    /**
     * 메시지 확인 (ACK)
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     * @param messageIds 메시지 ID 목록
     * @return ACK된 메시지 수
     */
    public Long acknowledge(String streamKey, String groupName, String... messageIds) {
        try {
            RecordId[] recordIds = new RecordId[messageIds.length];
            for (int i = 0; i < messageIds.length; i++) {
                recordIds[i] = RecordId.of(messageIds[i]);
            }
            Long acked = redisTemplate.opsForStream().acknowledge(streamKey, groupName, recordIds);
            log.debug("[Stream] Acknowledged {} messages in group '{}'", acked, groupName);
            return acked;
        } catch (Exception e) {
            log.error("[Stream] Failed to acknowledge messages in group '{}'", groupName, e);
            return 0L;
        }
    }

    /**
     * Pending 메시지 조회 (ACK되지 않은 메시지)
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     * @return Pending 메시지 정보
     */
    public PendingMessagesSummary getPending(String streamKey, String groupName) {
        try {
            var pending = redisTemplate.opsForStream().pending(streamKey, groupName);
            log.debug("[Stream] Found {} pending messages in group '{}'", pending.getTotalPendingMessages(), groupName);
            return pending;
        } catch (Exception e) {
            log.error("[Stream] Failed to get pending messages for group '{}'", groupName, e);
            throw new RuntimeException("Failed to get pending messages", e);
        }
    }

    /**
     * 컨슈머 그룹 삭제
     *
     * @param streamKey 스트림 키
     * @param groupName 그룹 이름
     */
    public void destroyGroup(String streamKey, String groupName) {
        try {
            redisTemplate.opsForStream().destroyGroup(streamKey, groupName);
            log.info("[Stream] Destroyed consumer group '{}' for stream '{}'", groupName, streamKey);
        } catch (Exception e) {
            log.error("[Stream] Failed to destroy consumer group '{}' for stream '{}'", groupName, streamKey, e);
        }
    }

    /**
     * 객체로 역직렬화
     *
     * @param record 레코드
     * @param type 타입
     * @return 역직렬화된 객체
     */
    public <T> T deserialize(MapRecord<String, Object, Object> record, Class<T> type) {
        try {
            Object data = record.getValue().get("_data");
            if (data != null) {
                return objectMapper.readValue(data.toString(), type);
            }
            return null;
        } catch (Exception e) {
            log.error("[Stream] Failed to deserialize record", e);
            return null;
        }
    }
}
