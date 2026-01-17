package com.eraf.gateway.oauth2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 설정 프로퍼티
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.oauth2")
public class OAuth2Properties {

    /**
     * OAuth2 인증 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * Token 헤더 이름
     */
    private String tokenHeaderName = "Authorization";

    /**
     * Token 접두사 (기본값: "Bearer ")
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 스코프 검증 활성화 여부
     */
    private boolean validateScopes = true;

    /**
     * 필수 스코프 목록 (전역 설정)
     */
    private List<String> requiredScopes = new ArrayList<>();

    /**
     * Token Introspection Endpoint URL
     * 외부 OAuth2 서버의 introspection endpoint
     * 설정되지 않으면 로컬 검증 사용
     */
    private String introspectionEndpoint;

    /**
     * Introspection Client ID
     * 외부 introspection endpoint 호출 시 사용
     */
    private String introspectionClientId;

    /**
     * Introspection Client Secret
     * 외부 introspection endpoint 호출 시 사용
     */
    private String introspectionClientSecret;

    /**
     * 로컬 검증 사용 여부
     * true: 로컬 DB에서 토큰 검증
     * false: 외부 introspection endpoint 사용
     */
    private boolean localValidation = true;

    /**
     * 검증 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Cookie에서 토큰 추출 허용 여부
     */
    private boolean allowCookie = false;

    /**
     * Cookie 이름
     */
    private String cookieName = "access_token";

    /**
     * 클레임을 헤더로 전달 활성화
     */
    private boolean propagateClaims = true;

    /**
     * Access Token 기본 유효 시간 (초)
     */
    private Integer defaultAccessTokenValiditySeconds = 3600; // 1시간

    /**
     * Refresh Token 기본 유효 시간 (초)
     */
    private Integer defaultRefreshTokenValiditySeconds = 2592000; // 30일
}
