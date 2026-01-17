package com.eraf.gateway.analytics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Analytics 설정
 */
@Data
@ConfigurationProperties(prefix = "eraf.gateway.analytics")
public class AnalyticsProperties {

    /**
     * Analytics 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 데이터 보관 기간 (일)
     */
    private int retentionDays = 30;

    /**
     * 제외 패턴
     */
    private List<String> excludePatterns = new ArrayList<>();

    /**
     * 비동기 기록 여부
     */
    private boolean asyncRecording = true;

    /**
     * 배치 크기 (비동기 기록 시)
     */
    private int batchSize = 100;
}
