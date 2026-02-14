package com.eraf.jpa.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DataSource 라우팅 어노테이션
 *
 * 메서드나 클래스에 지정하여 특정 DataSource를 사용하도록 합니다.
 *
 * 예시:
 * <pre>
 * {@literal @}DataSourceRouting("readOnly")
 * public List&lt;User&gt; findAll() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceRouting {
    /**
     * DataSource 키
     */
    String value();
}
