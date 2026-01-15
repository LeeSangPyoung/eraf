package com.eraf.core.code;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 공통코드 유효성 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = CodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Code {

    String message() default "유효하지 않은 코드입니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 코드 그룹명
     */
    String group();

    /**
     * 사용 여부 검증
     */
    boolean checkEnabled() default true;
}
