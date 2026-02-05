package com.eraf.core.resilience;

/**
 * 재시도 횟수 소진 시 발생하는 예외
 */
public class RetryExhaustedException extends RuntimeException {

    private final int maxAttempts;
    private final int attemptsMade;

    public RetryExhaustedException(int maxAttempts, int attemptsMade, Throwable lastException) {
        super("Retry exhausted after " + attemptsMade + " attempts (max: " + maxAttempts + ")", lastException);
        this.maxAttempts = maxAttempts;
        this.attemptsMade = attemptsMade;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getAttemptsMade() {
        return attemptsMade;
    }

    public Throwable getLastException() {
        return getCause();
    }
}
