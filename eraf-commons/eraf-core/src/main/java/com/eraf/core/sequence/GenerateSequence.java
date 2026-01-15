package com.eraf.core.sequence;

import java.lang.annotation.*;

/**
 * 메서드 실행 전 파라미터 객체의 @Sequence 필드에 채번 값을 자동 생성
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateSequence {
}
