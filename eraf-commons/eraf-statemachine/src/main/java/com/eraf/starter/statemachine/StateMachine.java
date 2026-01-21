package com.eraf.starter.statemachine;

import java.lang.annotation.*;

/**
 * 상태 머신 정의 어노테이션
 * 클래스에 선언하여 상태 머신을 정의
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StateMachine {

    /**
     * 상태 머신 ID
     */
    String id();

    /**
     * 초기 상태
     */
    String initialState();

    /**
     * 상태 목록
     */
    String[] states();

    /**
     * 종료 상태 목록
     */
    String[] endStates() default {};

    /**
     * 상태 머신 설명
     */
    String description() default "";
}
