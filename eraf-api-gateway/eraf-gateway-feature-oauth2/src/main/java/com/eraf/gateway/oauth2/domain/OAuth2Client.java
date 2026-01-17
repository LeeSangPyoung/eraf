package com.eraf.gateway.oauth2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * OAuth2 Client 도메인 모델
 * OAuth2 클라이언트 애플리케이션 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Client {

    /**
     * Client ID (공개 식별자)
     */
    private String clientId;

    /**
     * Client Secret (비밀 키)
     */
    private String clientSecret;

    /**
     * Client 이름
     */
    private String clientName;

    /**
     * Client 설명
     */
    private String description;

    /**
     * 허용된 Redirect URI 목록
     * Authorization Code Flow에서 사용
     */
    private Set<String> redirectUris;

    /**
     * 허용된 Grant Type 목록
     * - authorization_code
     * - client_credentials
     * - password
     * - refresh_token
     * - implicit
     */
    private Set<String> allowedGrantTypes;

    /**
     * 허용된 Scope 목록
     */
    private Set<String> allowedScopes;

    /**
     * Access Token 유효 시간 (초)
     */
    private Integer accessTokenValiditySeconds;

    /**
     * Refresh Token 유효 시간 (초)
     */
    private Integer refreshTokenValiditySeconds;

    /**
     * 활성화 여부
     */
    private boolean enabled;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;

    /**
     * Client가 유효한지 확인
     */
    public boolean isValid() {
        return enabled;
    }

    /**
     * 특정 Grant Type을 지원하는지 확인
     */
    public boolean supportsGrantType(String grantType) {
        return allowedGrantTypes != null && allowedGrantTypes.contains(grantType);
    }

    /**
     * 특정 Redirect URI가 허용되는지 확인
     */
    public boolean isRedirectUriAllowed(String redirectUri) {
        if (redirectUris == null || redirectUris.isEmpty()) {
            return false;
        }
        return redirectUris.contains(redirectUri);
    }

    /**
     * 특정 Scope가 허용되는지 확인
     */
    public boolean isScopeAllowed(String scope) {
        if (allowedScopes == null || allowedScopes.isEmpty()) {
            return true; // 모든 스코프 허용
        }
        return allowedScopes.contains(scope);
    }

    /**
     * 모든 Scope가 허용되는지 확인
     */
    public boolean areAllScopesAllowed(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return true;
        }
        if (allowedScopes == null || allowedScopes.isEmpty()) {
            return true; // 모든 스코프 허용
        }
        return allowedScopes.containsAll(scopes);
    }
}
