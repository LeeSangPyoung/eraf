package com.eraf.gateway.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bot Detection 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.bot-detection")
public class BotDetectionProperties {

    /**
     * Bot Detection 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 봇 차단 여부 (false인 경우 탐지만 하고 통과시킴)
     */
    private boolean blockBots = false;

    /**
     * 알 수 없는 봇 차단 여부
     */
    private boolean blockUnknownBots = false;

    /**
     * 허용된 봇 이름 목록
     */
    private Set<String> allowedBotNames = new HashSet<>();

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * Bot 정보 헤더 추가 여부 (디버깅용)
     */
    private boolean addBotHeaders = true;
}
