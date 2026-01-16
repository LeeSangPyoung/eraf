package com.eraf.starter.kafka;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * ERAF 표준 Kafka 이벤트 포맷
 */
public class ErafKafkaEvent<T> {

    /**
     * 이벤트 고유 ID
     */
    private String eventId;

    /**
     * 이벤트 타입 (예: ORDER_CREATED, PAYMENT_COMPLETED)
     */
    private String eventType;

    /**
     * 이벤트 발생 시간
     */
    private Instant timestamp;

    /**
     * 소스 서비스명
     */
    private String source;

    /**
     * 추적 ID (분산 추적용)
     */
    private String traceId;

    /**
     * 이벤트 버전
     */
    private String version;

    /**
     * 이벤트 페이로드
     */
    private T payload;

    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata;

    public ErafKafkaEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.version = "1.0";
    }

    public ErafKafkaEvent(String eventType, T payload) {
        this();
        this.eventType = eventType;
        this.payload = payload;
    }

    public static <T> ErafKafkaEvent<T> of(String eventType, T payload) {
        return new ErafKafkaEvent<>(eventType, payload);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    // Getters and Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static class Builder<T> {
        private final ErafKafkaEvent<T> event = new ErafKafkaEvent<>();

        public Builder<T> eventType(String eventType) {
            event.setEventType(eventType);
            return this;
        }

        public Builder<T> source(String source) {
            event.setSource(source);
            return this;
        }

        public Builder<T> traceId(String traceId) {
            event.setTraceId(traceId);
            return this;
        }

        public Builder<T> payload(T payload) {
            event.setPayload(payload);
            return this;
        }

        public Builder<T> metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }

        public ErafKafkaEvent<T> build() {
            return event;
        }
    }
}
