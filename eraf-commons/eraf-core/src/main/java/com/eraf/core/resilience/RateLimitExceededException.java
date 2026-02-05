package com.eraf.core.resilience;

/**
 * Rate Limit 초과 시 발생하는 예외
 */
public class RateLimitExceededException extends RuntimeException {

    private final String rateLimiterName;
    private final double permitsPerSecond;

    public RateLimitExceededException(String rateLimiterName, double permitsPerSecond) {
        super("Rate limit exceeded for '" + rateLimiterName + "' (limit: " + permitsPerSecond + "/s)");
        this.rateLimiterName = rateLimiterName;
        this.permitsPerSecond = permitsPerSecond;
    }

    public String getRateLimiterName() {
        return rateLimiterName;
    }

    public double getPermitsPerSecond() {
        return permitsPerSecond;
    }
}
