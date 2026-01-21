package com.eraf.starter.statemachine;

import java.lang.annotation.*;

/**
 * 복수 상태 전이 컨테이너 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transitions {

    Transition[] value();
}
