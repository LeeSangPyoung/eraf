package com.eraf.gateway.validation.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validation Result
 * 검증 결과를 담는 클래스
 */
public class ValidationResult {

    /**
     * 검증 성공 여부
     */
    private boolean success;

    /**
     * 전체 오류 메시지 목록
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 필드별 오류 메시지 맵
     * key: 필드명 (예: "name", "email")
     * value: 해당 필드의 오류 메시지 목록
     */
    private Map<String, List<String>> fieldErrors = new HashMap<>();

    public boolean isSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * 기본 생성자
     */
    public ValidationResult() {
        this.success = true;
    }

    /**
     * 생성자
     */
    public ValidationResult(boolean success, List<String> errors, Map<String, List<String>> fieldErrors) {
        this.success = success;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }

    /**
     * 검증 성공 결과 생성
     */
    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>(), new HashMap<>());
    }

    /**
     * 검증 실패 결과 생성
     */
    public static ValidationResult failure(String error) {
        return new ValidationResult(false, new ArrayList<>(List.of(error)), new HashMap<>());
    }

    /**
     * 검증 실패 결과 생성 (여러 오류)
     */
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, new ArrayList<>(errors), new HashMap<>());
    }

    /**
     * 필드별 검증 실패 결과 생성
     */
    public static ValidationResult fieldFailure(String field, String error) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        fieldErrors.put(field, new ArrayList<>(List.of(error)));
        return new ValidationResult(false, new ArrayList<>(List.of(error)), fieldErrors);
    }

    /**
     * 오류 추가
     */
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }

    /**
     * 필드 오류 추가
     */
    public void addFieldError(String field, String error) {
        this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
        this.errors.add(error);
        this.success = false;
    }

    /**
     * 다른 ValidationResult 병합
     */
    public void merge(ValidationResult other) {
        if (!other.isSuccess()) {
            this.success = false;
            this.errors.addAll(other.getErrors());
            other.getFieldErrors().forEach((field, otherFieldErrors) -> {
                this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(otherFieldErrors);
            });
        }
    }

    /**
     * 오류가 있는지 확인
     */
    public boolean hasErrors() {
        return !success;
    }

    /**
     * 특정 필드에 오류가 있는지 확인
     */
    public boolean hasFieldErrors(String field) {
        return fieldErrors.containsKey(field) && !fieldErrors.get(field).isEmpty();
    }
}
