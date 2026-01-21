package com.eraf.starter.session;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ERAF Session Configuration Properties
 * JWT + Redis 기반 세션 관리
 */
@ConfigurationProperties(prefix = "eraf.session")
public class ErafSessionProperties {

    /**
     * Session timeout duration
     */
    private Duration timeout = Duration.ofMinutes(30);

    /**
     * Session cookie name
     */
    private String cookieName = "ERAF_SESSION";

    /**
     * Enable secure cookie
     */
    private boolean secureCookie = false;

    /**
     * Enable HTTP only cookie
     */
    private boolean httpOnlyCookie = true;

    /**
     * Session cookie path
     */
    private String cookiePath = "/";

    /**
     * JWT 설정
     */
    private Jwt jwt = new Jwt();

    /**
     * 동시 세션 설정
     */
    private ConcurrentSession concurrentSession = new ConcurrentSession();

    /**
     * Redis 세션 네임스페이스
     */
    private String redisNamespace = "eraf:session";

    public static class Jwt {
        /**
         * JWT 활성화
         */
        private boolean enabled = true;

        /**
         * JWT 비밀키
         */
        private String secret;

        /**
         * JWT 만료 시간
         */
        private Duration expiration = Duration.ofHours(1);

        /**
         * Refresh Token 만료 시간
         */
        private Duration refreshExpiration = Duration.ofDays(7);

        /**
         * JWT 헤더 이름
         */
        private String headerName = "Authorization";

        /**
         * JWT 토큰 접두사
         */
        private String tokenPrefix = "Bearer ";

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

        public Duration getExpiration() {
            return expiration;
        }

        public void setExpiration(Duration expiration) {
            this.expiration = expiration;
        }

        public Duration getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Duration refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
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
    }

    public static class ConcurrentSession {
        /**
         * 최대 동시 세션 수 (1 = 단일 세션만 허용)
         */
        private int maxSessions = 1;

        /**
         * 동시 세션 정책 (kick-old: 기존 세션 종료, 설계서 기준 고정값)
         */
        private ConcurrentSessionPolicy policy = ConcurrentSessionPolicy.KICK_OLD;

        public enum ConcurrentSessionPolicy {
            /**
             * 기존 세션 종료 (고정값)
             */
            KICK_OLD
        }

        public int getMaxSessions() {
            return maxSessions;
        }

        public void setMaxSessions(int maxSessions) {
            this.maxSessions = maxSessions;
        }

        public ConcurrentSessionPolicy getPolicy() {
            return policy;
        }

        public void setPolicy(ConcurrentSessionPolicy policy) {
            this.policy = policy;
        }
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public ConcurrentSession getConcurrentSession() {
        return concurrentSession;
    }

    public void setConcurrentSession(ConcurrentSession concurrentSession) {
        this.concurrentSession = concurrentSession;
    }

    public String getRedisNamespace() {
        return redisNamespace;
    }

    public void setRedisNamespace(String redisNamespace) {
        this.redisNamespace = redisNamespace;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public boolean isSecureCookie() {
        return secureCookie;
    }

    public void setSecureCookie(boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    public boolean isHttpOnlyCookie() {
        return httpOnlyCookie;
    }

    public void setHttpOnlyCookie(boolean httpOnlyCookie) {
        this.httpOnlyCookie = httpOnlyCookie;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }
}
