package com.eraf.starter.messaging;

import java.lang.annotation.*;

/**
 * 메시지 리스너 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageListener {

    /**
     * 토픽/큐 이름
     */
    String destination();

    /**
     * 컨슈머 그룹 (Kafka용)
     */
    String group() default "";

    /**
     * 동시 처리 수
     */
    int concurrency() default 1;

    /**
     * 에러 핸들러 빈 이름
     */
    String errorHandler() default "";
}
