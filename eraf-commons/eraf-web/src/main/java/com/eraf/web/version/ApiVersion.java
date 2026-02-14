package com.eraf.web.version;

import java.lang.annotation.*;

/**
 * API 버전 지정 어노테이션
 *
 * <p>사용 예시:
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/users")
 * public class UserController {
 *
 *     @ApiVersion("1")
 *     @GetMapping
 *     public List<User> getUsersV1() {
 *         // v1 implementation
 *     }
 *
 *     @ApiVersion("2")
 *     @GetMapping
 *     public List<UserV2> getUsersV2() {
 *         // v2 implementation
 *     }
 * }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * API 버전
     * 예: "1", "2", "1.0", "2.1"
     */
    String value();

    /**
     * 버전 매칭 방식
     */
    VersionStrategy strategy() default VersionStrategy.URI;

    /**
     * Deprecated 여부
     */
    boolean deprecated() default false;

    /**
     * Deprecated 메시지
     */
    String deprecatedMessage() default "";

    /**
     * 대체 버전 (deprecated된 경우)
     */
    String replacedBy() default "";
}
