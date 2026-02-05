package com.eraf.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT 설정 속성
 */
@ConfigurationProperties(prefix = "eraf.security.jwt")
public class JwtProperties {

    /**
     * JWT 활성화 여부
     */
    private boolean enabled = false;

    /**
     * 서명 비밀키 (최소 32자, Base64 인코딩 권장)
     */
    private String secret;

    /**
     * 발급자 (issuer)
     */
    private String issuer = "eraf";

    /**
     * Access Token 만료 시간
     */
    private Duration accessTokenExpiration = Duration.ofHours(1);

    /**
     * Refresh Token 만료 시간
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    /**
     * Authorization 헤더 이름
     */
    private String headerName = "Authorization";

    /**
     * Token 접두어
     */
    private String tokenPrefix = "Bearer ";

    /**
     * JWT 검증 건너뛸 URL 패턴
     */
    private String[] skipPatterns = {
            "/api/auth/**",
            "/api/public/**"
    };

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String[] getSkipPatterns() {
        return skipPatterns;
    }

    public void setSkipPatterns(String[] skipPatterns) {
        this.skipPatterns = skipPatterns;
    }
}
