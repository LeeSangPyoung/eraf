package com.eraf.gateway.cache;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 캐시된 응답
 */
@Getter
@Builder
public class CachedResponse {

    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] body;
    private final String contentType;
    private final Instant cachedAt;
    private final Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public long getRemainingTtlSeconds() {
        long remaining = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
