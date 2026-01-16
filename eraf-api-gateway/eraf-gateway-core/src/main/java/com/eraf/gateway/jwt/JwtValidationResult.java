package com.eraf.gateway.jwt;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * JWT 검증 결과
 */
@Getter
@Builder
public class JwtValidationResult {

    private final boolean valid;
    private final String errorCode;
    private final String errorMessage;
    private final Map<String, Object> claims;

    public static JwtValidationResult success(Map<String, Object> claims) {
        return JwtValidationResult.builder()
                .valid(true)
                .claims(claims)
                .build();
    }

    public static JwtValidationResult failure(String errorCode, String errorMessage) {
        return JwtValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
