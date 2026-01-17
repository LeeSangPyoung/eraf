package com.eraf.gateway.oauth2.repository;

import com.eraf.gateway.oauth2.domain.OAuth2Token;

import java.util.List;
import java.util.Optional;

/**
 * OAuth2 Token 저장소 인터페이스
 */
public interface OAuth2TokenRepository {

    /**
     * Access Token으로 조회
     */
    Optional<OAuth2Token> findByAccessToken(String accessToken);

    /**
     * Refresh Token으로 조회
     */
    Optional<OAuth2Token> findByRefreshToken(String refreshToken);

    /**
     * User ID로 조회
     */
    List<OAuth2Token> findByUserId(String userId);

    /**
     * Client ID로 조회
     */
    List<OAuth2Token> findByClientId(String clientId);

    /**
     * Token 저장
     */
    OAuth2Token save(OAuth2Token token);

    /**
     * Token 삭제 (by Access Token)
     */
    void deleteByAccessToken(String accessToken);

    /**
     * Token 삭제 (by Refresh Token)
     */
    void deleteByRefreshToken(String refreshToken);

    /**
     * User의 모든 Token 삭제
     */
    void deleteByUserId(String userId);

    /**
     * 만료된 Token 삭제
     */
    void deleteExpiredTokens();
}
