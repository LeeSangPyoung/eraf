package com.eraf.gateway.oauth2.filter;

import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import com.eraf.gateway.oauth2.config.OAuth2Properties;
import com.eraf.gateway.oauth2.domain.OAuth2Token;
import com.eraf.gateway.oauth2.exception.InsufficientScopeException;
import com.eraf.gateway.oauth2.exception.OAuth2ErrorCode;
import com.eraf.gateway.oauth2.exception.OAuth2Exception;
import com.eraf.gateway.oauth2.service.OAuth2Service;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * OAuth2 인증 필터
 * Access Token을 검증하고 사용자 정보를 설정합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2Filter extends OncePerRequestFilter {

    private final OAuth2Service oauth2Service;
    private final OAuth2Properties properties;

    public static final String OAUTH2_TOKEN_ATTRIBUTE = "ERAF_OAUTH2_TOKEN";
    public static final String OAUTH2_USER_ID_ATTRIBUTE = "ERAF_OAUTH2_USER_ID";
    public static final String OAUTH2_CLIENT_ID_ATTRIBUTE = "ERAF_OAUTH2_CLIENT_ID";
    public static final String OAUTH2_SCOPES_ATTRIBUTE = "ERAF_OAUTH2_SCOPES";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 제외 패턴 확인
        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token 추출
        String token = extractToken(request);

        if (token == null) {
            response.setHeader("WWW-Authenticate", "Bearer");
            GatewayResponseUtils.sendError(response, OAuth2ErrorCode.OAUTH2_TOKEN_MISSING);
            return;
        }

        try {
            // Token 검증
            OAuth2Token oauthToken = oauth2Service.validateToken(token);

            // 스코프 검증
            if (properties.isValidateScopes() && !properties.getRequiredScopes().isEmpty()) {
                oauth2Service.validateScope(properties.getRequiredScopes(), oauthToken.getScopes());
            }

            // 검증된 토큰 정보를 request attribute에 저장
            request.setAttribute(OAUTH2_TOKEN_ATTRIBUTE, oauthToken);
            request.setAttribute(OAUTH2_USER_ID_ATTRIBUTE, oauthToken.getUserId());
            request.setAttribute(OAUTH2_CLIENT_ID_ATTRIBUTE, oauthToken.getClientId());
            request.setAttribute(OAUTH2_SCOPES_ATTRIBUTE, oauthToken.getScopes());

            // 클레임 정보를 헤더로 전달 (downstream 서비스용)
            if (properties.isPropagateClaims()) {
                response.setHeader("X-OAuth2-User-Id", oauthToken.getUserId());
                response.setHeader("X-OAuth2-Client-Id", oauthToken.getClientId());
                if (oauthToken.getScopes() != null && !oauthToken.getScopes().isEmpty()) {
                    response.setHeader("X-OAuth2-Scopes", String.join(",", oauthToken.getScopes()));
                }
            }

            log.debug("OAuth2 authentication successful for user: {}, client: {}",
                    oauthToken.getUserId(), oauthToken.getClientId());

            filterChain.doFilter(request, response);

        } catch (InsufficientScopeException e) {
            log.warn("Insufficient scope: {}", e.getMessage());
            response.setHeader("WWW-Authenticate",
                    String.format("Bearer error=\"insufficient_scope\", scope=\"%s\"",
                            String.join(" ", e.getRequiredScopes())));
            GatewayResponseUtils.sendError(response, OAuth2ErrorCode.OAUTH2_INSUFFICIENT_SCOPE,
                    "Required scopes: " + String.join(", ", e.getRequiredScopes()));

        } catch (OAuth2Exception e) {
            log.warn("OAuth2 authentication failed: {}", e.getMessage());
            response.setHeader("WWW-Authenticate",
                    String.format("Bearer error=\"%s\"", getErrorType(e.getErrorCode())));
            GatewayResponseUtils.sendError(response, e.getErrorCode(), e.getMessage());

        } catch (Exception e) {
            log.error("OAuth2 authentication error", e);
            response.setHeader("WWW-Authenticate", "Bearer error=\"server_error\"");
            GatewayResponseUtils.sendError(response, OAuth2ErrorCode.OAUTH2_SERVER_ERROR);
        }
    }

    /**
     * Request에서 Token 추출
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 추출
        String header = request.getHeader(properties.getTokenHeaderName());

        if (header != null) {
            if (header.startsWith(properties.getTokenPrefix())) {
                return header.substring(properties.getTokenPrefix().length());
            }
            // 접두사 없이도 허용
            return header;
        }

        // 2. Cookie에서 추출 (설정된 경우)
        if (properties.isAllowCookie() && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (properties.getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 제외 패턴 확인
     */
    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, properties.getExcludePatterns());
    }

    /**
     * OAuth2 에러 타입 매핑
     */
    private String getErrorType(com.eraf.core.exception.ErrorCode errorCode) {
        if (errorCode == OAuth2ErrorCode.OAUTH2_TOKEN_INVALID) {
            return "invalid_token";
        } else if (errorCode == OAuth2ErrorCode.OAUTH2_TOKEN_EXPIRED) {
            return "invalid_token";
        } else if (errorCode == OAuth2ErrorCode.OAUTH2_INSUFFICIENT_SCOPE) {
            return "insufficient_scope";
        }
        return "invalid_request";
    }
}
