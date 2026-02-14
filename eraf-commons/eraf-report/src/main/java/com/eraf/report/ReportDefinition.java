package com.eraf.report;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines the structure and metadata of a report.
 *
 * <p>A report definition describes what the report is, what format it should
 * be generated in, and what parameters it accepts. Use the {@link Builder}
 * to create instances.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * ReportDefinition definition = ReportDefinition.builder("salesReport", ReportFormat.CSV)
 *     .title("Monthly Sales Report")
 *     .description("Sales summary by region")
 *     .parameter(ReportParameter.builder("month", ReportParameter.Type.STRING)
 *         .required(true)
 *         .description("Target month (yyyy-MM)")
 *         .build())
 *     .build();
 * </pre>
 */
public class ReportDefinition {

    private final String name;
    private final String title;
    private final String description;
    private final ReportFormat format;
    private final Map<String, ReportParameter> parameters;

    private ReportDefinition(Builder builder) {
        this.name = builder.name;
        this.title = builder.title;
        this.description = builder.description;
        this.format = builder.format;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public Map<String, ReportParameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the file name for this report (name + format extension).
     */
    public String getFileName() {
        return name + "." + format.getExtension();
    }

    public static Builder builder(String name, ReportFormat format) {
        return new Builder(name, format);
    }

    public static class Builder {
        private final String name;
        private final ReportFormat format;
        private String title;
        private String description;
        private final Map<String, ReportParameter> parameters = new LinkedHashMap<>();

        private Builder(String name, ReportFormat format) {
            this.name = name;
            this.format = format;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameter(ReportParameter parameter) {
            this.parameters.put(parameter.getName(), parameter);
            return this;
        }

        public ReportDefinition build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Report name must not be blank");
            }
            if (format == null) {
                throw new IllegalArgumentException("Report format must not be null");
            }
            return new ReportDefinition(this);
        }
    }
}
