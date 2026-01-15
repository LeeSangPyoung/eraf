package com.eraf.core.i18n;

import java.lang.annotation.*;

/**
 * 메시지 코드 어노테이션
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

    /**
     * 메시지 코드
     */
    String value();

    /**
     * 기본 메시지 (코드로 찾을 수 없을 때)
     */
    String defaultMessage() default "";
}
