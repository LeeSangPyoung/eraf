package com.eraf.core.converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Object ↔ Map 변환 유틸리티
 */
public final class MapConverter {

    private MapConverter() {
    }

    /**
     * 객체를 Map으로 변환
     */
    public static Map<String, Object> toMap(Object object) {
        if (object == null) {
            return null;
        }

        // Jackson을 통한 변환
        return JsonConverter.convert(object, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 객체를 Map으로 변환 (null 값 포함)
     */
    public static Map<String, Object> toMapIncludeNull(Object object) {
        if (object == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(object));
            } catch (IllegalAccessException e) {
                // skip
            }
        }

        return map;
    }

    /**
     * Map을 객체로 변환
     */
    public static <T> T fromMap(Map<String, ?> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return JsonConverter.convert(map, clazz);
    }

    /**
     * Map을 다른 Map으로 복사 (깊은 복사)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepCopy(Map<String, Object> source) {
        if (source == null) {
            return null;
        }

        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    /**
     * 두 Map을 병합
     */
    public static Map<String, Object> merge(Map<String, Object> base, Map<String, Object> override) {
        if (base == null) {
            return override != null ? new HashMap<>(override) : null;
        }
        if (override == null) {
            return new HashMap<>(base);
        }

        Map<String, Object> merged = new HashMap<>(base);
        merged.putAll(override);
        return merged;
    }
}
