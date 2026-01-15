package com.eraf.core.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동적 설정 관리
 */
@Component
public class Config {

    private final Map<String, Object> configs = new ConcurrentHashMap<>();

    /**
     * 설정 값 조회
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) configs.get(key));
    }

    /**
     * 설정 값 조회 (기본값)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = configs.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 문자열 설정 조회
     */
    public String getString(String key) {
        Object value = configs.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 문자열 설정 조회 (기본값)
     */
    public String getString(String key, String defaultValue) {
        Object value = configs.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    /**
     * 정수 설정 조회
     */
    public Integer getInt(String key) {
        Object value = configs.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    /**
     * 정수 설정 조회 (기본값)
     */
    public int getInt(String key, int defaultValue) {
        Integer value = getInt(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Long 설정 조회
     */
    public Long getLong(String key) {
        Object value = configs.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    /**
     * Long 설정 조회 (기본값)
     */
    public long getLong(String key, long defaultValue) {
        Long value = getLong(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 불린 설정 조회
     */
    public Boolean getBoolean(String key) {
        Object value = configs.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * 불린 설정 조회 (기본값)
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 설정 저장
     */
    public void set(String key, Object value) {
        if (value == null) {
            configs.remove(key);
        } else {
            configs.put(key, value);
        }
    }

    /**
     * 설정 삭제
     */
    public void remove(String key) {
        configs.remove(key);
    }

    /**
     * 설정 존재 여부
     */
    public boolean contains(String key) {
        return configs.containsKey(key);
    }

    /**
     * 모든 설정 조회
     */
    public Map<String, Object> getAll() {
        return Map.copyOf(configs);
    }

    /**
     * 모든 설정 초기화
     */
    public void clear() {
        configs.clear();
    }

    /**
     * 여러 설정 일괄 저장
     */
    public void setAll(Map<String, Object> settings) {
        configs.putAll(settings);
    }
}
