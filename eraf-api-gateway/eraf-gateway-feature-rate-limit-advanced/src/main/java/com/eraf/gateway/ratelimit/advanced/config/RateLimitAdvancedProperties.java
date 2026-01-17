package com.eraf.gateway.ratelimit.advanced.config;

import com.eraf.gateway.ratelimit.advanced.domain.RateLimitAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 고급 Rate Limit 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.rate-limit-advanced")
public class RateLimitAdvancedProperties {

    /**
     * 고급 Rate Limit 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 기본 알고리즘
     */
    private RateLimitAlgorithm defaultAlgorithm = RateLimitAlgorithm.TOKEN_BUCKET;

    /**
     * 기본 최대 요청 수
     */
    private int defaultMaxRequests = 100;

    /**
     * 기본 윈도우 크기 (초)
     */
    private int defaultWindowSeconds = 60;

    /**
     * 기본 버스트 크기 (Token Bucket용)
     */
    private int defaultBurstSize = 150;

    /**
     * 기본 리필 속도 (초당 토큰 수)
     */
    private double defaultRefillRate = 10.0;

    /**
     * 분산 모드 활성화 (Redis 사용)
     */
    private boolean distributedMode = false;

    /**
     * Redis 설정
     */
    private RedisConfig redis = new RedisConfig();

    /**
     * 경로별 알고리즘 오버라이드
     * 예: "/api/v1/*" -> TOKEN_BUCKET
     */
    private Map<String, RateLimitAlgorithm> pathAlgorithms = new HashMap<>();

    /**
     * 경로별 제한 오버라이드
     * 예: "/api/v1/upload" -> 10 (requests per window)
     */
    private Map<String, Integer> pathLimits = new HashMap<>();

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Consumer별 제한 설정
     */
    private Map<String, ConsumerConfig> consumers = new HashMap<>();

    /**
     * 헤더 기반 제한 설정
     */
    private Map<String, Integer> headerLimits = new HashMap<>();

    /**
     * Redis 설정
     */
    @Data
    public static class RedisConfig {
        /**
         * Redis 호스트
         */
        private String host = "localhost";

        /**
         * Redis 포트
         */
        private int port = 6379;

        /**
         * Redis 비밀번호
         */
        private String password;

        /**
         * Redis 데이터베이스 인덱스
         */
        private int database = 0;

        /**
         * 연결 타임아웃 (밀리초)
         */
        private int timeout = 2000;

        /**
         * 클러스터 모드 활성화
         */
        private boolean cluster = false;

        /**
         * 클러스터 노드 목록
         */
        private List<String> clusterNodes = new ArrayList<>();
    }

    /**
     * Consumer별 설정
     */
    @Data
    public static class ConsumerConfig {
        /**
         * Consumer ID
         */
        private String id;

        /**
         * 최대 요청 수
         */
        private int maxRequests = 100;

        /**
         * 버스트 크기
         */
        private int burstSize = 150;

        /**
         * 리필 속도
         */
        private double refillRate = 10.0;

        /**
         * 알고리즘
         */
        private RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;
    }
}
