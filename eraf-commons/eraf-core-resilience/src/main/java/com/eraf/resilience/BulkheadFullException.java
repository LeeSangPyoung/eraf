package com.eraf.resilience;

/**
 * Bulkhead가 가득 찼을 때 발생하는 예외
 */
public class BulkheadFullException extends RuntimeException {

    private final String bulkheadName;
    private final int maxConcurrent;

    public BulkheadFullException(String bulkheadName, int maxConcurrent) {
        super("Bulkhead '" + bulkheadName + "' is full (max concurrent: " + maxConcurrent + ")");
        this.bulkheadName = bulkheadName;
        this.maxConcurrent = maxConcurrent;
    }

    public String getBulkheadName() {
        return bulkheadName;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }
}
