package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Path Traversal 공격 방지 검증 구현
 */
public class NoPathTraversalValidator implements ConstraintValidator<NoPathTraversal, String> {

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "\\.\\.[\\\\/]|" +          // ../  ..\
                    "[\\\\/]\\.\\.[\\\\/]?|" +  // /..  /../  \..  \..\
                    "^\\.\\.[\\\\/]?|" +        // 시작이 ../
                    "%2e%2e[\\\\/]|" +          // URL 인코딩된 ../
                    "%252e%252e[\\\\/]",        // 이중 URL 인코딩
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !PATH_TRAVERSAL_PATTERN.matcher(value).find();
    }
}
