package com.eraf.gateway.exception;

import lombok.Getter;

/**
 * Rate Limit 초과 예외
 */
@Getter
public class RateLimitExceededException extends GatewayException {

    private final int retryAfterSeconds;
    private final int limit;
    private final int remaining;

    public RateLimitExceededException(int retryAfterSeconds, int limit) {
        super(GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = 0;
    }

    public RateLimitExceededException(int retryAfterSeconds, int limit, int remaining) {
        super(GatewayErrorCode.RATE_LIMIT_EXCEEDED);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = remaining;
    }
}
