package com.eraf.actuator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Actuator Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.actuator")
public class ErafActuatorProperties {

    /**
     * Enable custom health indicators
     */
    private boolean healthEnabled = true;

    /**
     * Enable metrics collection
     */
    private boolean metricsEnabled = true;

    /**
     * Application name for metrics tagging
     */
    private String applicationName = "eraf-app";

    /**
     * Health indicator 개별 설정
     */
    private HealthConfig health = new HealthConfig();

    public boolean isHealthEnabled() {
        return healthEnabled;
    }

    public void setHealthEnabled(boolean healthEnabled) {
        this.healthEnabled = healthEnabled;
    }

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public HealthConfig getHealth() {
        return health;
    }

    public void setHealth(HealthConfig health) {
        this.health = health;
    }

    /**
     * Health Indicator 설정
     */
    public static class HealthConfig {
        private IndicatorConfig redis = new IndicatorConfig();
        private IndicatorConfig database = new IndicatorConfig();
        private IndicatorConfig kafka = new IndicatorConfig();
        private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();
        private S3Config s3 = new S3Config();

        public IndicatorConfig getRedis() {
            return redis;
        }

        public void setRedis(IndicatorConfig redis) {
            this.redis = redis;
        }

        public IndicatorConfig getDatabase() {
            return database;
        }

        public void setDatabase(IndicatorConfig database) {
            this.database = database;
        }

        public IndicatorConfig getKafka() {
            return kafka;
        }

        public void setKafka(IndicatorConfig kafka) {
            this.kafka = kafka;
        }

        public ElasticsearchConfig getElasticsearch() {
            return elasticsearch;
        }

        public void setElasticsearch(ElasticsearchConfig elasticsearch) {
            this.elasticsearch = elasticsearch;
        }

        public S3Config getS3() {
            return s3;
        }

        public void setS3(S3Config s3) {
            this.s3 = s3;
        }
    }

    /**
     * Elasticsearch Health 설정
     */
    public static class ElasticsearchConfig {
        private String url;
        private long timeoutMs = 5000L;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    /**
     * S3 Health 설정
     */
    public static class S3Config {
        private String endpoint;
        private String bucket;
        private long timeoutMs = 5000L;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    /**
     * 개별 Indicator 설정
     */
    public static class IndicatorConfig {
        private boolean enabled = true;
        private long timeoutMs = 5000L;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}
