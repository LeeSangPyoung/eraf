package com.eraf.report;

/**
 * Defines an input parameter for a report.
 *
 * <p>Report parameters allow callers to customize report generation
 * by providing values such as date ranges, filters, or flags.</p>
 */
public class ReportParameter {

    /**
     * Parameter types supported by report definitions.
     */
    public enum Type {
        STRING,
        NUMBER,
        DATE,
        BOOLEAN
    }

    private final String name;
    private final Type type;
    private final boolean required;
    private final String defaultValue;
    private final String description;

    private ReportParameter(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.required = builder.required;
        this.defaultValue = builder.defaultValue;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public static Builder builder(String name, Type type) {
        return new Builder(name, type);
    }

    public static class Builder {
        private final String name;
        private final Type type;
        private boolean required = false;
        private String defaultValue;
        private String description;

        private Builder(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ReportParameter build() {
            return new ReportParameter(this);
        }
    }
}
