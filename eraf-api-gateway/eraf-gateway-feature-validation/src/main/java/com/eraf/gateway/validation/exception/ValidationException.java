package com.eraf.gateway.validation.exception;

import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.exception.GatewayException;
import com.eraf.gateway.validation.domain.ValidationResult;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Validation Exception
 * 검증 실패 예외
 */
@Getter
public class ValidationException extends GatewayException {

    private final ValidationResult validationResult;

    public ValidationException(ValidationResult validationResult) {
        super(GatewayErrorCode.BAD_REQUEST);
        this.validationResult = validationResult;
    }

    public ValidationException(String message) {
        super(GatewayErrorCode.BAD_REQUEST, message);
        this.validationResult = ValidationResult.failure(message);
    }

    public ValidationException(List<String> errors) {
        super(GatewayErrorCode.BAD_REQUEST);
        this.validationResult = ValidationResult.failure(errors);
    }

    /**
     * 전체 오류 메시지 조회
     */
    public List<String> getErrors() {
        return validationResult.getErrors();
    }

    /**
     * 필드별 오류 메시지 조회
     */
    public Map<String, List<String>> getFieldErrors() {
        return validationResult.getFieldErrors();
    }

    /**
     * 검증 결과 조회
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * 오류 메시지 문자열 생성
     */
    @Override
    public String getMessage() {
        if (validationResult.getErrors().isEmpty()) {
            return "Validation failed";
        }

        if (validationResult.getErrors().size() == 1) {
            return validationResult.getErrors().get(0);
        }

        return "Validation failed with " + validationResult.getErrors().size() + " errors: " +
                String.join(", ", validationResult.getErrors());
    }

    /**
     * ValidationResult로부터 예외 생성
     */
    public static ValidationException from(ValidationResult result) {
        return new ValidationException(result);
    }
}
