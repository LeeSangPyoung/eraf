package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * SQL Injection 방지 검증 구현
 */
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('|\")|" +
                    "(--|#)|" +
                    "(;\\s*(drop|delete|truncate|update|insert|select|union|exec|execute))|" +
                    "\\b(or|and)\\b\\s*\\d+\\s*=\\s*\\d+|" +
                    "\\b(union\\s+select|select\\s+.*\\s+from|insert\\s+into|delete\\s+from|drop\\s+table|truncate\\s+table)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(value).find();
    }
}
