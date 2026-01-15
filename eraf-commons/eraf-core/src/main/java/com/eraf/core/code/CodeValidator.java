package com.eraf.core.code;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 공통코드 유효성 검증 구현
 */
public class CodeValidator implements ConstraintValidator<Code, String> {

    private String group;
    private boolean checkEnabled;

    @Autowired(required = false)
    private CodeRepository codeRepository;

    @Override
    public void initialize(Code constraintAnnotation) {
        this.group = constraintAnnotation.group();
        this.checkEnabled = constraintAnnotation.checkEnabled();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        if (codeRepository == null) {
            // CodeRepository가 없으면 검증 통과
            return true;
        }

        return codeRepository.existsByGroupAndCode(group, value, checkEnabled);
    }
}
