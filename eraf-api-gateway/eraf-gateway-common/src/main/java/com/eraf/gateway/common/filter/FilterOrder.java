package com.eraf.gateway.common.filter;

import org.springframework.core.Ordered;

/**
 * Gateway 필터 실행 순서 정의
 */
public final class FilterOrder {

    private FilterOrder() {
    }

    // 기준점
    public static final int HIGHEST = Ordered.HIGHEST_PRECEDENCE;
    public static final int LOWEST = Ordered.LOWEST_PRECEDENCE;

    // 필터 순서
    public static final int BOT_DETECTION = HIGHEST + 5;
    public static final int RATE_LIMIT = HIGHEST + 10;
    public static final int VALIDATION = HIGHEST + 15;
    public static final int IP_RESTRICTION = HIGHEST + 20;
    public static final int API_KEY_AUTH = HIGHEST + 30;
    public static final int OAUTH2 = HIGHEST + 32;
    public static final int JWT_VALIDATION = HIGHEST + 35;
    public static final int CIRCUIT_BREAKER = HIGHEST + 40;
    public static final int RESPONSE_CACHE = HIGHEST + 50;
    public static final int REQUEST_TRANSFORM = HIGHEST + 60;
    public static final int RESPONSE_TRANSFORM = LOWEST - 20;
    public static final int ANALYTICS = LOWEST - 10;
}
