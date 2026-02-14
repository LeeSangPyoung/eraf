package com.eraf.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "authorities";
    private static final String USER_ID_KEY = "userId";

    private final SecretKey secretKey;
    private final JwtProperties properties;
    private final JwtParser jwtParser;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, properties.getAccessTokenExpiration().toMillis());
    }

    /**
     * Access Token 생성 (사용자 정보 직접 지정)
     */
    public String createAccessToken(String username, String userId, Collection<String> roles) {
        return createToken(username, userId, roles, properties.getAccessTokenExpiration().toMillis());
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, properties.getRefreshTokenExpiration().toMillis());
    }

    /**
     * Refresh Token 생성 (사용자 정보 직접 지정)
     */
    public String createRefreshToken(String username, String userId) {
        return createToken(username, userId, Collections.emptyList(), properties.getRefreshTokenExpiration().toMillis());
    }

    private String createToken(Authentication authentication, long expirationMillis) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Object principal = authentication.getPrincipal();
        String userId = null;
        if (principal instanceof ErafUserDetails erafUserDetails) {
            userId = erafUserDetails.getUserId();
        }

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMillis);

        JwtBuilder builder = Jwts.builder()
                .subject(authentication.getName())
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(AUTHORITIES_KEY, authorities);

        if (userId != null) {
            builder.claim(USER_ID_KEY, userId);
        }

        return builder.signWith(secretKey).compact();
    }

    private String createToken(String username, String userId, Collection<String> roles, long expirationMillis) {
        String authorities = String.join(",", roles);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMillis);

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim(AUTHORITIES_KEY, authorities);

        if (userId != null) {
            builder.claim(USER_ID_KEY, userId);
        }

        return builder.signWith(secretKey).compact();
    }

    /**
     * 토큰에서 Authentication 객체 추출
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String authoritiesStr = claims.get(AUTHORITIES_KEY, String.class);
        Collection<? extends GrantedAuthority> authorities;

        if (authoritiesStr == null || authoritiesStr.isEmpty()) {
            authorities = Collections.emptyList();
        } else {
            authorities = Arrays.stream(authoritiesStr.split(","))
                    .filter(auth -> !auth.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        String userId = claims.get(USER_ID_KEY, String.class);
        UserDetails principal = new ErafUserDetails(claims.getSubject(), "", authorities, userId);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public String getUserId(String token) {
        return parseClaims(token).get(USER_ID_KEY, String.class);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
        } catch (SecurityException | io.jsonwebtoken.security.SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 토큰 만료 시간 조회
     */
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
