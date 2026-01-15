package com.eraf.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 전화번호 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {

    String message() default "올바른 전화번호 형식이 아닙니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 허용 유형 (mobile: 휴대폰, landline: 유선전화, all: 모두)
     */
    PhoneType type() default PhoneType.ALL;

    enum PhoneType {
        MOBILE,
        LANDLINE,
        ALL
    }
}
