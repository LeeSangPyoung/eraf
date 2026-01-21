package com.eraf.starter.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Kafka Configuration Properties
 * Kafka 프로듀서/컨슈머 공통 설정
 */
@ConfigurationProperties(prefix = "eraf.kafka")
public class ErafKafkaProperties {

    /**
     * Default topic prefix
     */
    private String topicPrefix = "eraf-";

    /**
     * Consumer group ID prefix
     */
    private String groupIdPrefix = "eraf-group-";

    /**
     * Enable auto commit
     */
    private boolean enableAutoCommit = false;

    /**
     * Auto offset reset
     */
    private String autoOffsetReset = "earliest";

    /**
     * Max poll records
     */
    private int maxPollRecords = 500;

    /**
     * 재시도 설정
     */
    private RetryProperties retry = new RetryProperties();

    /**
     * Dead Letter Queue 설정
     */
    private DlqProperties dlq = new DlqProperties();

    /**
     * 트랜잭션 설정
     */
    private TransactionProperties transaction = new TransactionProperties();

    /**
     * 프로듀서 설정
     */
    private ProducerProperties producer = new ProducerProperties();

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getGroupIdPrefix() {
        return groupIdPrefix;
    }

    public void setGroupIdPrefix(String groupIdPrefix) {
        this.groupIdPrefix = groupIdPrefix;
    }

    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(boolean enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public DlqProperties getDlq() {
        return dlq;
    }

    public void setDlq(DlqProperties dlq) {
        this.dlq = dlq;
    }

    public TransactionProperties getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionProperties transaction) {
        this.transaction = transaction;
    }

    public ProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(ProducerProperties producer) {
        this.producer = producer;
    }

    /**
     * 재시도 설정
     */
    public static class RetryProperties {
        /**
         * 재시도 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 최대 재시도 횟수
         */
        private int maxAttempts = 3;

        /**
         * 재시도 간격 (밀리초)
         */
        private long backoffInterval = 1000;

        /**
         * 백오프 승수 (지수 백오프용)
         */
        private double backoffMultiplier = 2.0;

        /**
         * 최대 백오프 간격 (밀리초)
         */
        private long maxBackoffInterval = 30000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoffInterval() {
            return backoffInterval;
        }

        public void setBackoffInterval(long backoffInterval) {
            this.backoffInterval = backoffInterval;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }

        public long getMaxBackoffInterval() {
            return maxBackoffInterval;
        }

        public void setMaxBackoffInterval(long maxBackoffInterval) {
            this.maxBackoffInterval = maxBackoffInterval;
        }
    }

    /**
     * Dead Letter Queue 설정
     */
    public static class DlqProperties {
        /**
         * DLQ 활성화 여부
         */
        private boolean enabled = true;

        /**
         * DLQ 토픽 접미사
         */
        private String topicSuffix = ".DLQ";

        /**
         * DLQ 보존 기간 (일)
         */
        private int retentionDays = 7;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTopicSuffix() {
            return topicSuffix;
        }

        public void setTopicSuffix(String topicSuffix) {
            this.topicSuffix = topicSuffix;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    /**
     * 트랜잭션 설정
     */
    public static class TransactionProperties {
        /**
         * 트랜잭션 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 트랜잭션 ID 접두사
         */
        private String idPrefix = "eraf-tx-";

        /**
         * 트랜잭션 타임아웃 (초)
         */
        private int timeoutSeconds = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getIdPrefix() {
            return idPrefix;
        }

        public void setIdPrefix(String idPrefix) {
            this.idPrefix = idPrefix;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    /**
     * 프로듀서 설정
     */
    public static class ProducerProperties {
        /**
         * 압축 타입 (none, gzip, snappy, lz4, zstd)
         */
        private String compressionType = "none";

        /**
         * 배치 크기 (바이트)
         */
        private int batchSize = 16384;

        /**
         * 링거 시간 (밀리초)
         */
        private int lingerMs = 0;

        /**
         * 버퍼 메모리 (바이트)
         */
        private long bufferMemory = 33554432;

        /**
         * acks 설정 (0, 1, all)
         */
        private String acks = "all";

        /**
         * idempotence 활성화
         */
        private boolean idempotence = true;

        public String getCompressionType() {
            return compressionType;
        }

        public void setCompressionType(String compressionType) {
            this.compressionType = compressionType;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getLingerMs() {
            return lingerMs;
        }

        public void setLingerMs(int lingerMs) {
            this.lingerMs = lingerMs;
        }

        public long getBufferMemory() {
            return bufferMemory;
        }

        public void setBufferMemory(long bufferMemory) {
            this.bufferMemory = bufferMemory;
        }

        public String getAcks() {
            return acks;
        }

        public void setAcks(String acks) {
            this.acks = acks;
        }

        public boolean isIdempotence() {
            return idempotence;
        }

        public void setIdempotence(boolean idempotence) {
            this.idempotence = idempotence;
        }
    }
}
