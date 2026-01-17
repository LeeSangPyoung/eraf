package com.eraf.gateway.oauth2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2 Access Token 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Token {

    /**
     * Token ID
     */
    private String id;

    /**
     * Access Token 값
     */
    private String accessToken;

    /**
     * Refresh Token 값
     */
    private String refreshToken;

    /**
     * Token 타입 (일반적으로 "Bearer")
     */
    private String tokenType;

    /**
     * Token이 속한 Client ID
     */
    private String clientId;

    /**
     * Token이 발급된 사용자 ID
     */
    private String userId;

    /**
     * Token 스코프 (권한 범위)
     */
    private List<String> scopes;

    /**
     * Token 만료 시간
     */
    private LocalDateTime expiresAt;

    /**
     * Refresh Token 만료 시간
     */
    private LocalDateTime refreshExpiresAt;

    /**
     * Token 발급 시간
     */
    private LocalDateTime issuedAt;

    /**
     * Token이 유효한지 확인
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Refresh Token이 유효한지 확인
     */
    public boolean isRefreshValid() {
        return refreshExpiresAt != null && LocalDateTime.now().isBefore(refreshExpiresAt);
    }

    /**
     * 특정 스코프를 가지고 있는지 확인
     */
    public boolean hasScope(String scope) {
        return scopes != null && scopes.contains(scope);
    }

    /**
     * 모든 필수 스코프를 가지고 있는지 확인
     */
    public boolean hasAllScopes(List<String> requiredScopes) {
        if (requiredScopes == null || requiredScopes.isEmpty()) {
            return true;
        }
        if (scopes == null || scopes.isEmpty()) {
            return false;
        }
        return scopes.containsAll(requiredScopes);
    }

    /**
     * 적어도 하나의 스코프를 가지고 있는지 확인
     */
    public boolean hasAnyScope(List<String> requiredScopes) {
        if (requiredScopes == null || requiredScopes.isEmpty()) {
            return true;
        }
        if (scopes == null || scopes.isEmpty()) {
            return false;
        }
        for (String scope : requiredScopes) {
            if (scopes.contains(scope)) {
                return true;
            }
        }
        return false;
    }
}
