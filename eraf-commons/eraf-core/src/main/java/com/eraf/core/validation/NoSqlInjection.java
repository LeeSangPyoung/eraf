package com.eraf.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * SQL Injection 방지 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = NoSqlInjectionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSqlInjection {

    String message() default "허용되지 않는 문자가 포함되어 있습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
