package com.eraf.gateway.jwt;

import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.util.GatewayResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 토큰 검증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final List<String> excludePatterns;
    private final String headerName;

    public static final String JWT_CLAIMS_ATTRIBUTE = "ERAF_JWT_CLAIMS";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token == null) {
            response.setHeader("WWW-Authenticate", "Bearer");
            GatewayResponseUtils.sendError(response, GatewayErrorCode.JWT_MISSING);
            return;
        }

        try {
            JwtValidationResult result = jwtValidator.validate(token);

            if (!result.isValid()) {
                response.setHeader("WWW-Authenticate", "Bearer");
                GatewayErrorCode errorCode = mapToErrorCode(result.getErrorCode());
                GatewayResponseUtils.sendError(response, errorCode, result.getErrorMessage());
                return;
            }

            // 검증된 클레임을 request attribute에 저장
            request.setAttribute(JWT_CLAIMS_ATTRIBUTE, result.getClaims());

            // 클레임 정보를 헤더로 전달 (downstream 서비스용)
            if (result.getClaims() != null) {
                if (result.getClaims().containsKey("sub")) {
                    response.setHeader("X-User-Id", String.valueOf(result.getClaims().get("sub")));
                }
                if (result.getClaims().containsKey("roles")) {
                    response.setHeader("X-User-Roles", String.valueOf(result.getClaims().get("roles")));
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT validation error", e);
            response.setHeader("WWW-Authenticate", "Bearer");
            GatewayResponseUtils.sendError(response, GatewayErrorCode.JWT_INVALID);
        }
    }

    private GatewayErrorCode mapToErrorCode(String code) {
        return switch (code) {
            case "JWT_EXPIRED" -> GatewayErrorCode.JWT_EXPIRED;
            case "JWT_MALFORMED" -> GatewayErrorCode.JWT_MALFORMED;
            case "JWT_SIGNATURE_INVALID" -> GatewayErrorCode.JWT_SIGNATURE_INVALID;
            default -> GatewayErrorCode.JWT_INVALID;
        };
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(headerName);

        if (header != null) {
            if (header.startsWith("Bearer ")) {
                return header.substring(7);
            }
            return header;
        }

        // Cookie에서도 확인
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
