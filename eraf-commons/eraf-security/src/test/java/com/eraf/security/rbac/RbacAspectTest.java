package com.eraf.security.rbac;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * RbacAspect 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class RbacAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    private RolePermissionRegistry registry;
    private ErafPermissionEvaluator evaluator;
    private RbacAspect aspect;

    @BeforeEach
    void setUp() {
        registry = new RolePermissionRegistry();
        evaluator = new ErafPermissionEvaluator(registry);
        aspect = new RbacAspect(evaluator);

        // Mock JoinPoint (lenient: not all tests use joinPoint)
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.toShortString()).thenReturn("TestController.testMethod()");
    }

    @Test
    void checkHasRole_ShouldPassWhenUserHasRole() {
        // given
        setAuthentication("user", "ROLE_ADMIN");
        HasRole annotation = createHasRoleAnnotation("ADMIN");

        // when & then
        assertThatCode(() -> aspect.checkHasRole(joinPoint, annotation))
                .doesNotThrowAnyException();
    }

    @Test
    void checkHasRole_ShouldThrowExceptionWhenUserDoesNotHaveRole() {
        // given
        setAuthentication("user", "ROLE_USER");
        HasRole annotation = createHasRoleAnnotation("ADMIN");

        // when & then
        assertThatThrownBy(() -> aspect.checkHasRole(joinPoint, annotation))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    void checkHasAnyRole_ShouldPassWhenUserHasAnyRole() {
        // given
        setAuthentication("user", "ROLE_USER");
        HasAnyRole annotation = createHasAnyRoleAnnotation("ADMIN", "USER");

        // when & then
        assertThatCode(() -> aspect.checkHasAnyRole(joinPoint, annotation))
                .doesNotThrowAnyException();
    }

    @Test
    void checkHasAnyRole_ShouldThrowExceptionWhenUserHasNoMatchingRole() {
        // given
        setAuthentication("user", "ROLE_GUEST");
        HasAnyRole annotation = createHasAnyRoleAnnotation("ADMIN", "USER");

        // when & then
        assertThatThrownBy(() -> aspect.checkHasAnyRole(joinPoint, annotation))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkHasPermission_ShouldPassWhenUserHasPermission() {
        // given
        setAuthentication("user", "USER:READ");
        HasPermission annotation = createHasPermissionAnnotation("USER:READ");

        // when & then
        assertThatCode(() -> aspect.checkHasPermission(joinPoint, annotation))
                .doesNotThrowAnyException();
    }

    @Test
    void checkHasPermission_ShouldPassWhenUserHasRoleWithPermission() {
        // given
        registry.addPermission("ADMIN", "USER:DELETE");
        setAuthentication("user", "ROLE_ADMIN");
        HasPermission annotation = createHasPermissionAnnotation("USER:DELETE");

        // Mock getThis() for PermissionEvaluator
        when(joinPoint.getThis()).thenReturn(new Object());

        // when & then
        assertThatCode(() -> aspect.checkHasPermission(joinPoint, annotation))
                .doesNotThrowAnyException();
    }

    @Test
    void checkHasPermission_ShouldThrowExceptionWhenUserDoesNotHavePermission() {
        // given
        setAuthentication("user", "USER:READ");
        HasPermission annotation = createHasPermissionAnnotation("USER:DELETE");

        when(joinPoint.getThis()).thenReturn(new Object());

        // when & then
        assertThatThrownBy(() -> aspect.checkHasPermission(joinPoint, annotation))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkHasAnyPermission_ShouldPassWhenUserHasAnyPermission() {
        // given
        setAuthentication("user", "USER:READ");
        HasAnyPermission annotation = createHasAnyPermissionAnnotation("USER:READ", "USER:WRITE");

        // when & then
        assertThatCode(() -> aspect.checkHasAnyPermission(joinPoint, annotation))
                .doesNotThrowAnyException();
    }

    @Test
    void checkHasAnyPermission_ShouldThrowExceptionWhenUserHasNoMatchingPermission() {
        // given
        setAuthentication("user", "PROFILE:READ");
        HasAnyPermission annotation = createHasAnyPermissionAnnotation("USER:WRITE", "USER:DELETE");

        // when & then
        assertThatThrownBy(() -> aspect.checkHasAnyPermission(joinPoint, annotation))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkHasRole_ShouldThrowExceptionWhenNotAuthenticated() {
        // given
        SecurityContextHolder.clearContext();
        HasRole annotation = createHasRoleAnnotation("ADMIN");

        // when & then
        assertThatThrownBy(() -> aspect.checkHasRole(joinPoint, annotation))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("인증이 필요합니다.");
    }

    // Helper methods

    private void setAuthentication(String username, String... authorities) {
        Authentication auth = new TestingAuthenticationToken(
                username,
                "password",
                authorities
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private HasRole createHasRoleAnnotation(String role) {
        return new HasRole() {
            @Override
            public String value() {
                return role;
            }

            @Override
            public String message() {
                return "접근 권한이 없습니다.";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasRole.class;
            }
        };
    }

    private HasAnyRole createHasAnyRoleAnnotation(String... roles) {
        return new HasAnyRole() {
            @Override
            public String[] value() {
                return roles;
            }

            @Override
            public String message() {
                return "접근 권한이 없습니다.";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasAnyRole.class;
            }
        };
    }

    private HasPermission createHasPermissionAnnotation(String permission) {
        return new HasPermission() {
            @Override
            public String value() {
                return permission;
            }

            @Override
            public String resourceType() {
                return "";
            }

            @Override
            public String message() {
                return "해당 권한이 없습니다.";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasPermission.class;
            }
        };
    }

    private HasAnyPermission createHasAnyPermissionAnnotation(String... permissions) {
        return new HasAnyPermission() {
            @Override
            public String[] value() {
                return permissions;
            }

            @Override
            public String message() {
                return "해당 권한이 없습니다.";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return HasAnyPermission.class;
            }
        };
    }
}
