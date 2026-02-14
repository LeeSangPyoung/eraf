package com.eraf.report;

/**
 * Supported report output formats.
 *
 * <p>Each format defines its MIME type and file extension for download/storage.</p>
 */
public enum ReportFormat {

    PDF("application/pdf", "pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    CSV("text/csv", "csv"),
    HTML("text/html", "html");

    private final String mimeType;
    private final String extension;

    ReportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }
}
