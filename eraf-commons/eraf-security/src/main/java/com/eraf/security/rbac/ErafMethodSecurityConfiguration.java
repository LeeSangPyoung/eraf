package com.eraf.security.rbac;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * ERAF 메서드 레벨 시큐리티 설정
 * @PreAuthorize, @PostAuthorize, @Secured 어노테이션 지원
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @PreAuthorize("hasRole('ADMIN')")}
 * public void deleteUser(Long userId) {
 *     userRepository.deleteById(userId);
 * }
 *
 * {@code @PreAuthorize("hasPermission(#userId, 'USER', 'READ')")}
 * public User getUser(Long userId) {
 *     return userRepository.findById(userId);
 * }
 * </pre>
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,  // @PreAuthorize, @PostAuthorize 활성화
        securedEnabled = true,  // @Secured 활성화
        jsr250Enabled = true    // @RolesAllowed 활성화
)
public class ErafMethodSecurityConfiguration {

    /**
     * MethodSecurityExpressionHandler 빈 등록
     * hasPermission() 표현식 지원을 위해 PermissionEvaluator 주입
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            ErafPermissionEvaluator permissionEvaluator) {

        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}
