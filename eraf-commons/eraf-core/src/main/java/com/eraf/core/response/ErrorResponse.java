package com.eraf.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 에러 응답 (RFC 7807 Problem Details 준수)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final String detail;
    private final String instance;
    private final Map<String, String> fieldErrors;
    private final Instant timestamp;

    private ErrorResponse(Builder builder) {
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.detail = builder.detail;
        this.instance = builder.instance;
        this.fieldErrors = builder.fieldErrors.isEmpty() ? null : Collections.unmodifiableMap(builder.fieldErrors);
        this.timestamp = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static class Builder {
        private int status;
        private String code;
        private String message;
        private String detail;
        private String instance;
        private Map<String, String> fieldErrors = new HashMap<>();

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder fieldError(String field, String error) {
            this.fieldErrors.put(field, error);
            return this;
        }

        public Builder fieldErrors(Map<String, String> fieldErrors) {
            if (fieldErrors != null) {
                this.fieldErrors.putAll(fieldErrors);
            }
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
