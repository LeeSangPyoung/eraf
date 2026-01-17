package com.eraf.gateway.jwt;

/**
 * JWT 검증 인터페이스
 */
public interface JwtValidator {

    /**
     * JWT 토큰 검증
     */
    JwtValidationResult validate(String token);
}
