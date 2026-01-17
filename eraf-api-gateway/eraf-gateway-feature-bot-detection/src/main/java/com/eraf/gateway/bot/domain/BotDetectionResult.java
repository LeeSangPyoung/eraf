package com.eraf.gateway.bot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Bot 탐지 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotDetectionResult {

    /**
     * Bot으로 판단되었는지 여부
     */
    private boolean isBot;

    /**
     * Bot 유형 (CRAWLER, SCRAPER, AUTOMATED, KNOWN_BOT 등)
     */
    private BotType botType;

    /**
     * Bot 이름 (Googlebot, Bingbot 등)
     */
    private String botName;

    /**
     * 신뢰도 (0.0 ~ 1.0)
     */
    private double confidence;

    /**
     * 탐지 방법 (USER_AGENT, BEHAVIOR, FINGERPRINT 등)
     */
    private DetectionMethod detectionMethod;

    /**
     * 허용되는 Bot인지 여부
     */
    private boolean allowed;

    public enum BotType {
        SEARCH_ENGINE_CRAWLER,  // Google, Bing 등 검색엔진
        SOCIAL_MEDIA_BOT,       // Facebook, Twitter 등 소셜미디어
        MONITORING_BOT,         // Uptime 모니터링 등
        FEED_FETCHER,           // RSS 피드 수집기
        SCRAPER,                // 웹 스크래퍼
        AUTOMATED_TOOL,         // 자동화 도구
        MALICIOUS_BOT,          // 악성 봇
        UNKNOWN                 // 알 수 없음
    }

    public enum DetectionMethod {
        USER_AGENT,
        BEHAVIOR_PATTERN,
        REQUEST_FINGERPRINT,
        IP_REPUTATION,
        RATE_PATTERN
    }

    public static BotDetectionResult notBot() {
        return BotDetectionResult.builder()
                .isBot(false)
                .confidence(0.0)
                .allowed(true)
                .build();
    }

    public static BotDetectionResult allowedBot(BotType type, String name, double confidence, DetectionMethod method) {
        return BotDetectionResult.builder()
                .isBot(true)
                .botType(type)
                .botName(name)
                .confidence(confidence)
                .detectionMethod(method)
                .allowed(true)
                .build();
    }

    public static BotDetectionResult blockedBot(BotType type, String name, double confidence, DetectionMethod method) {
        return BotDetectionResult.builder()
                .isBot(true)
                .botType(type)
                .botName(name)
                .confidence(confidence)
                .detectionMethod(method)
                .allowed(false)
                .build();
    }
}
