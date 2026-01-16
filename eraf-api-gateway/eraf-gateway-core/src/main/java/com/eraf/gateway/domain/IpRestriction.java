package com.eraf.gateway.domain;

import com.eraf.core.utils.PathMatcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IP 제한 도메인 모델
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpRestriction {

    private String id;

    /**
     * IP 주소 또는 CIDR 블록 (예: 192.168.1.0/24)
     */
    private String ipAddress;

    /**
     * 제한 타입 (ALLOW: 화이트리스트, DENY: 블랙리스트)
     */
    private RestrictionType type;

    /**
     * 적용 경로 패턴 (null이면 모든 경로에 적용)
     */
    private String pathPattern;

    /**
     * 설명
     */
    private String description;

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
     * 제한 타입
     */
    public enum RestrictionType {
        ALLOW,  // 화이트리스트 (이 IP만 허용)
        DENY    // 블랙리스트 (이 IP 차단)
    }

    /**
     * 해당 IP 제한 규칙이 유효한지 확인
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
     * 특정 IP가 이 규칙에 매칭되는지 확인
     */
    public boolean matches(String clientIp) {
        if (ipAddress.contains("/")) {
            return matchesCidr(clientIp);
        }
        return ipAddress.equals(clientIp);
    }

    /**
     * 특정 경로가 이 규칙의 적용 대상인지 확인
     */
    public boolean matchesPath(String path) {
        return PathMatcher.matches(path, pathPattern);
    }

    /**
     * CIDR 블록 매칭
     */
    private boolean matchesCidr(String clientIp) {
        try {
            String[] parts = ipAddress.split("/");
            String networkAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            long clientIpLong = ipToLong(clientIp);
            long networkIpLong = ipToLong(networkAddress);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

            return (clientIpLong & mask) == (networkIpLong & mask);
        } catch (Exception e) {
            return false;
        }
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(octets[i]);
        }
        return result;
    }
}
