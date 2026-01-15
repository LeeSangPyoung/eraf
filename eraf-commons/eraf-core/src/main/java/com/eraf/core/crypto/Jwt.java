package com.eraf.core.crypto;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * JWT 유틸리티 (HS256/RS256)
 */
public final class Jwt {

    private Jwt() {
    }

    /**
     * JWT 생성 (HS256)
     *
     * @param claims    클레임
     * @param secretKey 비밀 키 (최소 32바이트)
     * @param expirationMinutes 만료 시간 (분)
     * @return JWT 토큰
     */
    public static String create(Map<String, Object> claims, String secretKey, long expirationMinutes) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key);

        if (claims != null) {
            claims.forEach(builder::claim);
        }

        return builder.compact();
    }

    /**
     * JWT 생성 (기본 1시간 만료)
     */
    public static String create(Map<String, Object> claims, String secretKey) {
        return create(claims, secretKey, 60);
    }

    /**
     * JWT 검증 및 클레임 추출
     *
     * @param token     JWT 토큰
     * @param secretKey 비밀 키
     * @return 클레임
     */
    public static Claims verify(String token, String secretKey) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("만료된 토큰입니다", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("잘못된 형식의 토큰입니다", e);
        } catch (SecurityException e) {
            throw new JwtValidationException("서명 검증에 실패했습니다", e);
        } catch (Exception e) {
            throw new JwtValidationException("토큰 검증에 실패했습니다", e);
        }
    }

    /**
     * JWT 검증 (예외 없이 Optional 반환)
     */
    public static Optional<Claims> verifyQuietly(String token, String secretKey) {
        try {
            return Optional.of(verify(token, secretKey));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * JWT 만료 여부 확인
     */
    public static boolean isExpired(String token, String secretKey) {
        try {
            verify(token, secretKey);
            return false;
        } catch (JwtValidationException e) {
            return e.getCause() instanceof ExpiredJwtException;
        }
    }

    /**
     * JWT에서 특정 클레임 추출 (검증 없이)
     */
    public static Optional<Object> getClaim(String token, String claimName) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Optional.empty();
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // 간단한 JSON 파싱 (Jackson 없이)
            if (payload.contains("\"" + claimName + "\"")) {
                int start = payload.indexOf("\"" + claimName + "\"");
                int colonIndex = payload.indexOf(":", start);
                int endIndex = payload.indexOf(",", colonIndex);
                if (endIndex == -1) {
                    endIndex = payload.indexOf("}", colonIndex);
                }
                String value = payload.substring(colonIndex + 1, endIndex).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return Optional.of(value);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * JWT 만료 시간 추출 (검증 없이)
     */
    public static Optional<Instant> getExpiration(String token) {
        return getClaim(token, "exp")
                .map(exp -> {
                    try {
                        long epochSeconds = Long.parseLong(exp.toString());
                        return Instant.ofEpochSecond(epochSeconds);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }

    /**
     * 새 비밀 키 생성
     */
    public static String generateSecretKey() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * JWT 검증 예외
     */
    public static class JwtValidationException extends CryptoException {
        public JwtValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
