package com.eraf.starter.statemachine;

import java.lang.annotation.*;

/**
 * 상태 전이 정의 어노테이션
 * 메소드에 선언하여 상태 전이를 정의
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Transitions.class)
public @interface Transition {

    /**
     * 이벤트 이름
     */
    String event();

    /**
     * 소스 상태
     */
    String source();

    /**
     * 타겟 상태
     */
    String target();

    /**
     * 가드 조건 (SpEL 표현식)
     */
    String guard() default "";

    /**
     * 전이 설명
     */
    String description() default "";
}
