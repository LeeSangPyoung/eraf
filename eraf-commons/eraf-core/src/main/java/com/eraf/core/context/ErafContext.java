package com.eraf.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ERAF 컨텍스트
 * 요청 전체에서 공유되는 정보 관리
 */
public class ErafContext {

    private String traceId;
    private String requestId;
    private String userId;
    private String username;
    private String clientIp;
    private String userAgent;
    private Map<String, Object> attributes = new HashMap<>();

    // Static accessor methods (ThreadLocal 기반)
    public static String getCurrentUserId() {
        return ErafContextHolder.getContext().getUserId();
    }

    public static String getCurrentUsername() {
        return ErafContextHolder.getContext().getUsername();
    }

    public static String getTraceId() {
        return ErafContextHolder.getContext().traceId;
    }

    public static String getRequestId() {
        return ErafContextHolder.getContext().requestId;
    }

    public static String getClientIp() {
        return ErafContextHolder.getContext().clientIp;
    }

    public static Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(ErafContextHolder.getContext().attributes.get(key));
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getAttribute(String key, Class<T> type) {
        Object value = ErafContextHolder.getContext().attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    // Instance getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    public void clear() {
        this.traceId = null;
        this.requestId = null;
        this.userId = null;
        this.username = null;
        this.clientIp = null;
        this.userAgent = null;
        this.attributes.clear();
    }
}
