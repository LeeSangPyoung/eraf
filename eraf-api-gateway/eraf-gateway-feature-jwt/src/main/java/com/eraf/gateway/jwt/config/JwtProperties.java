package com.eraf.gateway.jwt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.jwt")
public class JwtProperties {

    /**
     * JWT 검증 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * JWT 헤더 이름
     */
    private String headerName = "Authorization";

    /**
     * JWT Secret Key
     */
    private String secretKey;

    /**
     * 검증 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Cookie에서 토큰 추출 허용 여부
     */
    private boolean allowCookie = true;

    /**
     * Cookie 이름
     */
    private String cookieName = "access_token";

    /**
     * 클레임을 헤더로 전달 활성화
     */
    private boolean propagateClaims = true;
}
