package com.eraf.core.security.bot;

/**
 * 봇 탐지 방법
 */
public enum DetectionMethod {
    /** User-Agent 분석 */
    USER_AGENT,
    /** 행동 패턴 분석 */
    BEHAVIOR_PATTERN,
    /** 요청 핑거프린트 */
    REQUEST_FINGERPRINT,
    /** IP 평판 */
    IP_REPUTATION,
    /** 요청 빈도 패턴 */
    RATE_PATTERN
}
