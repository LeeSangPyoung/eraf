package com.eraf.starter.session;

import com.eraf.core.crypto.Jwt;
import io.jsonwebtoken.Claims;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ERAF JWT 토큰 프로바이더
 */
public class ErafJwtTokenProvider {

    private final ErafSessionProperties properties;
    private final String secretKey;

    public ErafJwtTokenProvider(ErafSessionProperties properties) {
        this.properties = properties;
        this.secretKey = properties.getJwt().getSecret();
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String userId, Map<String, Object> claims) {
        long expirationMinutes = properties.getJwt().getExpiration().toMinutes();

        Map<String, Object> allClaims = new HashMap<>();
        allClaims.put("sub", userId);
        allClaims.put("jti", UUID.randomUUID().toString());
        allClaims.put("type", "access");
        if (claims != null) {
            allClaims.putAll(claims);
        }

        return Jwt.create(allClaims, secretKey, expirationMinutes);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String userId) {
        long expirationMinutes = properties.getJwt().getRefreshExpiration().toMinutes();

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("type", "refresh");

        return Jwt.create(claims, secretKey, expirationMinutes);
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public String getUserId(String token) {
        Claims claims = Jwt.verify(token, secretKey);
        return claims.getSubject();
    }

    /**
     * 토큰에서 클레임 추출
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getClaims(String token) {
        Claims claims = Jwt.verify(token, secretKey);
        return new HashMap<>(claims);
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        return Jwt.verifyQuietly(token, secretKey).isPresent();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        return Jwt.isExpired(token, secretKey);
    }

    /**
     * HTTP 헤더에서 토큰 추출
     */
    public String resolveToken(String bearerToken) {
        String prefix = properties.getJwt().getTokenPrefix();
        if (bearerToken != null && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length());
        }
        return null;
    }

    /**
     * 토큰 쌍 생성 (Access + Refresh)
     */
    public TokenPair createTokenPair(String userId, Map<String, Object> claims) {
        String accessToken = createAccessToken(userId, claims);
        String refreshToken = createRefreshToken(userId);
        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * 토큰 만료 시간 추출
     */
    public Optional<Instant> getExpiration(String token) {
        return Jwt.getExpiration(token);
    }

    /**
     * 토큰 쌍
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
