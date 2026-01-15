package com.eraf.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 파일 확장자 검증 어노테이션
 */
@Documented
@Constraint(validatedBy = FileExtensionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileExtension {

    String message() default "허용되지 않는 파일 확장자입니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 허용되는 확장자 목록 (예: {"jpg", "png", "gif"})
     */
    String[] allowed() default {};

    /**
     * 금지되는 확장자 목록 (예: {"exe", "bat", "sh"})
     */
    String[] denied() default {"exe", "bat", "sh", "cmd", "com", "msi", "dll", "scr", "pif", "jar", "vbs", "js", "ps1"};

    /**
     * 대소문자 구분 여부
     */
    boolean caseSensitive() default false;
}
