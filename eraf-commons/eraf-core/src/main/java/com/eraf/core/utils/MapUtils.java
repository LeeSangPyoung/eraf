package com.eraf.core.utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Map 유틸리티 (null-safe)
 */
public final class MapUtils {

    private MapUtils() {
    }

    // ===== Null/Empty 체크 =====

    /**
     * Map이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Map이 null이 아니고 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    // ===== 크기 =====

    /**
     * Map 크기 반환 (null-safe)
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    // ===== 안전한 조회 =====

    /**
     * 키로 값 조회 (null-safe)
     */
    public static <K, V> V get(Map<K, V> map, K key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * 키로 값 조회 (기본값 지정)
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * String 값 조회
     */
    public static <K> String getString(Map<K, ?> map, K key) {
        return getString(map, key, null);
    }

    /**
     * String 값 조회 (기본값 지정)
     */
    public static <K> String getString(Map<K, ?> map, K key, String defaultValue) {
        Object value = get(map, key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    /**
     * Integer 값 조회
     */
    public static <K> Integer getInteger(Map<K, ?> map, K key) {
        return getInteger(map, key, null);
    }

    /**
     * Integer 값 조회 (기본값 지정)
     */
    public static <K> Integer getInteger(Map<K, ?> map, K key, Integer defaultValue) {
        Object value = get(map, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * int 값 조회 (primitive)
     */
    public static <K> int getInt(Map<K, ?> map, K key, int defaultValue) {
        Integer value = getInteger(map, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Long 값 조회
     */
    public static <K> Long getLong(Map<K, ?> map, K key) {
        return getLong(map, key, null);
    }

    /**
     * Long 값 조회 (기본값 지정)
     */
    public static <K> Long getLong(Map<K, ?> map, K key, Long defaultValue) {
        Object value = get(map, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Double 값 조회
     */
    public static <K> Double getDouble(Map<K, ?> map, K key) {
        return getDouble(map, key, null);
    }

    /**
     * Double 값 조회 (기본값 지정)
     */
    public static <K> Double getDouble(Map<K, ?> map, K key, Double defaultValue) {
        Object value = get(map, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Boolean 값 조회
     */
    public static <K> Boolean getBoolean(Map<K, ?> map, K key) {
        return getBoolean(map, key, null);
    }

    /**
     * Boolean 값 조회 (기본값 지정)
     */
    public static <K> Boolean getBoolean(Map<K, ?> map, K key, Boolean defaultValue) {
        Object value = get(map, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = String.valueOf(value);
        if ("true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "1".equals(str)) {
            return true;
        }
        if ("false".equalsIgnoreCase(str) || "no".equalsIgnoreCase(str) || "0".equals(str)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * BigDecimal 값 조회
     */
    public static <K> BigDecimal getBigDecimal(Map<K, ?> map, K key) {
        return getBigDecimal(map, key, null);
    }

    /**
     * BigDecimal 값 조회 (기본값 지정)
     */
    public static <K> BigDecimal getBigDecimal(Map<K, ?> map, K key, BigDecimal defaultValue) {
        Object value = get(map, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ===== 포함 여부 =====

    /**
     * 키가 포함되어 있는지 확인
     */
    public static <K> boolean containsKey(Map<K, ?> map, K key) {
        if (map == null) {
            return false;
        }
        return map.containsKey(key);
    }

    /**
     * 값이 포함되어 있는지 확인
     */
    public static <V> boolean containsValue(Map<?, V> map, V value) {
        if (map == null) {
            return false;
        }
        return map.containsValue(value);
    }

    /**
     * 모든 키가 포함되어 있는지 확인
     */
    @SafeVarargs
    public static <K> boolean containsAllKeys(Map<K, ?> map, K... keys) {
        if (map == null || keys == null) {
            return false;
        }
        for (K key : keys) {
            if (!map.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 하나라도 키가 포함되어 있는지 확인
     */
    @SafeVarargs
    public static <K> boolean containsAnyKey(Map<K, ?> map, K... keys) {
        if (map == null || keys == null) {
            return false;
        }
        for (K key : keys) {
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    // ===== 안전한 추가 =====

    /**
     * 안전하게 값 추가 (null이면 새 Map 생성)
     */
    public static <K, V> Map<K, V> putSafely(Map<K, V> map, K key, V value) {
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(key, value);
        return map;
    }

    /**
     * 값이 null이 아닌 경우에만 추가
     */
    public static <K, V> void putIfNotNull(Map<K, V> map, K key, V value) {
        if (map != null && value != null) {
            map.put(key, value);
        }
    }

    /**
     * 키가 없는 경우에만 추가
     */
    public static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
        if (map != null && !map.containsKey(key)) {
            map.put(key, value);
        }
    }

    /**
     * 모든 항목 추가
     */
    public static <K, V> void putAll(Map<K, V> target, Map<K, V> source) {
        if (target != null && source != null) {
            target.putAll(source);
        }
    }

    // ===== 변환 =====

    /**
     * Map의 키와 값을 뒤집기
     */
    public static <K, V> Map<V, K> invert(Map<K, V> map) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<V, K> inverted = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }

    /**
     * Map의 값을 변환
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> mapper) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<K, R> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return result;
    }

    /**
     * Map의 키를 변환
     */
    public static <K, V, R> Map<R, V> mapKeys(Map<K, V> map, Function<K, R> mapper) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<R, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(mapper.apply(entry.getKey()), entry.getValue());
        }
        return result;
    }

    /**
     * Map의 키와 값을 모두 변환
     */
    public static <K, V, RK, RV> Map<RK, RV> map(Map<K, V> map,
                                                   Function<K, RK> keyMapper,
                                                   Function<V, RV> valueMapper) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<RK, RV> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue()));
        }
        return result;
    }

    // ===== 필터링 =====

    /**
     * 조건에 맞는 항목만 필터링
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map,
                                           BiFunction<K, V, Boolean> predicate) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.apply(entry.getKey(), entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 특정 키들만 포함
     */
    @SafeVarargs
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, K... keys) {
        if (isEmpty(map) || keys == null || keys.length == 0) {
            return new HashMap<>();
        }
        Set<K> keySet = new HashSet<>(Arrays.asList(keys));
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (keySet.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * null이 아닌 값만 필터링
     */
    public static <K, V> Map<K, V> filterNotNullValues(Map<K, V> map) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // ===== 병합 =====

    /**
     * 여러 Map을 하나로 병합
     */
    @SafeVarargs
    public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
        Map<K, V> result = new HashMap<>();
        if (maps != null) {
            for (Map<K, V> map : maps) {
                if (map != null) {
                    result.putAll(map);
                }
            }
        }
        return result;
    }

    /**
     * 두 Map 병합 (충돌시 처리 함수 사용)
     */
    public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2,
                                          BiFunction<V, V, V> mergeFunction) {
        if (isEmpty(map1) && isEmpty(map2)) {
            return new HashMap<>();
        }
        if (isEmpty(map1)) {
            return new HashMap<>(map2);
        }
        if (isEmpty(map2)) {
            return new HashMap<>(map1);
        }

        Map<K, V> result = new HashMap<>(map1);
        for (Map.Entry<K, V> entry : map2.entrySet()) {
            result.merge(entry.getKey(), entry.getValue(), mergeFunction);
        }
        return result;
    }

    // ===== 정렬 =====

    /**
     * 키로 정렬 (LinkedHashMap 반환)
     */
    public static <K extends Comparable<K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        if (isEmpty(map)) {
            return new LinkedHashMap<>();
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll);
    }

    /**
     * 값으로 정렬 (LinkedHashMap 반환)
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
        if (isEmpty(map)) {
            return new LinkedHashMap<>();
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll);
    }

    /**
     * 값으로 역순 정렬
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValueReverse(Map<K, V> map) {
        if (isEmpty(map)) {
            return new LinkedHashMap<>();
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll);
    }

    // ===== 유틸리티 =====

    /**
     * null을 빈 Map으로 변환
     */
    public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }

    /**
     * 빈 Map을 null로 변환
     */
    public static <K, V> Map<K, V> emptyToNull(Map<K, V> map) {
        return isEmpty(map) ? null : map;
    }

    /**
     * Map 복제
     */
    public static <K, V> Map<K, V> copy(Map<K, V> map) {
        if (map == null) {
            return null;
        }
        return new HashMap<>(map);
    }

    /**
     * 불변 Map 생성
     */
    public static <K, V> Map<K, V> unmodifiable(Map<K, V> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Map을 문자열로 변환 (디버깅용)
     */
    public static <K, V> String toString(Map<K, V> map) {
        if (map == null) {
            return "null";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 간단한 Map 생성 헬퍼
     */
    public static <K, V> Map<K, V> of(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    /**
     * 간단한 Map 생성 헬퍼 (2쌍)
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * 간단한 Map 생성 헬퍼 (3쌍)
     */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
}
