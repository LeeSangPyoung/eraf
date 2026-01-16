package com.eraf.core.security.bot;

/**
 * 봇 유형
 */
public enum BotType {
    /** Google, Bing 등 검색엔진 */
    SEARCH_ENGINE_CRAWLER,
    /** Facebook, Twitter 등 소셜미디어 */
    SOCIAL_MEDIA_BOT,
    /** Uptime 모니터링 등 */
    MONITORING_BOT,
    /** RSS 피드 수집기 */
    FEED_FETCHER,
    /** 웹 스크래퍼 */
    SCRAPER,
    /** 자동화 도구 (curl, wget, postman 등) */
    AUTOMATED_TOOL,
    /** 악성 봇 */
    MALICIOUS_BOT,
    /** 알 수 없음 */
    UNKNOWN
}
