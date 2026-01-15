package com.eraf.core.sequence;

import java.lang.annotation.*;

/**
 * 채번 어노테이션
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sequence {

    /**
     * 채번 키 (미지정 시 필드명 사용)
     */
    String name() default "";

    /**
     * 접두사
     */
    String prefix() default "";

    /**
     * 리셋 정책
     */
    Reset reset() default Reset.NEVER;

    /**
     * 숫자 부분 자릿수 (0-padding)
     */
    int digits() default 5;

    /**
     * 날짜 구분자
     */
    String dateSeparator() default "-";

    /**
     * 날짜 포맷
     */
    String dateFormat() default "yyyyMMdd";
}
