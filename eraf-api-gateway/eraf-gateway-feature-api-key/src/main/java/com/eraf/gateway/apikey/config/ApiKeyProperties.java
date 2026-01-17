package com.eraf.gateway.apikey.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * API Key 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.api-key")
public class ApiKeyProperties {

    /**
     * API Key 인증 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * API Key 헤더 이름
     */
    private String headerName = "X-API-Key";

    /**
     * 인증 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Authorization 헤더에서 API Key 추출 허용 여부
     */
    private boolean allowAuthorizationHeader = true;

    /**
     * 쿼리 파라미터에서 API Key 추출 허용 여부 (보안상 비권장)
     */
    private boolean allowQueryParameter = false;

    /**
     * API Key 길이 (생성 시)
     */
    private int keyLength = 32;

    /**
     * API Key 만료 체크 활성화
     */
    private boolean checkExpiration = true;

    /**
     * IP 제한 체크 활성화
     */
    private boolean checkIpRestriction = true;

    /**
     * 경로 제한 체크 활성화
     */
    private boolean checkPathRestriction = true;
}
