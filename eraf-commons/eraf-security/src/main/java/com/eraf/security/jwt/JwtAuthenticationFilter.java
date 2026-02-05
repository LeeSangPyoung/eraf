package com.eraf.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 인증 필터
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> skipPatterns;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties properties) {
        this.tokenProvider = tokenProvider;
        this.properties = properties;
        this.skipPatterns = Arrays.asList(properties.getSkipPatterns());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip JWT validation for configured patterns
        if (shouldSkip(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for user: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(properties.getHeaderName());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenPrefix())) {
            return bearerToken.substring(properties.getTokenPrefix().length());
        }
        return null;
    }

    private boolean shouldSkip(String requestPath) {
        return skipPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }
}
