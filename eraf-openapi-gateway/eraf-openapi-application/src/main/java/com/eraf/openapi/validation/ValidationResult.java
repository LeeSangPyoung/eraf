package com.eraf.openapi.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * API Request Validation 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /**
     * 검증 성공 여부
     */
    private boolean valid;

    /**
     * 실패한 검증 항목들
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 검증 실패 타입
     */
    private ValidationType failureType;

    /**
     * 상세 메시지
     */
    private String message;

    /**
     * 성공 결과 생성
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * 실패 결과 생성
     */
    public static ValidationResult failure(ValidationType type, String message) {
        return ValidationResult.builder()
                .valid(false)
                .failureType(type)
                .message(message)
                .build();
    }

    /**
     * 에러 추가
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    /**
     * Validation 실패 타입
     */
    public enum ValidationType {
        MISSING_HEADER,          // 필수 헤더 누락
        MISSING_QUERY_PARAM,     // 필수 쿼리 파라미터 누락
        INVALID_CONTENT_TYPE,    // 허용되지 않은 Content-Type
        BODY_TOO_LARGE,          // Body 크기 초과
        MISSING_REQUIRED_FIELD,  // 필수 필드 누락
        JSON_SCHEMA_VIOLATION,   // JSON 스키마 위반
        INVALID_JSON            // 잘못된 JSON 형식
    }
}
