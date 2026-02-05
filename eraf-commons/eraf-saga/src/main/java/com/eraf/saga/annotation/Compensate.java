package com.eraf.saga.annotation;

import java.lang.annotation.*;

/**
 * 보상 트랜잭션 메서드 어노테이션
 * SagaStep의 compensate 속성과 매핑됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Compensate {

    /**
     * 보상 대상 단계 이름 (SagaStep.name과 매칭)
     */
    String value() default "";
}
