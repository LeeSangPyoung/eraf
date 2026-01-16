package com.eraf.gateway.jwt;

import com.eraf.core.crypto.Jwt;
import com.eraf.core.crypto.Jwt.JwtValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 기본 JWT 검증 구현체
 * eraf-core의 Jwt 유틸리티 활용
 */
@Slf4j
public class DefaultJwtValidator implements JwtValidator {

    private final String secretKey;

    public DefaultJwtValidator(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public JwtValidationResult validate(String token) {
        try {
            // eraf-core의 Jwt.verify() 사용
            Claims claims = Jwt.verify(token, secretKey);
            Map<String, Object> claimsMap = new HashMap<>(claims);

            return JwtValidationResult.success(claimsMap);

        } catch (JwtValidationException e) {
            log.warn("JWT validation failed: {}", e.getMessage());

            // 원인 예외로 만료 여부 판단
            if (e.getCause() instanceof ExpiredJwtException) {
                return JwtValidationResult.failure("JWT_EXPIRED", "Token has expired");
            }

            return JwtValidationResult.failure("JWT_INVALID", e.getMessage());

        } catch (Exception e) {
            log.error("JWT validation error", e);
            return JwtValidationResult.failure("JWT_ERROR", "Token validation failed");
        }
    }
}
