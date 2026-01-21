package com.eraf.starter.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Redis Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.redis")
public class ErafRedisProperties {

    /**
     * Default cache TTL
     */
    private Duration defaultTtl = Duration.ofHours(1);

    /**
     * Enable cache prefix
     */
    private boolean enablePrefix = true;

    /**
     * Cache key prefix
     */
    private String keyPrefix = "eraf:";

    /**
     * Enable transaction support
     */
    private boolean enableTransactions = false;

    /**
     * 분산 락 설정
     */
    private Lock lock = new Lock();

    /**
     * 시퀀스 설정
     */
    private Sequence sequence = new Sequence();

    /**
     * 멱등성 설정
     */
    private Idempotent idempotent = new Idempotent();

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public boolean isEnablePrefix() {
        return enablePrefix;
    }

    public void setEnablePrefix(boolean enablePrefix) {
        this.enablePrefix = enablePrefix;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public boolean isEnableTransactions() {
        return enableTransactions;
    }

    public void setEnableTransactions(boolean enableTransactions) {
        this.enableTransactions = enableTransactions;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Idempotent getIdempotent() {
        return idempotent;
    }

    public void setIdempotent(Idempotent idempotent) {
        this.idempotent = idempotent;
    }

    /**
     * 분산 락 설정
     */
    public static class Lock {
        private boolean enabled = true;
        private Duration defaultLeaseTime = Duration.ofSeconds(30);
        private Duration defaultWaitTime = Duration.ofSeconds(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getDefaultLeaseTime() {
            return defaultLeaseTime;
        }

        public void setDefaultLeaseTime(Duration defaultLeaseTime) {
            this.defaultLeaseTime = defaultLeaseTime;
        }

        public Duration getDefaultWaitTime() {
            return defaultWaitTime;
        }

        public void setDefaultWaitTime(Duration defaultWaitTime) {
            this.defaultWaitTime = defaultWaitTime;
        }
    }

    /**
     * 시퀀스 설정
     */
    public static class Sequence {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 멱등성 설정
     */
    public static class Idempotent {
        private boolean enabled = true;
        private Duration ttl = Duration.ofHours(24);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
