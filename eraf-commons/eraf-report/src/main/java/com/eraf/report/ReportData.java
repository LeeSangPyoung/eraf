package com.eraf.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for report data to be rendered by a {@link ReportGenerator}.
 *
 * <p>Holds tabular data as headers + rows, along with pagination metadata.
 * Use the {@link Builder} to create instances.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * ReportData data = ReportData.builder()
 *     .headers(List.of("Name", "Amount", "Date"))
 *     .addRow(Map.of("Name", "Item A", "Amount", "1000", "Date", "2026-01-01"))
 *     .addRow(Map.of("Name", "Item B", "Amount", "2000", "Date", "2026-01-02"))
 *     .totalCount(100)
 *     .page(1)
 *     .pageSize(10)
 *     .metadata("generatedBy", "system")
 *     .build();
 * </pre>
 */
public class ReportData {

    private final List<String> headers;
    private final List<Map<String, Object>> rows;
    private final Map<String, Object> metadata;
    private final long totalCount;
    private final int page;
    private final int pageSize;

    private ReportData(Builder builder) {
        this.headers = Collections.unmodifiableList(new ArrayList<>(builder.headers));
        this.rows = Collections.unmodifiableList(new ArrayList<>(builder.rows));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.totalCount = builder.totalCount;
        this.page = builder.page;
        this.pageSize = builder.pageSize;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns true if there are no rows.
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> headers = new ArrayList<>();
        private final List<Map<String, Object>> rows = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private long totalCount = 0;
        private int page = 1;
        private int pageSize = 0;

        private Builder() {
        }

        public Builder headers(List<String> headers) {
            this.headers = new ArrayList<>(headers);
            return this;
        }

        public Builder addRow(Map<String, Object> row) {
            this.rows.add(new HashMap<>(row));
            return this;
        }

        public Builder rows(List<Map<String, Object>> rows) {
            this.rows.clear();
            for (Map<String, Object> row : rows) {
                this.rows.add(new HashMap<>(row));
            }
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder totalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public ReportData build() {
            return new ReportData(this);
        }
    }
}
