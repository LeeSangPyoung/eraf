package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 비밀번호 검증 구현
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecial;
    private int maxConsecutive;
    private int maxRepeated;

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

    @Override
    public void initialize(Password constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecial = constraintAnnotation.requireSpecial();
        this.maxConsecutive = constraintAnnotation.maxConsecutive();
        this.maxRepeated = constraintAnnotation.maxRepeated();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        // 길이 검사
        if (value.length() < minLength || value.length() > maxLength) {
            setMessage(context, "비밀번호는 " + minLength + "자 이상 " + maxLength + "자 이하여야 합니다");
            return false;
        }

        // 대문자 검사
        if (requireUppercase && !containsUppercase(value)) {
            setMessage(context, "비밀번호에 대문자가 포함되어야 합니다");
            return false;
        }

        // 소문자 검사
        if (requireLowercase && !containsLowercase(value)) {
            setMessage(context, "비밀번호에 소문자가 포함되어야 합니다");
            return false;
        }

        // 숫자 검사
        if (requireDigit && !containsDigit(value)) {
            setMessage(context, "비밀번호에 숫자가 포함되어야 합니다");
            return false;
        }

        // 특수문자 검사
        if (requireSpecial && !containsSpecial(value)) {
            setMessage(context, "비밀번호에 특수문자가 포함되어야 합니다");
            return false;
        }

        // 연속 문자 검사 (abc, 123)
        if (maxConsecutive > 0 && hasConsecutiveChars(value, maxConsecutive)) {
            setMessage(context, "비밀번호에 " + maxConsecutive + "자 이상 연속된 문자를 사용할 수 없습니다");
            return false;
        }

        // 반복 문자 검사 (aaa, 111)
        if (maxRepeated > 0 && hasRepeatedChars(value, maxRepeated)) {
            setMessage(context, "비밀번호에 같은 문자를 " + maxRepeated + "번 이상 반복할 수 없습니다");
            return false;
        }

        return true;
    }

    private void setMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean containsUppercase(String value) {
        return value.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLowercase(String value) {
        return value.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }

    private boolean containsSpecial(String value) {
        return value.chars().anyMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0);
    }

    private boolean hasConsecutiveChars(String value, int max) {
        int count = 1;
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1) + 1 ||
                    value.charAt(i) == value.charAt(i - 1) - 1) {
                count++;
                if (count >= max) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }

    private boolean hasRepeatedChars(String value, int max) {
        int count = 1;
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1)) {
                count++;
                if (count >= max) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }
}
