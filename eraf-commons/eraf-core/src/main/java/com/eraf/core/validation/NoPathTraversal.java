package com.eraf.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Path Traversal 공격 방지 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = NoPathTraversalValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPathTraversal {

    String message() default "허용되지 않는 경로가 포함되어 있습니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
