package com.eraf.gateway.oauth2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2 Authorization Code 도메인 모델
 * Authorization Code Flow에서 사용되는 인증 코드
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthorizationCode {

    /**
     * Authorization Code 값
     */
    private String code;

    /**
     * 코드가 발급된 Client ID
     */
    private String clientId;

    /**
     * 코드가 발급된 사용자 ID
     */
    private String userId;

    /**
     * Redirect URI (코드 교환 시 검증에 사용)
     */
    private String redirectUri;

    /**
     * 요청된 Scope 목록
     */
    private List<String> scopes;

    /**
     * 코드 만료 시간
     */
    private LocalDateTime expiresAt;

    /**
     * 코드 발급 시간
     */
    private LocalDateTime issuedAt;

    /**
     * 코드 사용 여부
     */
    private boolean used;

    /**
     * PKCE Code Challenge (옵션)
     */
    private String codeChallenge;

    /**
     * PKCE Code Challenge Method (옵션)
     * - S256 (SHA-256)
     * - plain
     */
    private String codeChallengeMethod;

    /**
     * Authorization Code가 유효한지 확인
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * PKCE가 활성화되어 있는지 확인
     */
    public boolean isPkceEnabled() {
        return codeChallenge != null && !codeChallenge.isEmpty();
    }

    /**
     * Authorization Code를 사용됨으로 표시
     */
    public OAuth2AuthorizationCode markAsUsed() {
        return OAuth2AuthorizationCode.builder()
                .code(this.code)
                .clientId(this.clientId)
                .userId(this.userId)
                .redirectUri(this.redirectUri)
                .scopes(this.scopes)
                .expiresAt(this.expiresAt)
                .issuedAt(this.issuedAt)
                .used(true)
                .codeChallenge(this.codeChallenge)
                .codeChallengeMethod(this.codeChallengeMethod)
                .build();
    }
}
