package com.eraf.gateway.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * ERAF Gateway 설정 속성
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "eraf.gateway")
public class ErafGatewayProperties {

    /**
     * Gateway 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 저장소 타입 (memory, jpa, redis)
     */
    private StoreType storeType = StoreType.MEMORY;

    /**
     * Rate Limiting 설정
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * IP Restriction 설정
     */
    private IpRestrictionConfig ipRestriction = new IpRestrictionConfig();

    /**
     * API Key 인증 설정
     */
    private ApiKeyConfig apiKey = new ApiKeyConfig();

    /**
     * Circuit Breaker 설정
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    /**
     * Analytics 설정
     */
    private AnalyticsConfig analytics = new AnalyticsConfig();

    /**
     * Response Cache 설정
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * JWT Validation 설정
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * Bot Detection 설정
     */
    private BotDetectionConfig botDetection = new BotDetectionConfig();

    /**
     * 저장소 타입
     */
    public enum StoreType {
        MEMORY,
        JPA,
        REDIS
    }

    /**
     * Rate Limiting 설정
     */
    @Getter
    @Setter
    public static class RateLimitConfig {
        /**
         * Rate Limiting 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**"};

        /**
         * 기본 Rate Limit (초당 요청 수)
         */
        private int defaultLimitPerSecond = 100;

        /**
         * 기본 윈도우 크기 (초)
         */
        private int defaultWindowSeconds = 1;
    }

    /**
     * IP Restriction 설정
     */
    @Getter
    @Setter
    public static class IpRestrictionConfig {
        /**
         * IP Restriction 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**"};
    }

    /**
     * API Key 인증 설정
     */
    @Getter
    @Setter
    public static class ApiKeyConfig {
        /**
         * API Key 인증 활성화 여부
         */
        private boolean enabled = false;

        /**
         * API Key 헤더 이름
         */
        private String headerName = "X-API-Key";

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**", "/public/**"};
    }

    /**
     * Circuit Breaker 설정
     */
    @Getter
    @Setter
    public static class CircuitBreakerConfig {
        /**
         * Circuit Breaker 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**"};

        /**
         * 실패 임계치 (이 값 이상이면 OPEN)
         */
        private int failureThreshold = 5;

        /**
         * 성공 임계치 (HALF_OPEN에서 이 값 이상이면 CLOSED)
         */
        private int successThreshold = 3;

        /**
         * OPEN 상태에서 HALF_OPEN으로 전환까지 대기 시간 (ms)
         */
        private long openTimeoutMs = 30000;
    }

    /**
     * Analytics 설정
     */
    @Getter
    @Setter
    public static class AnalyticsConfig {
        /**
         * Analytics 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**"};

        /**
         * 최대 저장 레코드 수 (인메모리)
         */
        private int maxRecords = 100000;
    }

    /**
     * Response Cache 설정
     */
    @Getter
    @Setter
    public static class CacheConfig {
        /**
         * Response Cache 활성화 여부
         */
        private boolean enabled = false;

        /**
         * 최대 캐시 항목 수
         */
        private int maxEntries = 10000;

        /**
         * 기본 TTL (초)
         */
        private int defaultTtlSeconds = 300;
    }

    /**
     * JWT Validation 설정
     */
    @Getter
    @Setter
    public static class JwtConfig {
        /**
         * JWT Validation 활성화 여부
         */
        private boolean enabled = false;

        /**
         * JWT Secret Key (HMAC 기반)
         */
        private String secretKey;

        /**
         * JWT 헤더 이름
         */
        private String headerName = "Authorization";

        /**
         * JWT 토큰 Prefix
         */
        private String tokenPrefix = "Bearer ";

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**", "/public/**", "/auth/**"};
    }

    /**
     * Bot Detection 설정
     */
    @Getter
    @Setter
    public static class BotDetectionConfig {
        /**
         * Bot Detection 활성화 여부
         */
        private boolean enabled = false;

        /**
         * Bot 차단 여부 (false이면 탐지만 하고 차단하지 않음)
         */
        private boolean blockBots = false;

        /**
         * 알 수 없는 Bot 차단 여부
         */
        private boolean blockUnknownBots = false;

        /**
         * 허용할 Bot 이름 목록
         */
        private Set<String> allowedBots = new HashSet<>();

        /**
         * 제외할 경로 패턴
         */
        private String[] excludePatterns = {"/actuator/**", "/health/**", "/robots.txt"};
    }
}
