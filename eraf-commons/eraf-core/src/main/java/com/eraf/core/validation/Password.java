package com.eraf.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 비밀번호 복잡도 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

    String message() default "비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 최소 길이 (기본: 8)
     */
    int minLength() default 8;

    /**
     * 최대 길이 (기본: 20)
     */
    int maxLength() default 20;

    /**
     * 대문자 필수 여부
     */
    boolean requireUppercase() default true;

    /**
     * 소문자 필수 여부
     */
    boolean requireLowercase() default true;

    /**
     * 숫자 필수 여부
     */
    boolean requireDigit() default true;

    /**
     * 특수문자 필수 여부
     */
    boolean requireSpecial() default true;

    /**
     * 연속 문자 허용 개수 (0이면 검사 안 함)
     */
    int maxConsecutive() default 3;

    /**
     * 반복 문자 허용 개수 (0이면 검사 안 함)
     */
    int maxRepeated() default 3;
}
