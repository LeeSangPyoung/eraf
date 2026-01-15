package com.eraf.starter.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Kafka Configuration Properties
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
}
