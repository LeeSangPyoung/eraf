package com.eraf.gateway.cache.domain;

import com.eraf.core.utils.PathMatcher;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * 응답 캐싱 규칙
 */
@Getter
@Builder
public class CacheRule {

    private final String id;
    private final String pathPattern;
    private final Set<String> methods;  // GET, HEAD 등
    private final int ttlSeconds;
    private final boolean varyByQueryParams;
    private final boolean varyByHeaders;
    private final Set<String> varyHeaders;  // 캐시 키에 포함할 헤더
    private final boolean enabled;

    public boolean matchesPath(String path) {
        return PathMatcher.matches(path, pathPattern);
    }

    public boolean matchesMethod(String method) {
        return methods == null || methods.isEmpty() || methods.contains(method.toUpperCase());
    }
}
