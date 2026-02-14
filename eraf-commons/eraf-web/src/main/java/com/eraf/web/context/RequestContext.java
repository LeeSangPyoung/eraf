package com.eraf.web.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 요청 컨텍스트 정보
 * 요청마다 생성되어 ThreadLocal에 저장되는 불변 객체
 */
public class RequestContext {

    private final String requestId;
    private final String traceId;
    private final String sessionId;
    private final String userId;
    private final String username;
    private final String clientIp;
    private final String userAgent;
    private final String method;
    private final String uri;
    private final String queryString;
    private final Locale locale;
    private final Instant requestTime;
    private final Map<String, String> headers;
    private final Map<String, Object> attributes;

    private RequestContext(Builder builder) {
        this.requestId = builder.requestId;
        this.traceId = builder.traceId;
        this.sessionId = builder.sessionId;
        this.userId = builder.userId;
        this.username = builder.username;
        this.clientIp = builder.clientIp;
        this.userAgent = builder.userAgent;
        this.method = builder.method;
        this.uri = builder.uri;
        this.queryString = builder.queryString;
        this.locale = builder.locale;
        this.requestTime = builder.requestTime;
        this.headers = Map.copyOf(builder.headers);
        this.attributes = Map.copyOf(builder.attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters

    public String getRequestId() {
        return requestId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getQueryString() {
        return queryString;
    }

    public Locale getLocale() {
        return locale;
    }

    public Instant getRequestTime() {
        return requestTime;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public static class Builder {
        private String requestId;
        private String traceId;
        private String sessionId;
        private String userId;
        private String username;
        private String clientIp;
        private String userAgent;
        private String method;
        private String uri;
        private String queryString;
        private Locale locale;
        private Instant requestTime = Instant.now();
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder requestTime(Instant requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder attribute(String name, Object value) {
            this.attributes.put(name, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "requestId='" + requestId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", userId='" + userId + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", clientIp='" + clientIp + '\'' +
                '}';
    }
}
