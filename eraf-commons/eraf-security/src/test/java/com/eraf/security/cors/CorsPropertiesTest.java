package com.eraf.security.cors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CorsProperties 테스트
 */
class CorsPropertiesTest {

    private CorsProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CorsProperties();
    }

    @Test
    @DisplayName("기본값 확인")
    void shouldHaveDefaultValues() {
        assertTrue(properties.getAllowedOrigins().isEmpty());
        assertTrue(properties.getAllowedOriginPatterns().isEmpty());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"), properties.getAllowedMethods());
        assertEquals(List.of("*"), properties.getAllowedHeaders());
        assertFalse(properties.isAllowCredentials());
        assertEquals(3600L, properties.getMaxAge());
        assertTrue(properties.isEnabled());
        assertEquals("/**", properties.getPathPattern());
    }

    @Test
    @DisplayName("허용 Origin 설정")
    void shouldSetAllowedOrigins() {
        properties.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://example.com"));

        assertEquals(2, properties.getAllowedOrigins().size());
        assertTrue(properties.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(properties.getAllowedOrigins().contains("https://example.com"));
    }

    @Test
    @DisplayName("허용 메서드 설정")
    void shouldSetAllowedMethods() {
        properties.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));

        assertEquals(4, properties.getAllowedMethods().size());
        assertTrue(properties.getAllowedMethods().contains("GET"));
        assertTrue(properties.getAllowedMethods().contains("POST"));
        assertTrue(properties.getAllowedMethods().contains("PUT"));
        assertTrue(properties.getAllowedMethods().contains("DELETE"));
    }

    @Test
    @DisplayName("허용 헤더 설정")
    void shouldSetAllowedHeaders() {
        properties.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Custom-Header"));

        assertEquals(3, properties.getAllowedHeaders().size());
        assertTrue(properties.getAllowedHeaders().contains("Authorization"));
        assertTrue(properties.getAllowedHeaders().contains("Content-Type"));
        assertTrue(properties.getAllowedHeaders().contains("X-Custom-Header"));
    }

    @Test
    @DisplayName("노출 헤더 설정")
    void shouldSetExposedHeaders() {
        properties.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Number"));

        assertEquals(2, properties.getExposedHeaders().size());
        assertTrue(properties.getExposedHeaders().contains("X-Total-Count"));
        assertTrue(properties.getExposedHeaders().contains("X-Page-Number"));
    }

    @Test
    @DisplayName("자격 증명 허용 여부 설정")
    void shouldSetAllowCredentials() {
        properties.setAllowCredentials(false);

        assertFalse(properties.isAllowCredentials());
    }

    @Test
    @DisplayName("최대 캐시 시간 설정")
    void shouldSetMaxAge() {
        properties.setMaxAge(7200L);

        assertEquals(7200L, properties.getMaxAge());
    }

    @Test
    @DisplayName("모든 설정 한번에 변경")
    void shouldAllowAllConfigurationsToBeChanged() {
        properties.setAllowedOrigins(List.of("https://app.example.com"));
        properties.setAllowedMethods(List.of("GET", "POST"));
        properties.setAllowedHeaders(List.of("Authorization"));
        properties.setExposedHeaders(List.of("X-Request-Id"));
        properties.setAllowCredentials(true);
        properties.setMaxAge(1800L);

        assertEquals(1, properties.getAllowedOrigins().size());
        assertEquals("https://app.example.com", properties.getAllowedOrigins().get(0));
        assertEquals(2, properties.getAllowedMethods().size());
        assertEquals(1, properties.getAllowedHeaders().size());
        assertEquals(1, properties.getExposedHeaders().size());
        assertTrue(properties.isAllowCredentials());
        assertEquals(1800L, properties.getMaxAge());
    }

    @Test
    @DisplayName("빈 Origin 목록 허용")
    void shouldAllowEmptyOrigins() {
        properties.setAllowedOrigins(List.of());

        assertTrue(properties.getAllowedOrigins().isEmpty());
    }

    @Test
    @DisplayName("와일드카드 패턴 지원")
    void shouldSupportWildcardPatterns() {
        properties.setAllowedOrigins(List.of("https://*.example.com"));

        assertEquals("https://*.example.com", properties.getAllowedOrigins().get(0));
    }

    @Test
    @DisplayName("경로 패턴 설정")
    void shouldSetPathPattern() {
        properties.setPathPattern("/api/**");

        assertEquals("/api/**", properties.getPathPattern());
    }
}
