package com.eraf.core.security.bot;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 봇 탐지 인터페이스
 */
public interface BotDetector {

    /**
     * 요청이 봇인지 탐지
     *
     * @param request HTTP 요청
     * @return 탐지 결과
     */
    BotDetectionResult detect(HttpServletRequest request);
}
