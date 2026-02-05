package com.eraf.security.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API Key 인증 필터
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private final ApiKeyProperties properties;
    private final Map<String, ApiKeyProperties.ApiKeyEntry> keyMap;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> applyPatterns;
    private final List<String> skipPatterns;

    public ApiKeyAuthenticationFilter(ApiKeyProperties properties) {
        this.properties = properties;
        this.keyMap = properties.getKeys().stream()
                .filter(ApiKeyProperties.ApiKeyEntry::isEnabled)
                .collect(Collectors.toMap(ApiKeyProperties.ApiKeyEntry::getKey, e -> e));
        this.applyPatterns = Arrays.asList(properties.getApplyPatterns());
        this.skipPatterns = Arrays.asList(properties.getSkipPatterns());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip patterns
        if (shouldSkip(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if path matches apply patterns
        if (!shouldApply(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = resolveApiKey(request);

        if (!StringUtils.hasText(apiKey)) {
            log.debug("No API key found in request");
            filterChain.doFilter(request, response);
            return;
        }

        ApiKeyProperties.ApiKeyEntry keyEntry = keyMap.get(apiKey);

        if (keyEntry == null) {
            log.warn("Invalid API key attempt from IP: {}", getClientIp(request));
            filterChain.doFilter(request, response);
            return;
        }

        // Validate IP if configured
        if (!keyEntry.getAllowedIps().isEmpty()) {
            String clientIp = getClientIp(request);
            if (!isIpAllowed(clientIp, keyEntry.getAllowedIps())) {
                log.warn("API key '{}' not allowed from IP: {}", keyEntry.getName(), clientIp);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Validate URL pattern if configured
        if (!keyEntry.getAllowedPatterns().isEmpty()) {
            if (!isPatternAllowed(requestPath, keyEntry.getAllowedPatterns())) {
                log.warn("API key '{}' not allowed for path: {}", keyEntry.getName(), requestPath);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Create authentication token
        List<SimpleGrantedAuthority> authorities = keyEntry.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(Collectors.toList());

        ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(apiKey, keyEntry.getName(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("API key authentication successful for client: {}", keyEntry.getName());

        filterChain.doFilter(request, response);
    }

    private String resolveApiKey(HttpServletRequest request) {
        // Check header first
        String apiKey = request.getHeader(properties.getHeaderName());
        if (StringUtils.hasText(apiKey)) {
            return apiKey;
        }

        // Fallback to query parameter
        return request.getParameter(properties.getQueryParamName());
    }

    private boolean shouldApply(String requestPath) {
        return applyPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    private boolean shouldSkip(String requestPath) {
        return skipPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    private boolean isIpAllowed(String clientIp, List<String> allowedIps) {
        return allowedIps.stream()
                .anyMatch(allowed -> allowed.equals(clientIp) || matchesCidr(clientIp, allowed));
    }

    private boolean isPatternAllowed(String requestPath, List<String> allowedPatterns) {
        return allowedPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    private boolean matchesCidr(String ip, String cidr) {
        // Simple CIDR matching (supports /24 notation)
        if (!cidr.contains("/")) {
            return ip.equals(cidr);
        }
        try {
            String[] parts = cidr.split("/");
            String baseIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            String[] ipParts = ip.split("\\.");
            String[] baseParts = baseIp.split("\\.");

            int fullBytes = prefixLength / 8;
            for (int i = 0; i < fullBytes; i++) {
                if (!ipParts[i].equals(baseParts[i])) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return ip.equals(cidr);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
