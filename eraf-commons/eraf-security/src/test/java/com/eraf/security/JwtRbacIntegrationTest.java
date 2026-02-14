package com.eraf.security;

import com.eraf.security.jwt.ErafUserDetails;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import com.eraf.security.rbac.ErafPermissionEvaluator;
import com.eraf.security.rbac.RolePermissionRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT + RBAC 통합 테스트
 * JWT 토큰 생성/검증과 RBAC 권한 체크의 통합 시나리오 검증
 */
class JwtRbacIntegrationTest {

    private JwtTokenProvider jwtTokenProvider;
    private ErafPermissionEvaluator permissionEvaluator;
    private RolePermissionRegistry permissionRegistry;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        // JWT Properties 설정
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm");
        jwtProperties.setIssuer("eraf-test");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(30));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));

        // JWT Provider 생성
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        // RBAC 설정
        permissionRegistry = new RolePermissionRegistry();
        permissionRegistry.addRolePermissions("ADMIN", Set.of("user:read", "user:write", "user:delete"));
        permissionRegistry.addRolePermissions("USER", Set.of("user:read"));
        permissionRegistry.addRolePermissions("MANAGER", Set.of("user:read", "user:write"));

        permissionEvaluator = new ErafPermissionEvaluator(permissionRegistry);
    }

    @Test
    void jwt_ShouldCreateAndValidateAccessToken() {
        // given
        Authentication auth = createAuthentication("john", "user123", List.of("ROLE_USER"));

        // when
        String token = jwtTokenProvider.createAccessToken(auth);

        // then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT 형식: header.payload.signature

        // 토큰 검증
        Authentication parsedAuth = jwtTokenProvider.getAuthentication(token);
        assertThat(parsedAuth.getName()).isEqualTo("john");
        assertThat(parsedAuth.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void jwt_ShouldCreateTokenWithMultipleRoles() {
        // given
        Authentication auth = createAuthentication("admin", "admin123",
                List.of("ROLE_ADMIN", "ROLE_USER"));

        // when
        String token = jwtTokenProvider.createAccessToken(auth);
        Authentication parsedAuth = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(parsedAuth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void jwt_ShouldIncludeUserIdInToken() {
        // given - ErafUserDetails(username, password, authorities, userId)
        ErafUserDetails userDetails = new ErafUserDetails(
                "john", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "user123"
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // when
        String token = jwtTokenProvider.createAccessToken(auth);

        // then - 토큰에서 userId 추출
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("userId")).isEqualTo("user123");
    }

    @Test
    void jwt_TokenShouldExpire() throws InterruptedException {
        // given - 1초 만료 설정
        jwtProperties.setAccessTokenExpiration(Duration.ofSeconds(1));
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(jwtProperties);
        Authentication auth = createAuthentication("john", "user123", List.of("ROLE_USER"));

        // when
        String token = shortLivedProvider.createAccessToken(auth);
        Thread.sleep(1500); // 1.5초 대기

        // then - 만료된 토큰은 검증 실패 (validateToken returns false)
        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }

    @Test
    void jwt_RefreshTokenShouldHaveLongerExpiration() {
        // given
        Authentication auth = createAuthentication("john", "user123", List.of("ROLE_USER"));

        // when
        String accessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // then - 두 토큰 모두 유효하지만 만료 시간이 다름
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();

        // 토큰 내용 확인
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Claims accessClaims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(accessToken).getPayload();
        Claims refreshClaims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(refreshToken).getPayload();

        // Refresh 토큰이 더 긴 만료 시간을 가짐
        assertThat(refreshClaims.getExpiration())
                .isAfter(accessClaims.getExpiration());
    }

    @Test
    void rbac_AdminShouldHaveAllPermissions() {
        // given - ROLE_ADMIN -> evaluator strips "ROLE_" -> looks up "ADMIN" in registry
        Authentication auth = createAuthentication("admin", "admin123", List.of("ROLE_ADMIN"));

        // when & then
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:read")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:write")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:delete")).isTrue();
    }

    @Test
    void rbac_UserShouldHaveOnlyReadPermission() {
        // given
        Authentication auth = createAuthentication("user", "user123", List.of("ROLE_USER"));

        // when & then
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:read")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:write")).isFalse();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:delete")).isFalse();
    }

    @Test
    void rbac_ManagerShouldHaveReadAndWrite() {
        // given
        Authentication auth = createAuthentication("manager", "mgr123", List.of("ROLE_MANAGER"));

        // when & then
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:read")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:write")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:delete")).isFalse();
    }

    @Test
    void rbac_MultipleRolesShouldCombinePermissions() {
        // given - USER + MANAGER 역할을 모두 가진 사용자
        Authentication auth = createAuthentication("poweruser", "pu123",
                List.of("ROLE_USER", "ROLE_MANAGER"));

        // when & then - 두 역할의 권한을 모두 가짐
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:read")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:write")).isTrue();
    }

    @Test
    void integration_JwtTokenWithRbacCheck() {
        // given - JWT 토큰 생성
        Authentication originalAuth = createAuthentication("admin", "admin123", List.of("ROLE_ADMIN"));
        String token = jwtTokenProvider.createAccessToken(originalAuth);

        // when - 토큰에서 인증 정보 복원
        Authentication restoredAuth = jwtTokenProvider.getAuthentication(token);

        // then - 복원된 인증 정보로 RBAC 체크 가능
        assertThat(permissionEvaluator.hasPermission(restoredAuth, null, "user:delete")).isTrue();
    }

    @Test
    void integration_TokenCreationAndPermissionCheckFlow() {
        // 1. 사용자 로그인 (JWT 발급) - ErafUserDetails(username, password, authorities, userId)
        ErafUserDetails user = new ErafUserDetails(
                "john", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                "user123"
        );
        Authentication loginAuth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String accessToken = jwtTokenProvider.createAccessToken(loginAuth);

        // 2. 토큰 검증
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();

        // 3. 토큰에서 인증 정보 추출
        Authentication auth = jwtTokenProvider.getAuthentication(accessToken);

        // 4. RBAC 권한 체크
        boolean canDelete = permissionEvaluator.hasPermission(auth, null, "user:delete");
        assertThat(canDelete).isTrue();

        // 5. 권한이 없는 작업은 거부
        boolean canUnknown = permissionEvaluator.hasPermission(auth, null, "unknown:action");
        assertThat(canUnknown).isFalse();
    }

    @Test
    void integration_InvalidTokenShouldFailValidation() {
        // given - 잘못된 토큰
        String invalidToken = "invalid.jwt.token";

        // when & then - validateToken returns false for invalid tokens
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    void integration_TamperedTokenShouldBeDetected() {
        // given - 정상 토큰 생성
        Authentication auth = createAuthentication("user", "user123", List.of("ROLE_USER"));
        String validToken = jwtTokenProvider.createAccessToken(auth);

        // when - 토큰 변조 (마지막 문자 변경)
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // then - 변조된 토큰은 검증 실패
        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void rbac_UnregisteredRoleShouldHaveNoPermissions() {
        // given - 등록되지 않은 역할
        Authentication auth = createAuthentication("guest", "guest123", List.of("ROLE_GUEST"));

        // when & then - 모든 권한 체크 실패
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:read")).isFalse();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user:write")).isFalse();
    }

    @Test
    void rbac_PermissionRegistryShouldBeExtensible() {
        // given - 새 역할 추가
        permissionRegistry.addRolePermissions("SUPERUSER",
                Set.of("user:read", "user:write", "user:delete", "system:admin"));
        Authentication auth = createAuthentication("super", "super123", List.of("ROLE_SUPERUSER"));

        // when & then
        assertThat(permissionEvaluator.hasPermission(auth, null, "system:admin")).isTrue();
    }

    @Test
    void integration_RefreshTokenShouldContainSubject() {
        // given
        Authentication auth = createAuthentication("john", "user123",
                List.of("ROLE_USER", "ROLE_ADMIN"));

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // then - Refresh 토큰에서 subject 확인
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(refreshToken).getPayload();

        assertThat(claims.getSubject()).isEqualTo("john");
    }

    // Helper Methods

    private Authentication createAuthentication(String username, String userId, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        // ErafUserDetails(username, password, authorities, userId)
        ErafUserDetails userDetails = new ErafUserDetails(username, "password", authorities, userId);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
