package com.eraf.core.config;

import java.lang.annotation.*;

/**
 * 기능 토글 어노테이션
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Feature {

    /**
     * 기능 이름
     */
    String value();

    /**
     * 기능이 비활성화된 경우 대체 동작 (SpEL 표현식)
     */
    String fallback() default "";
}
