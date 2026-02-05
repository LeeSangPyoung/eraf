package com.eraf.kafka.dlq;

import java.time.Instant;
import java.util.Map;

/**
 * Dead Letter Queue 메시지
 */
public class DeadLetterMessage {

    /**
     * 원본 토픽
     */
    private String originalTopic;

    /**
     * 원본 파티션
     */
    private int originalPartition;

    /**
     * 원본 오프셋
     */
    private long originalOffset;

    /**
     * 원본 키
     */
    private String originalKey;

    /**
     * 원본 메시지 (JSON)
     */
    private String originalMessage;

    /**
     * 원본 헤더
     */
    private Map<String, String> originalHeaders;

    /**
     * 에러 발생 시간
     */
    private Instant errorTimestamp;

    /**
     * 에러 메시지
     */
    private String errorMessage;

    /**
     * 에러 클래스
     */
    private String errorClass;

    /**
     * 스택 트레이스
     */
    private String stackTrace;

    /**
     * 재시도 횟수
     */
    private int retryCount;

    /**
     * 컨슈머 그룹 ID
     */
    private String consumerGroup;

    /**
     * 트레이스 ID
     */
    private String traceId;

    // Getters and Setters

    public String getOriginalTopic() {
        return originalTopic;
    }

    public void setOriginalTopic(String originalTopic) {
        this.originalTopic = originalTopic;
    }

    public int getOriginalPartition() {
        return originalPartition;
    }

    public void setOriginalPartition(int originalPartition) {
        this.originalPartition = originalPartition;
    }

    public long getOriginalOffset() {
        return originalOffset;
    }

    public void setOriginalOffset(long originalOffset) {
        this.originalOffset = originalOffset;
    }

    public String getOriginalKey() {
        return originalKey;
    }

    public void setOriginalKey(String originalKey) {
        this.originalKey = originalKey;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public Map<String, String> getOriginalHeaders() {
        return originalHeaders;
    }

    public void setOriginalHeaders(Map<String, String> originalHeaders) {
        this.originalHeaders = originalHeaders;
    }

    public Instant getErrorTimestamp() {
        return errorTimestamp;
    }

    public void setErrorTimestamp(Instant errorTimestamp) {
        this.errorTimestamp = errorTimestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DeadLetterMessage message = new DeadLetterMessage();

        public Builder originalTopic(String topic) {
            message.originalTopic = topic;
            return this;
        }

        public Builder originalPartition(int partition) {
            message.originalPartition = partition;
            return this;
        }

        public Builder originalOffset(long offset) {
            message.originalOffset = offset;
            return this;
        }

        public Builder originalKey(String key) {
            message.originalKey = key;
            return this;
        }

        public Builder originalMessage(String msg) {
            message.originalMessage = msg;
            return this;
        }

        public Builder originalHeaders(Map<String, String> headers) {
            message.originalHeaders = headers;
            return this;
        }

        public Builder error(Throwable t) {
            message.errorTimestamp = Instant.now();
            message.errorMessage = t.getMessage();
            message.errorClass = t.getClass().getName();

            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : t.getStackTrace()) {
                sb.append(element.toString()).append("\n");
            }
            message.stackTrace = sb.toString();
            return this;
        }

        public Builder retryCount(int count) {
            message.retryCount = count;
            return this;
        }

        public Builder consumerGroup(String group) {
            message.consumerGroup = group;
            return this;
        }

        public Builder traceId(String traceId) {
            message.traceId = traceId;
            return this;
        }

        public DeadLetterMessage build() {
            return message;
        }
    }
}
