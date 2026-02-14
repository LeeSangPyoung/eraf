package com.eraf.report;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Report Configuration Properties
 *
 * <p>Configuration example:</p>
 * <pre>
 * eraf:
 *   report:
 *     enabled: true
 *     default-format: CSV
 *     output-dir: /tmp/reports
 *     max-rows: 100000
 * </pre>
 */
@ConfigurationProperties(prefix = "eraf.report")
public class ReportProperties {

    /**
     * Enable/disable report module
     */
    private boolean enabled = true;

    /**
     * Default report format when not specified
     */
    private ReportFormat defaultFormat = ReportFormat.CSV;

    /**
     * Default output directory for generated report files
     */
    private String outputDir;

    /**
     * Maximum number of data rows allowed per report (0 = unlimited)
     */
    private int maxRows = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ReportFormat getDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(ReportFormat defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
}
