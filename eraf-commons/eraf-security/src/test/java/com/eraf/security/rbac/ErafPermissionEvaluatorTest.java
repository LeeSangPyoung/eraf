package com.eraf.security.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ErafPermissionEvaluator 단위 테스트
 */
class ErafPermissionEvaluatorTest {

    private RolePermissionRegistry registry;
    private ErafPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        registry = new RolePermissionRegistry();
        evaluator = new ErafPermissionEvaluator(registry);
    }

    @Test
    void hasPermission_ShouldReturnFalseWhenAuthenticationIsNull() {
        // when & then
        assertThat(evaluator.hasPermission(null, new Object(), "READ")).isFalse();
    }

    @Test
    void hasPermission_ShouldReturnFalseWhenPermissionIsNull() {
        // given
        Authentication auth = createAuthentication("user", List.of("ROLE_USER"));

        // when & then
        assertThat(evaluator.hasPermission(auth, new Object(), null)).isFalse();
    }

    @Test
    void hasPermission_ShouldGrantAccessForDirectPermission() {
        // given
        Authentication auth = createAuthentication("user", List.of("USER:READ"));

        // when & then
        assertThat(evaluator.hasPermission(auth, new Object(), "USER:READ")).isTrue();
    }

    @Test
    void hasPermission_ShouldGrantAccessViaRoleMapping() {
        // given
        registry.addRolePermissions("ADMIN", Set.of("USER:READ", "USER:WRITE"));
        Authentication auth = createAuthentication("admin", List.of("ROLE_ADMIN"));

        // when & then
        assertThat(evaluator.hasPermission(auth, new Object(), "USER:READ")).isTrue();
        assertThat(evaluator.hasPermission(auth, new Object(), "USER:WRITE")).isTrue();
    }

    @Test
    void hasPermission_ShouldDenyAccessWithoutPermission() {
        // given
        registry.addRolePermissions("USER", Set.of("PROFILE:READ"));
        Authentication auth = createAuthentication("user", List.of("ROLE_USER"));

        // when & then
        assertThat(evaluator.hasPermission(auth, new Object(), "USER:DELETE")).isFalse();
    }

    @Test
    void hasPermission_WithTargetTypeAndId_ShouldReturnFalseWhenTargetTypeIsNull() {
        // given
        Authentication auth = createAuthentication("user", List.of("ROLE_USER"));

        // when & then
        assertThat(evaluator.hasPermission(auth, 1L, null, "READ")).isFalse();
    }

    @Test
    void hasPermission_WithTargetTypeAndId_ShouldGrantAccessForDirectPermission() {
        // given
        Authentication auth = createAuthentication("user", List.of("USER:READ"));

        // when & then
        assertThat(evaluator.hasPermission(auth, 1L, "USER", "READ")).isTrue();
    }

    @Test
    void hasPermission_WithTargetTypeAndId_ShouldGrantAccessViaRoleMapping() {
        // given
        registry.addRolePermissions("ADMIN", Set.of("USER:DELETE"));
        Authentication auth = createAuthentication("admin", List.of("ROLE_ADMIN"));

        // when & then
        assertThat(evaluator.hasPermission(auth, 123L, "USER", "DELETE")).isTrue();
    }

    @Test
    void hasPermission_WithTargetTypeAndId_ShouldSupportWildcard() {
        // given
        registry.addPermission("ADMIN", "USER:*");
        Authentication auth = createAuthentication("admin", List.of("ROLE_ADMIN"));

        // when & then
        assertThat(evaluator.hasPermission(auth, 1L, "USER", "READ")).isTrue();
        assertThat(evaluator.hasPermission(auth, 1L, "USER", "WRITE")).isTrue();
    }

    @Test
    void hasPermission_ShouldHandleMultipleRoles() {
        // given
        registry.addPermission("ADMIN", "USER:DELETE");
        registry.addPermission("MANAGER", "ORDER:APPROVE");
        Authentication auth = createAuthentication("user", List.of("ROLE_ADMIN", "ROLE_MANAGER"));

        // when & then
        assertThat(evaluator.hasPermission(auth, new Object(), "USER:DELETE")).isTrue();
        assertThat(evaluator.hasPermission(auth, new Object(), "ORDER:APPROVE")).isTrue();
    }

    private Authentication createAuthentication(String username, List<String> authorities) {
        List<GrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new TestingAuthenticationToken(username, "password", grantedAuthorities);
    }
}
