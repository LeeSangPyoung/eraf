package com.eraf.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErafUserDetails 테스트
 */
class ErafUserDetailsTest {

    @Test
    @DisplayName("기본 생성자로 생성")
    void shouldCreateWithConstructor() {
        Collection<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        ErafUserDetails userDetails = new ErafUserDetails("testuser", "", authorities, "user123");

        assertEquals("user123", userDetails.getUserId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals(2, userDetails.getAuthorities().size());
    }

    @Test
    @DisplayName("UserDetails 기본값 확인")
    void shouldHaveDefaultValues() {
        ErafUserDetails userDetails = new ErafUserDetails("user", "", List.of(), "user1");

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    @DisplayName("비밀번호는 빈 문자열")
    void shouldHaveEmptyPassword() {
        ErafUserDetails userDetails = new ErafUserDetails("user", "", List.of(), "user1");

        assertEquals("", userDetails.getPassword());
    }

    @Test
    @DisplayName("권한 목록 조회")
    void shouldReturnAuthorities() {
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_VIEWER"),
                new SimpleGrantedAuthority("ROLE_EDITOR")
        );

        ErafUserDetails userDetails = new ErafUserDetails("user", "", authorities, "user1");

        assertEquals(3, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EDITOR")));
    }

    @Test
    @DisplayName("빈 권한 목록 허용")
    void shouldAllowEmptyAuthorities() {
        ErafUserDetails userDetails = new ErafUserDetails("user", "", List.of(), "user1");

        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("userId와 username이 다를 수 있음")
    void shouldAllowDifferentUserIdAndUsername() {
        ErafUserDetails userDetails = new ErafUserDetails("john.doe@example.com", "", List.of(), "uuid-12345");

        assertEquals("uuid-12345", userDetails.getUserId());
        assertEquals("john.doe@example.com", userDetails.getUsername());
        assertNotEquals(userDetails.getUserId(), userDetails.getUsername());
    }

    @Test
    @DisplayName("특수 문자 포함 username 허용")
    void shouldAllowSpecialCharactersInUsername() {
        ErafUserDetails userDetails = new ErafUserDetails("user+test@example.com", "", List.of(), "user1");

        assertEquals("user+test@example.com", userDetails.getUsername());
    }

    @Test
    @DisplayName("Builder 패턴으로 생성")
    void shouldCreateWithBuilder() {
        ErafUserDetails userDetails = ErafUserDetails.erafBuilder()
                .username("testuser")
                .password("secret")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .userId("user123")
                .displayName("Test User")
                .email("test@example.com")
                .build();

        assertEquals("testuser", userDetails.getUsername());
        assertEquals("secret", userDetails.getPassword());
        assertEquals("user123", userDetails.getUserId());
        assertEquals("Test User", userDetails.getDisplayName());
        assertEquals("test@example.com", userDetails.getEmail());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    @DisplayName("6인자 생성자로 생성")
    void shouldCreateWithFullConstructor() {
        Collection<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        ErafUserDetails userDetails = new ErafUserDetails(
                "admin", "pass", authorities, "admin-id", "Admin User", "admin@example.com");

        assertEquals("admin", userDetails.getUsername());
        assertEquals("pass", userDetails.getPassword());
        assertEquals("admin-id", userDetails.getUserId());
        assertEquals("Admin User", userDetails.getDisplayName());
        assertEquals("admin@example.com", userDetails.getEmail());
    }
}
