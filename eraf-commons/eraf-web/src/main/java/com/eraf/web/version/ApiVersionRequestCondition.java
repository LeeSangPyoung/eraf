package com.eraf.web.version;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

/**
 * API 버전 요청 조건
 */
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private final String version;
    private final VersionStrategy strategy;

    public ApiVersionRequestCondition(String version, VersionStrategy strategy) {
        this.version = version;
        this.strategy = strategy;
    }

    public String getVersion() {
        return version;
    }

    public VersionStrategy getStrategy() {
        return strategy;
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        // Method level annotation overrides class level
        return other != null ? other : this;
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        String requestVersion = extractVersion(request);

        if (requestVersion != null && requestVersion.equals(this.version)) {
            return this;
        }

        return null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        // Higher version has higher priority
        try {
            double thisVersion = Double.parseDouble(this.version.replaceAll("[^0-9.]", ""));
            double otherVersion = Double.parseDouble(other.version.replaceAll("[^0-9.]", ""));
            return Double.compare(otherVersion, thisVersion);
        } catch (NumberFormatException e) {
            return this.version.compareTo(other.version);
        }
    }

    private String extractVersion(HttpServletRequest request) {
        switch (strategy) {
            case URI:
                return extractFromUri(request.getRequestURI());
            case HEADER:
                return extractFromHeader(request);
            case PARAM:
                return request.getParameter("version");
            case MEDIA_TYPE:
                return extractFromMediaType(request.getHeader("Accept"));
            default:
                return null;
        }
    }

    private String extractFromUri(String uri) {
        // Match /v1/, /v2/, /api/v1/, etc.
        if (uri.contains("/v" + version + "/") || uri.endsWith("/v" + version)) {
            return version;
        }
        return null;
    }

    private String extractFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader("X-API-Version");
        if (headerValue == null) {
            headerValue = request.getHeader("API-Version");
        }
        return headerValue;
    }

    private String extractFromMediaType(String acceptHeader) {
        if (acceptHeader == null) {
            return null;
        }
        // Match application/vnd.myapi.v1+json pattern
        if (acceptHeader.contains(".v" + version + "+")) {
            return version;
        }
        return null;
    }
}
