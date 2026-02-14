package com.eraf.security.ip;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * IP 접근 제어 설정
 */
@ConfigurationProperties(prefix = "eraf.security.ip-access-control")
public class IpAccessControlProperties {

    /**
     * IP 접근 제어 활성화 여부
     */
    private boolean enabled = false;

    /**
     * 기본 정책: ALLOW(기본허용) 또는 DENY(기본차단)
     */
    private AccessPolicy defaultPolicy = AccessPolicy.ALLOW;

    /**
     * 화이트리스트 IP/CIDR 목록
     * 예: ["192.168.1.100", "10.0.0.0/8", "172.16.0.0/12"]
     */
    private List<String> whitelist = new ArrayList<>();

    /**
     * 블랙리스트 IP/CIDR 목록
     */
    private List<String> blacklist = new ArrayList<>();

    /**
     * IP 접근 제어를 적용할 URL 패턴
     * 예: ["/admin/**", "/api/sensitive/**"]
     * 빈 리스트면 모든 요청에 적용
     */
    private List<String> includePatterns = new ArrayList<>();

    /**
     * IP 접근 제어에서 제외할 URL 패턴
     * 예: ["/actuator/health", "/public/**"]
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * X-Forwarded-For 헤더 사용 여부 (프록시 환경)
     */
    private boolean useForwardedFor = true;

    /**
     * X-Real-IP 헤더 사용 여부
     */
    private boolean useRealIp = true;

    /**
     * 차단 시 응답 메시지
     */
    private String deniedMessage = "Access denied from your IP address";

    /**
     * 차단 시 감사 로그 기록 여부
     */
    private boolean auditOnDenied = true;

    /**
     * 접근 정책
     */
    public enum AccessPolicy {
        /**
         * 기본 허용 (블랙리스트 방식)
         */
        ALLOW,
        /**
         * 기본 차단 (화이트리스트 방식)
         */
        DENY
    }

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AccessPolicy getDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(AccessPolicy defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public List<String> getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(List<String> includePatterns) {
        this.includePatterns = includePatterns;
    }

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public boolean isUseForwardedFor() {
        return useForwardedFor;
    }

    public void setUseForwardedFor(boolean useForwardedFor) {
        this.useForwardedFor = useForwardedFor;
    }

    public boolean isUseRealIp() {
        return useRealIp;
    }

    public void setUseRealIp(boolean useRealIp) {
        this.useRealIp = useRealIp;
    }

    public String getDeniedMessage() {
        return deniedMessage;
    }

    public void setDeniedMessage(String deniedMessage) {
        this.deniedMessage = deniedMessage;
    }

    public boolean isAuditOnDenied() {
        return auditOnDenied;
    }

    public void setAuditOnDenied(boolean auditOnDenied) {
        this.auditOnDenied = auditOnDenied;
    }
}
