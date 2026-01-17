package com.eraf.gateway.apikey.domain;

import org.springframework.util.AntPathMatcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * API Key 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    private String id;

    /**
     * API Key 값
     */
    private String apiKey;

    /**
     * API Key 이름 (식별용)
     */
    private String name;

    /**
     * API Key 설명
     */
    private String description;

    /**
     * 허용된 경로 패턴 (null이면 모든 경로 허용)
     */
    private Set<String> allowedPaths;

    /**
     * 허용된 IP 주소 (null이면 모든 IP 허용)
     */
    private Set<String> allowedIps;

    /**
     * Rate Limit (초당 요청 수, null이면 제한 없음)
     */
    private Integer rateLimitPerSecond;

    /**
     * 활성화 여부
     */
    private boolean enabled;

    /**
     * 만료 일시 (null이면 무기한)
     */
    private LocalDateTime expiresAt;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * API Key가 유효한지 확인
     */
    public boolean isValid() {
        if (!enabled) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    /**
     * 특정 경로에 대한 접근 권한이 있는지 확인
     */
    public boolean isPathAllowed(String path) {
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            return true;
        }
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String pattern : allowedPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 특정 IP에서의 접근 권한이 있는지 확인
     */
    public boolean isIpAllowed(String ip) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return true;
        }
        return allowedIps.contains(ip);
    }
}
