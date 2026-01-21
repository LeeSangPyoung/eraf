package com.eraf.starter.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Batch Configuration Properties
 * 배치 잡 공통 설정
 */
@ConfigurationProperties(prefix = "eraf.batch")
public class ErafBatchProperties {

    /**
     * Enable batch job auto start
     */
    private boolean jobEnabled = true;

    /**
     * Default chunk size
     */
    private int chunkSize = 100;

    /**
     * Enable restart on failure
     */
    private boolean restartOnFailure = true;

    /**
     * Batch table prefix
     */
    private String tablePrefix = "ERAF_BATCH_";

    /**
     * 재시도 설정
     */
    private RetryProperties retry = new RetryProperties();

    /**
     * 스킵 설정
     */
    private SkipProperties skip = new SkipProperties();

    /**
     * 스레드 풀 설정
     */
    private ThreadPoolProperties threadPool = new ThreadPoolProperties();

    public boolean isJobEnabled() {
        return jobEnabled;
    }

    public void setJobEnabled(boolean jobEnabled) {
        this.jobEnabled = jobEnabled;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public boolean isRestartOnFailure() {
        return restartOnFailure;
    }

    public void setRestartOnFailure(boolean restartOnFailure) {
        this.restartOnFailure = restartOnFailure;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public SkipProperties getSkip() {
        return skip;
    }

    public void setSkip(SkipProperties skip) {
        this.skip = skip;
    }

    public ThreadPoolProperties getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPoolProperties threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * 재시도 설정
     */
    public static class RetryProperties {
        /**
         * 재시도 활성화
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
    }

    /**
     * 스킵 설정
     */
    public static class SkipProperties {
        /**
         * 스킵 활성화
         */
        private boolean enabled = true;

        /**
         * 최대 스킵 횟수
         */
        private int maxSkips = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxSkips() {
            return maxSkips;
        }

        public void setMaxSkips(int maxSkips) {
            this.maxSkips = maxSkips;
        }
    }

    /**
     * 스레드 풀 설정
     */
    public static class ThreadPoolProperties {
        /**
         * 병렬 처리 활성화
         */
        private boolean enabled = false;

        /**
         * 코어 스레드 수
         */
        private int corePoolSize = 4;

        /**
         * 최대 스레드 수
         */
        private int maxPoolSize = 8;

        /**
         * 큐 용량
         */
        private int queueCapacity = 100;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }
}
