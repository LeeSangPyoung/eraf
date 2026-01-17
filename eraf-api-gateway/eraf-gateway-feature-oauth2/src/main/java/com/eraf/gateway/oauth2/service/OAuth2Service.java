package com.eraf.gateway.oauth2.service;

import com.eraf.gateway.oauth2.domain.OAuth2Client;
import com.eraf.gateway.oauth2.domain.OAuth2Token;
import com.eraf.gateway.oauth2.exception.InsufficientScopeException;
import com.eraf.gateway.oauth2.exception.InvalidTokenException;
import com.eraf.gateway.oauth2.exception.OAuth2ErrorCode;
import com.eraf.gateway.oauth2.exception.OAuth2Exception;
import com.eraf.gateway.oauth2.repository.OAuth2ClientRepository;
import com.eraf.gateway.oauth2.repository.OAuth2TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 인증/인가 서비스
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2TokenRepository tokenRepository;
    private final OAuth2ClientRepository clientRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    /**
     * Access Token 검증
     *
     * @param accessToken Access Token
     * @return 검증된 OAuth2Token
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    public OAuth2Token validateToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("Access token is missing");
            throw OAuth2Exception.tokenMissing();
        }

        Optional<OAuth2Token> optionalToken = tokenRepository.findByAccessToken(accessToken);

        if (optionalToken.isEmpty()) {
            log.warn("Access token not found: {}", maskToken(accessToken));
            throw OAuth2Exception.tokenInvalid();
        }

        OAuth2Token token = optionalToken.get();

        if (!token.isValid()) {
            log.warn("Access token expired: {}", token.getId());
            throw OAuth2Exception.tokenExpired();
        }

        log.debug("Access token validated: {}", token.getId());
        return token;
    }

    /**
     * Token Introspection (RFC 7662)
     * 외부 OAuth2 서버에 토큰 검증 요청하는 대신, 로컬 검증 수행
     *
     * @param token Access Token
     * @return TokenIntrospection 응답
     */
    public TokenIntrospection introspectToken(String token) {
        try {
            OAuth2Token oauthToken = validateToken(token);

            String scope = oauthToken.getScopes() != null
                    ? String.join(" ", oauthToken.getScopes())
                    : "";

            Long exp = oauthToken.getExpiresAt().toEpochSecond(ZoneOffset.UTC);
            Long iat = oauthToken.getIssuedAt().toEpochSecond(ZoneOffset.UTC);

            return TokenIntrospection.active(
                    oauthToken.getClientId(),
                    oauthToken.getUserId(),
                    scope,
                    exp,
                    iat,
                    oauthToken.getUserId()
            );

        } catch (OAuth2Exception e) {
            log.debug("Token introspection failed: {}", e.getMessage());
            return TokenIntrospection.inactive();
        }
    }

    /**
     * 스코프 검증
     *
     * @param requiredScopes 필수 스코프 목록
     * @param actualScopes   실제 토큰의 스코프 목록
     * @throws InsufficientScopeException 스코프가 부족한 경우
     */
    public void validateScope(List<String> requiredScopes, List<String> actualScopes) {
        if (requiredScopes == null || requiredScopes.isEmpty()) {
            return; // 필수 스코프가 없으면 검증 통과
        }

        if (actualScopes == null || actualScopes.isEmpty()) {
            log.warn("Token has no scopes, but required: {}", requiredScopes);
            throw new InsufficientScopeException(requiredScopes, actualScopes);
        }

        for (String requiredScope : requiredScopes) {
            if (!actualScopes.contains(requiredScope)) {
                log.warn("Insufficient scope. Required: {}, Actual: {}", requiredScopes, actualScopes);
                throw new InsufficientScopeException(requiredScopes, actualScopes);
            }
        }

        log.debug("Scope validation passed");
    }

    /**
     * Access Token 생성
     *
     * @param clientId Client ID
     * @param userId   사용자 ID
     * @param scopes   스코프 목록
     * @return 생성된 OAuth2Token
     */
    public OAuth2Token generateToken(String clientId, String userId, List<String> scopes) {
        Optional<OAuth2Client> optionalClient = clientRepository.findByClientId(clientId);

        if (optionalClient.isEmpty()) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_CLIENT_NOT_FOUND);
        }

        OAuth2Client client = optionalClient.get();

        if (!client.isValid()) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_CLIENT_DISABLED);
        }

        // 스코프 검증
        if (!client.areAllScopesAllowed(scopes)) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_INVALID_SCOPE);
        }

        String accessToken = generateSecureToken();
        String refreshToken = generateSecureToken();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiresAt = now.plusSeconds(
                client.getAccessTokenValiditySeconds() != null
                        ? client.getAccessTokenValiditySeconds()
                        : 3600 // 기본 1시간
        );
        LocalDateTime refreshExpiresAt = now.plusSeconds(
                client.getRefreshTokenValiditySeconds() != null
                        ? client.getRefreshTokenValiditySeconds()
                        : 2592000 // 기본 30일
        );

        OAuth2Token token = OAuth2Token.builder()
                .id(UUID.randomUUID().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .clientId(clientId)
                .userId(userId)
                .scopes(scopes)
                .expiresAt(accessExpiresAt)
                .refreshExpiresAt(refreshExpiresAt)
                .issuedAt(now)
                .build();

        return tokenRepository.save(token);
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     *
     * @param refreshToken Refresh Token
     * @return 새로운 OAuth2Token
     */
    public OAuth2Token refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_MISSING);
        }

        Optional<OAuth2Token> optionalToken = tokenRepository.findByRefreshToken(refreshToken);

        if (optionalToken.isEmpty()) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_INVALID);
        }

        OAuth2Token oldToken = optionalToken.get();

        if (!oldToken.isRefreshValid()) {
            throw new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_EXPIRED);
        }

        // 기존 토큰 삭제
        tokenRepository.deleteByAccessToken(oldToken.getAccessToken());

        // 새로운 토큰 생성
        return generateToken(oldToken.getClientId(), oldToken.getUserId(), oldToken.getScopes());
    }

    /**
     * Token 취소 (Revoke)
     *
     * @param accessToken Access Token
     */
    public void revokeToken(String accessToken) {
        tokenRepository.deleteByAccessToken(accessToken);
        log.info("Token revoked: {}", maskToken(accessToken));
    }

    /**
     * 사용자의 모든 Token 취소
     *
     * @param userId 사용자 ID
     */
    public void revokeAllUserTokens(String userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("All tokens revoked for user: {}", userId);
    }

    /**
     * 안전한 토큰 생성
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 토큰 마스킹 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}
