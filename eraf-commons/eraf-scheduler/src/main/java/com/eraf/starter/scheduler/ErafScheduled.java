package com.eraf.starter.scheduler;

import java.lang.annotation.*;

/**
 * ERAF 스케줄 작업 어노테이션
 * 선언적 스케줄 작업 정의
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ErafScheduled {

    /**
     * 작업 이름
     */
    String name();

    /**
     * Cron 표현식
     */
    String cron() default "";

    /**
     * 고정 지연 시간 (ms)
     */
    long fixedDelay() default -1;

    /**
     * 고정 실행 간격 (ms)
     */
    long fixedRate() default -1;

    /**
     * 초기 지연 시간 (ms)
     */
    long initialDelay() default 0;

    /**
     * 분산 락 활성화
     */
    boolean lockEnabled() default true;

    /**
     * 최대 락 유지 시간 (ISO-8601 duration 형식)
     */
    String lockAtMostFor() default "PT5M";

    /**
     * 최소 락 유지 시간 (ISO-8601 duration 형식)
     */
    String lockAtLeastFor() default "PT30S";

    /**
     * 작업 그룹
     */
    String group() default "default";

    /**
     * 작업 설명
     */
    String description() default "";

    /**
     * 활성화 여부 (SpEL 표현식 지원)
     */
    String enabled() default "true";
}
