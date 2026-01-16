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
     * 로깅 설정
     */
    private LoggingConfig logging = new LoggingConfig();

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

    /**
     * 파일 업로드 설정
     */
    private FileUploadConfig fileUpload = new FileUploadConfig();

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

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
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

    public FileUploadConfig getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(FileUploadConfig fileUpload) {
        this.fileUpload = fileUpload;
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

    /**
     * 로깅 설정
     */
    public static class LoggingConfig {
        /**
         * 로깅 활성화
         */
        private boolean enabled = true;

        /**
         * 요청 본문 로깅 포함
         */
        private boolean includePayload = true;

        /**
         * 최대 페이로드 길이
         */
        private int maxPayloadLength = 1000;

        /**
         * 로깅 제외 경로
         */
        private String[] excludePatterns = {"/actuator", "/health", "/favicon.ico"};

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludePayload() {
            return includePayload;
        }

        public void setIncludePayload(boolean includePayload) {
            this.includePayload = includePayload;
        }

        public int getMaxPayloadLength() {
            return maxPayloadLength;
        }

        public void setMaxPayloadLength(int maxPayloadLength) {
            this.maxPayloadLength = maxPayloadLength;
        }

        public String[] getExcludePatterns() {
            return excludePatterns;
        }

        public void setExcludePatterns(String[] excludePatterns) {
            this.excludePatterns = excludePatterns;
        }
    }

    /**
     * 파일 업로드 설정
     */
    public static class FileUploadConfig {
        /**
         * 파일 업로드 기능 활성화
         */
        private boolean enabled = true;

        /**
         * 파일 저장 경로
         */
        private String uploadPath = "./uploads";

        /**
         * 파일 접근 URL prefix
         */
        private String baseUrl = "/files";

        /**
         * 날짜별 디렉토리 생성 여부
         */
        private boolean createDateDirectory = true;

        /**
         * 최대 파일 크기 (MB)
         */
        private int maxFileSizeMB = 10;

        /**
         * 허용 확장자 (빈 배열이면 모두 허용)
         */
        private String[] allowedExtensions = {};

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUploadPath() {
            return uploadPath;
        }

        public void setUploadPath(String uploadPath) {
            this.uploadPath = uploadPath;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public boolean isCreateDateDirectory() {
            return createDateDirectory;
        }

        public void setCreateDateDirectory(boolean createDateDirectory) {
            this.createDateDirectory = createDateDirectory;
        }

        public int getMaxFileSizeMB() {
            return maxFileSizeMB;
        }

        public void setMaxFileSizeMB(int maxFileSizeMB) {
            this.maxFileSizeMB = maxFileSizeMB;
        }

        public String[] getAllowedExtensions() {
            return allowedExtensions;
        }

        public void setAllowedExtensions(String[] allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
        }
    }
}
