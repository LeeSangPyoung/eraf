package com.eraf.core.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 검증 예외
 * 필드별 오류 정보를 포함
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String field, String message) {
        super(message);
        this.fieldErrors = Collections.singletonMap(field, message);
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("유효성 검증에 실패했습니다.");
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> fieldErrors = new HashMap<>();
        private String message = "유효성 검증에 실패했습니다.";

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder addFieldError(String field, String message) {
            this.fieldErrors.put(field, message);
            return this;
        }

        public Builder addFieldErrors(Map<String, String> errors) {
            this.fieldErrors.putAll(errors);
            return this;
        }

        public ValidationException build() {
            return new ValidationException(message, fieldErrors);
        }

        public boolean hasErrors() {
            return !fieldErrors.isEmpty();
        }

        public void throwIfErrors() {
            if (hasErrors()) {
                throw build();
            }
        }
    }
}
