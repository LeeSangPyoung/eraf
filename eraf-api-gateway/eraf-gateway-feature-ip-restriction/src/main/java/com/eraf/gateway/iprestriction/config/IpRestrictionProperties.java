package com.eraf.gateway.iprestriction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * IP Restriction 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.ip-restriction")
public class IpRestrictionProperties {

    /**
     * IP Restriction 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * 기본 블랙리스트 IP 목록
     */
    private List<String> defaultBlacklist = new ArrayList<>();

    /**
     * 기본 화이트리스트 IP 목록
     */
    private List<String> defaultWhitelist = new ArrayList<>();

    /**
     * CIDR 블록 지원 여부
     */
    private boolean supportCidr = true;

    /**
     * 만료된 규칙 자동 정리 여부
     */
    private boolean autoCleanupExpired = true;

    /**
     * 만료 규칙 정리 주기 (분)
     */
    private int cleanupIntervalMinutes = 60;
}
