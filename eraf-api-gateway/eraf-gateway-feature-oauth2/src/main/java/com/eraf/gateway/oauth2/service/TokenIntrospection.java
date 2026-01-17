package com.eraf.gateway.oauth2.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RFC 7662 Token Introspection Response
 * https://tools.ietf.org/html/rfc7662
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenIntrospection {

    /**
     * 토큰이 활성 상태인지 여부 (REQUIRED)
     */
    @JsonProperty("active")
    private boolean active;

    /**
     * 토큰의 스코프 (공백으로 구분된 문자열)
     */
    @JsonProperty("scope")
    private String scope;

    /**
     * 토큰의 클라이언트 ID
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 토큰의 사용자명
     */
    @JsonProperty("username")
    private String username;

    /**
     * 토큰 타입 (일반적으로 "Bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 토큰 만료 시간 (Unix timestamp)
     */
    @JsonProperty("exp")
    private Long exp;

    /**
     * 토큰 발급 시간 (Unix timestamp)
     */
    @JsonProperty("iat")
    private Long iat;

    /**
     * 토큰이 유효하기 시작하는 시간 (Unix timestamp)
     */
    @JsonProperty("nbf")
    private Long nbf;

    /**
     * 토큰의 주체 (일반적으로 사용자 ID)
     */
    @JsonProperty("sub")
    private String sub;

    /**
     * 토큰의 대상 (Audience)
     */
    @JsonProperty("aud")
    private String aud;

    /**
     * 토큰 발급자 (Issuer)
     */
    @JsonProperty("iss")
    private String iss;

    /**
     * 토큰 ID (JWT ID)
     */
    @JsonProperty("jti")
    private String jti;

    /**
     * 스코프 목록 (편의 메서드)
     */
    public List<String> getScopeList() {
        if (scope == null || scope.isEmpty()) {
            return List.of();
        }
        return List.of(scope.split("\\s+"));
    }

    /**
     * 비활성 토큰 응답 생성
     */
    public static TokenIntrospection inactive() {
        return TokenIntrospection.builder()
                .active(false)
                .build();
    }

    /**
     * 활성 토큰 응답 생성
     */
    public static TokenIntrospection active(String clientId, String username, String scope,
                                           Long exp, Long iat, String sub) {
        return TokenIntrospection.builder()
                .active(true)
                .clientId(clientId)
                .username(username)
                .scope(scope)
                .tokenType("Bearer")
                .exp(exp)
                .iat(iat)
                .sub(sub)
                .build();
    }
}
