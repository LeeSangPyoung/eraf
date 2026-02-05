package com.eraf.actuator.tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 트레이스 컨텍스트
 */
public class TraceContext {

    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private final long startTime;
    private final Map<String, String> baggage;

    private TraceContext(Builder builder) {
        this.traceId = builder.traceId;
        this.spanId = builder.spanId;
        this.parentSpanId = builder.parentSpanId;
        this.startTime = builder.startTime;
        this.baggage = builder.baggage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TraceContext create() {
        return builder()
                .traceId(generateId())
                .spanId(generateId())
                .startTime(System.currentTimeMillis())
                .build();
    }

    public static TraceContext createChild(TraceContext parent) {
        return builder()
                .traceId(parent.getTraceId())
                .spanId(generateId())
                .parentSpanId(parent.getSpanId())
                .startTime(System.currentTimeMillis())
                .baggage(parent.getBaggage())
                .build();
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // Getters

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public long getStartTime() {
        return startTime;
    }

    public Map<String, String> getBaggage() {
        return baggage;
    }

    public String getBaggage(String key) {
        return baggage.get(key);
    }

    public long getDurationMs() {
        return System.currentTimeMillis() - startTime;
    }

    public static class Builder {
        private String traceId;
        private String spanId;
        private String parentSpanId;
        private long startTime = System.currentTimeMillis();
        private Map<String, String> baggage = new HashMap<>();

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder baggage(Map<String, String> baggage) {
            if (baggage != null) {
                this.baggage.putAll(baggage);
            }
            return this;
        }

        public Builder addBaggage(String key, String value) {
            this.baggage.put(key, value);
            return this;
        }

        public TraceContext build() {
            return new TraceContext(this);
        }
    }
}
