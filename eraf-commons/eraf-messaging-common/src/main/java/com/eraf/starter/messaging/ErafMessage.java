package com.eraf.starter.messaging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ERAF 표준 메시지 포맷
 */
public class ErafMessage<T> {

    private String messageId;
    private String traceId;
    private String source;
    private String type;
    private T payload;
    private Map<String, Object> headers;
    private Instant timestamp;
    private int retryCount;

    public ErafMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.headers = new HashMap<>();
        this.retryCount = 0;
    }

    public ErafMessage(T payload) {
        this();
        this.payload = payload;
        this.type = payload != null ? payload.getClass().getSimpleName() : null;
    }

    public static <T> ErafMessage<T> of(T payload) {
        return new ErafMessage<>(payload);
    }

    public ErafMessage<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ErafMessage<T> withSource(String source) {
        this.source = source;
        return this;
    }

    public ErafMessage<T> withHeader(String key, Object value) {
        this.headers.put(key, value);
        return this;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
