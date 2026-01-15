package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 전화번호 검증 구현
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    // 휴대폰: 010, 011, 016, 017, 018, 019로 시작
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$"
    );

    // 유선전화: 02, 031, 032, ... (지역번호)
    private static final Pattern LANDLINE_PATTERN = Pattern.compile(
            "^0[2-6][0-9]?-?[0-9]{3,4}-?[0-9]{4}$"
    );

    private Phone.PhoneType type;

    @Override
    public void initialize(Phone constraintAnnotation) {
        this.type = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String cleaned = value.replaceAll("[^0-9]", "");

        return switch (type) {
            case MOBILE -> MOBILE_PATTERN.matcher(cleaned).matches() ||
                    (cleaned.length() >= 10 && cleaned.length() <= 11 && cleaned.startsWith("01"));
            case LANDLINE -> LANDLINE_PATTERN.matcher(cleaned).matches() ||
                    (cleaned.length() >= 9 && cleaned.length() <= 11 && cleaned.startsWith("0") && !cleaned.startsWith("01"));
            case ALL -> MOBILE_PATTERN.matcher(cleaned).matches() ||
                    LANDLINE_PATTERN.matcher(cleaned).matches() ||
                    (cleaned.length() >= 9 && cleaned.length() <= 11 && cleaned.startsWith("0"));
        };
    }
}
