package com.eraf.security.jwt;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtAuthenticationFilter 테스트
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setHeaderName("Authorization");
        properties.setTokenPrefix("Bearer ");

        filter = new JwtAuthenticationFilter(tokenProvider, properties);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰으로 인증 성공")
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        Authentication authentication = mock(Authentication.class);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getAuthentication(token)).thenReturn(authentication);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("토큰 없이 요청 시 인증 없이 통과")
    void shouldPassWithoutToken() throws ServletException, IOException {
        // Given - 토큰 없음

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 인증 실패")
    void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Bearer 접두어 없는 토큰 무시")
    void shouldIgnoreTokenWithoutBearerPrefix() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "invalid.token");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("빈 토큰 무시")
    void shouldIgnoreEmptyToken() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "Bearer ");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("토큰 검증 중 예외 발생 시 필터 계속 진행")
    void shouldContinueFilterOnValidationException() throws ServletException, IOException {
        // Given
        String token = "problematic.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Validation error"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("커스텀 헤더명 지원")
    void shouldSupportCustomHeaderName() throws ServletException, IOException {
        // Given
        properties.setHeaderName("X-Auth-Token");
        properties.setTokenPrefix("");
        filter = new JwtAuthenticationFilter(tokenProvider, properties);

        String token = "custom.jwt.token";
        request.addHeader("X-Auth-Token", token);

        Authentication authentication = mock(Authentication.class);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getAuthentication(token)).thenReturn(authentication);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
