package com.eraf.starter.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Web Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.web")
public class ErafWebProperties {

    /**
     * Enable CORS support
     */
    private boolean corsEnabled = true;

    /**
     * CORS allowed origins
     */
    private String[] corsAllowedOrigins = {"*"};

    /**
     * CORS allowed methods
     */
    private String[] corsAllowedMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

    /**
     * CORS allowed headers
     */
    private String[] corsAllowedHeaders = {"*"};

    /**
     * CORS max age in seconds
     */
    private long corsMaxAge = 3600;

    /**
     * Enable request/response logging
     */
    private boolean loggingEnabled = true;

    /**
     * Include request body in logging
     */
    private boolean loggingIncludeBody = false;

    /**
     * Idempotent configuration
     */
    private Idempotent idempotent = new Idempotent();

    /**
     * Lock configuration
     */
    private Lock lock = new Lock();

    /**
     * Feature toggle configuration
     */
    private FeatureToggleConfig featureToggle = new FeatureToggleConfig();

    /**
     * Sequence configuration
     */
    private SequenceConfig sequence = new SequenceConfig();

    /**
     * I18n configuration
     */
    private I18nConfig i18n = new I18nConfig();

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public String[] getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String[] corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public String[] getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String[] corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }

    public String[] getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String[] corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    public long getCorsMaxAge() {
        return corsMaxAge;
    }

    public void setCorsMaxAge(long corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public boolean isLoggingIncludeBody() {
        return loggingIncludeBody;
    }

    public void setLoggingIncludeBody(boolean loggingIncludeBody) {
        this.loggingIncludeBody = loggingIncludeBody;
    }

    public Idempotent getIdempotent() {
        return idempotent;
    }

    public void setIdempotent(Idempotent idempotent) {
        this.idempotent = idempotent;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    /**
     * 멱등성 설정
     */
    public static class Idempotent {
        /**
         * 멱등성 기능 활성화
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 락 설정
     */
    public static class Lock {
        /**
         * 분산 락 기능 활성화
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public FeatureToggleConfig getFeatureToggle() {
        return featureToggle;
    }

    public void setFeatureToggle(FeatureToggleConfig featureToggle) {
        this.featureToggle = featureToggle;
    }

    public SequenceConfig getSequence() {
        return sequence;
    }

    public void setSequence(SequenceConfig sequence) {
        this.sequence = sequence;
    }

    public I18nConfig getI18n() {
        return i18n;
    }

    public void setI18n(I18nConfig i18n) {
        this.i18n = i18n;
    }

    /**
     * 기능 토글 설정
     */
    public static class FeatureToggleConfig {
        /**
         * 기능 토글 활성화
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 채번 설정
     */
    public static class SequenceConfig {
        /**
         * 채번 기능 활성화
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 국제화 설정
     */
    public static class I18nConfig {
        /**
         * 국제화 기능 활성화
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
