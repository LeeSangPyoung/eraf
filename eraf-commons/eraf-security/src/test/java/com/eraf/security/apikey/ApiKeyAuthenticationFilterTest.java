package com.eraf.security.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ApiKeyAuthenticationFilter 테스트
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    private ApiKeyAuthenticationFilter filter;
    private ApiKeyProperties properties;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        properties = new ApiKeyProperties();
        properties.setHeaderName("X-API-KEY");

        Map<String, ApiKeyProperties.ApiKeyEntry> keys = new HashMap<>();

        ApiKeyProperties.ApiKeyEntry serviceA = new ApiKeyProperties.ApiKeyEntry();
        serviceA.setKey("api-key-123");
        serviceA.setName("Service A");
        serviceA.setRoles(Arrays.asList("ROLE_API", "ROLE_READ"));
        keys.put("service-a", serviceA);

        ApiKeyProperties.ApiKeyEntry restrictedService = new ApiKeyProperties.ApiKeyEntry();
        restrictedService.setKey("restricted-key");
        restrictedService.setName("Restricted Service");
        restrictedService.setRoles(List.of("ROLE_API"));
        restrictedService.setAllowedIps(List.of("10.0.0.1", "10.0.0.2"));
        restrictedService.setAllowedPatterns(List.of("/api/restricted/**"));
        keys.put("restricted", restrictedService);

        properties.setKeys(keys);

        filter = new ApiKeyAuthenticationFilter(properties);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 API Key로 인증 성공")
    void shouldAuthenticateWithValidApiKey() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "api-key-123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("Service A", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("API Key 없이 요청 시 인증 없이 통과")
    void shouldPassWithoutApiKey() throws ServletException, IOException {
        // Given - API Key 없음

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("잘못된 API Key는 인증 실패")
    void shouldNotAuthenticateWithInvalidApiKey() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "wrong-api-key");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("IP 제한이 있는 키의 허용된 IP에서 인증 성공")
    void shouldAuthenticateFromAllowedIp() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "restricted-key");
        request.setRemoteAddr("10.0.0.1");
        request.setRequestURI("/api/restricted/data");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("IP 제한이 있는 키의 허용되지 않은 IP에서 인증 실패")
    void shouldNotAuthenticateFromDisallowedIp() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "restricted-key");
        request.setRemoteAddr("192.168.1.1"); // 허용되지 않은 IP
        request.setRequestURI("/api/restricted/data");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("인증 시 올바른 역할 부여")
    void shouldGrantCorrectRoles() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "api-key-123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(2, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_API")));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_READ")));
    }

    @Test
    @DisplayName("커스텀 헤더명 지원")
    void shouldSupportCustomHeaderName() throws ServletException, IOException {
        // Given
        properties.setHeaderName("Authorization");
        filter = new ApiKeyAuthenticationFilter(properties);
        request.addHeader("Authorization", "api-key-123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("빈 API Key 무시")
    void shouldIgnoreEmptyApiKey() throws ServletException, IOException {
        // Given
        request.addHeader("X-API-KEY", "");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
