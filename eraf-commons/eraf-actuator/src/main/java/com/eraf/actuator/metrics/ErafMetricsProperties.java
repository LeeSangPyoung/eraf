package com.eraf.actuator.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ERAF Metrics 설정
 */
@ConfigurationProperties(prefix = "eraf.actuator.metrics")
public class ErafMetricsProperties {

    /**
     * 메트릭 수집 활성화
     */
    private boolean enabled = true;

    /**
     * 공통 태그
     */
    private List<Tag> commonTags = new ArrayList<>();

    /**
     * HTTP 요청 메트릭 활성화
     */
    private boolean httpEnabled = true;

    /**
     * 메서드 실행 시간 메트릭 활성화
     */
    private boolean methodTimingEnabled = true;

    /**
     * 캐시 메트릭 활성화
     */
    private boolean cacheEnabled = true;

    /**
     * DB 메트릭 활성화
     */
    private boolean databaseEnabled = true;

    /**
     * 메시지 큐 메트릭 활성화
     */
    private boolean messagingEnabled = true;

    /**
     * 비즈니스 메트릭 접두어
     */
    private String businessPrefix = "business";

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Tag> getCommonTags() {
        return commonTags;
    }

    public void setCommonTags(List<Tag> commonTags) {
        this.commonTags = commonTags;
    }

    public boolean isHttpEnabled() {
        return httpEnabled;
    }

    public void setHttpEnabled(boolean httpEnabled) {
        this.httpEnabled = httpEnabled;
    }

    public boolean isMethodTimingEnabled() {
        return methodTimingEnabled;
    }

    public void setMethodTimingEnabled(boolean methodTimingEnabled) {
        this.methodTimingEnabled = methodTimingEnabled;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public void setDatabaseEnabled(boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public boolean isMessagingEnabled() {
        return messagingEnabled;
    }

    public void setMessagingEnabled(boolean messagingEnabled) {
        this.messagingEnabled = messagingEnabled;
    }

    public String getBusinessPrefix() {
        return businessPrefix;
    }

    public void setBusinessPrefix(String businessPrefix) {
        this.businessPrefix = businessPrefix;
    }

    public static class Tag {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
