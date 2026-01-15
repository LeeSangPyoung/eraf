package com.eraf.core.i18n;

import java.lang.annotation.*;

/**
 * ApiResponse의 메시지를 자동으로 다국어 처리
 * message 필드의 값을 메시지 코드로 간주하고 번역
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TranslateResponse {
}
