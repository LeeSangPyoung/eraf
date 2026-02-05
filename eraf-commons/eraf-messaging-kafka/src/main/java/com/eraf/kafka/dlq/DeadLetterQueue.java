package com.eraf.kafka.dlq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DLQ(Dead Letter Queue) 설정
 *
 * 사용 예:
 * <pre>
 * @KafkaListener(topics = "orders")
 * @DeadLetterQueue(topic = "orders.dlq", maxRetries = 3)
 * public void processOrder(OrderEvent event) {
 *     // 처리 실패 시 orders.dlq로 이동
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeadLetterQueue {

    /**
     * DLQ 토픽 이름 (비어있으면 원본토픽.dlq)
     */
    String topic() default "";

    /**
     * 최대 재시도 횟수
     */
    int maxRetries() default 3;

    /**
     * 재시도 간격 (밀리초)
     */
    long retryDelay() default 1000;

    /**
     * 지수 백오프 사용 여부
     */
    boolean exponentialBackoff() default true;

    /**
     * 백오프 배율
     */
    double backoffMultiplier() default 2.0;

    /**
     * 최대 백오프 시간 (밀리초)
     */
    long maxBackoff() default 60000;

    /**
     * 재시도할 예외 클래스들 (비어있으면 모든 예외)
     */
    Class<? extends Throwable>[] retryOn() default {};

    /**
     * 재시도하지 않을 예외 클래스들
     */
    Class<? extends Throwable>[] noRetryOn() default {};
}
