package com.eraf.starter.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Batch Configuration Properties
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
}
