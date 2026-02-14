package com.eraf.security.rbac;

import java.lang.annotation.*;

/**
 * RBAC 다중 권한 기반 접근 제어 어노테이션
 * 여러 권한 중 하나라도 가지고 있으면 접근 허용
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @HasAnyPermission({"USER:READ", "USER:LIST"})}
 * public List<User> getUsers() {
 *     // USER:READ 또는 USER:LIST 권한 중 하나라도 있으면 접근 가능
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasAnyPermission {

    /**
     * 필요한 권한 목록 (하나라도 일치하면 허용)
     *
     * @return 권한 코드 배열
     */
    String[] value();

    /**
     * 에러 메시지 (옵션)
     *
     * @return 에러 메시지
     */
    String message() default "해당 권한이 없습니다.";
}
