package com.eraf.gateway.ratelimit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Rate Limit 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.rate-limit")
public class RateLimitProperties {

    /**
     * Rate Limit 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 기본 초당 요청 제한 수
     */
    private int defaultLimitPerSecond = 100;

    /**
     * 기본 윈도우 크기 (초)
     */
    private int defaultWindowSeconds = 60;

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * 버스트 허용 여부
     */
    private boolean burstAllowed = false;

    /**
     * 버스트 최대 배율 (기본 제한의 N배)
     */
    private double burstMultiplier = 1.5;
}
