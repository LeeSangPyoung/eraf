package com.eraf.core.resilience;

/**
 * 타임아웃 발생 시 예외
 */
public class TimeoutException extends RuntimeException {

    private final long timeoutMs;

    public TimeoutException(long timeoutMs) {
        super("Operation timed out after " + timeoutMs + "ms");
        this.timeoutMs = timeoutMs;
    }

    public TimeoutException(long timeoutMs, Throwable cause) {
        super("Operation timed out after " + timeoutMs + "ms", cause);
        this.timeoutMs = timeoutMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}
