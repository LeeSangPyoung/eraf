package com.eraf.core.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 컬렉션 유틸리티 (null-safe)
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    // ===== Null/Empty 체크 =====

    /**
     * 컬렉션이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 컬렉션이 null이 아니고 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

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
     * 컬렉션 크기 반환 (null-safe)
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Map 크기 반환 (null-safe)
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    // ===== 조회 =====

    /**
     * 첫 번째 요소 반환
     */
    public static <T> T first(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        if (collection instanceof List) {
            return ((List<T>) collection).get(0);
        }
        return collection.iterator().next();
    }

    /**
     * 첫 번째 요소 반환 (기본값 지정)
     */
    public static <T> T first(Collection<T> collection, T defaultValue) {
        T first = first(collection);
        return first != null ? first : defaultValue;
    }

    /**
     * 마지막 요소 반환
     */
    public static <T> T last(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(list.size() - 1);
        }
        T last = null;
        for (T item : collection) {
            last = item;
        }
        return last;
    }

    /**
     * 마지막 요소 반환 (기본값 지정)
     */
    public static <T> T last(Collection<T> collection, T defaultValue) {
        T last = last(collection);
        return last != null ? last : defaultValue;
    }

    /**
     * 인덱스로 요소 가져오기 (null-safe)
     */
    public static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 인덱스로 요소 가져오기 (기본값 지정)
     */
    public static <T> T get(List<T> list, int index, T defaultValue) {
        T value = get(list, index);
        return value != null ? value : defaultValue;
    }

    // ===== 포함 여부 =====

    /**
     * 요소 포함 여부 확인 (null-safe)
     */
    public static <T> boolean contains(Collection<T> collection, T value) {
        if (collection == null) {
            return false;
        }
        return collection.contains(value);
    }

    /**
     * 모든 요소 포함 여부 확인
     */
    public static <T> boolean containsAll(Collection<T> collection, Collection<T> values) {
        if (collection == null || values == null) {
            return false;
        }
        return collection.containsAll(values);
    }

    /**
     * 하나라도 포함하는지 확인
     */
    public static <T> boolean containsAny(Collection<T> collection, Collection<T> values) {
        if (collection == null || values == null) {
            return false;
        }
        for (T value : values) {
            if (collection.contains(value)) {
                return true;
            }
        }
        return false;
    }

    // ===== 추가/제거 =====

    /**
     * 안전하게 요소 추가 (null이면 새 리스트 생성)
     */
    public static <T> List<T> addSafely(List<T> list, T element) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(element);
        return list;
    }

    /**
     * 안전하게 모든 요소 추가
     */
    public static <T> List<T> addAllSafely(List<T> list, Collection<T> elements) {
        if (list == null) {
            list = new ArrayList<>();
        }
        if (elements != null) {
            list.addAll(elements);
        }
        return list;
    }

    /**
     * null이 아닌 요소만 추가
     */
    public static <T> void addIfNotNull(Collection<T> collection, T element) {
        if (collection != null && element != null) {
            collection.add(element);
        }
    }

    /**
     * 조건에 맞는 요소만 제거
     */
    public static <T> List<T> remove(List<T> list, Predicate<T> predicate) {
        if (list == null) {
            return new ArrayList<>();
        }
        list.removeIf(predicate);
        return list;
    }

    // ===== 변환 =====

    /**
     * 리스트를 Set으로 변환
     */
    public static <T> Set<T> toSet(Collection<T> collection) {
        if (collection == null) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Set을 리스트로 변환
     */
    public static <T> List<T> toList(Collection<T> collection) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    /**
     * 배열을 리스트로 변환
     */
    @SafeVarargs
    public static <T> List<T> toList(T... array) {
        if (array == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * 컬렉션을 Map으로 변환 (keyMapper 사용)
     */
    public static <K, V> Map<K, V> toMap(Collection<V> collection, Function<V, K> keyMapper) {
        if (collection == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * 컬렉션을 Map으로 변환 (keyMapper, valueMapper 사용)
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection,
                                             Function<T, K> keyMapper,
                                             Function<T, V> valueMapper) {
        if (collection == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    // ===== 필터링 =====

    /**
     * 조건에 맞는 요소만 필터링
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * null이 아닌 요소만 필터링
     */
    public static <T> List<T> filterNotNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 중복 제거
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 특정 필드 기준으로 중복 제거
     */
    public static <T, U> List<T> distinctBy(Collection<T> collection, Function<T, U> keyExtractor) {
        if (collection == null) {
            return new ArrayList<>();
        }
        Set<U> seen = new HashSet<>();
        return collection.stream()
                .filter(e -> seen.add(keyExtractor.apply(e)))
                .collect(Collectors.toList());
    }

    // ===== 매핑 =====

    /**
     * 각 요소를 변환
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * flatMap 적용
     */
    public static <T, R> List<R> flatMap(Collection<T> collection, Function<T, Collection<R>> mapper) {
        if (collection == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .flatMap(item -> mapper.apply(item).stream())
                .collect(Collectors.toList());
    }

    // ===== 분할 =====

    /**
     * 리스트를 지정된 크기로 분할
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        if (size <= 0) {
            throw new IllegalArgumentException("분할 크기는 1 이상이어야 합니다");
        }

        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    // ===== 정렬 =====

    /**
     * 리스트 정렬 (오름차순)
     */
    public static <T extends Comparable<T>> List<T> sort(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * 리스트 정렬 (Comparator 사용)
     */
    public static <T> List<T> sort(List<T> list, Comparator<T> comparator) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> sorted = new ArrayList<>(list);
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * 리스트 역순 정렬
     */
    public static <T extends Comparable<T>> List<T> sortReverse(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> sorted = new ArrayList<>(list);
        sorted.sort(Collections.reverseOrder());
        return sorted;
    }

    /**
     * 리스트 순서 뒤집기
     */
    public static <T> List<T> reverse(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return reversed;
    }

    // ===== 집계 =====

    /**
     * 조건을 만족하는 요소 개수
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null) {
            return 0;
        }
        return collection.stream()
                .filter(predicate)
                .count();
    }

    /**
     * 모든 요소가 조건을 만족하는지 확인
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * 하나라도 조건을 만족하는지 확인
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * 모든 요소가 조건을 만족하지 않는지 확인
     */
    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }
        return collection.stream().noneMatch(predicate);
    }

    // ===== 조인 =====

    /**
     * 컬렉션을 문자열로 조인
     */
    public static <T> String join(Collection<T> collection, String delimiter) {
        if (collection == null) {
            return "";
        }
        return collection.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 컬렉션을 문자열로 조인 (prefix, suffix 포함)
     */
    public static <T> String join(Collection<T> collection, String delimiter, String prefix, String suffix) {
        if (collection == null) {
            return prefix + suffix;
        }
        return collection.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    // ===== 교집합/합집합/차집합 =====

    /**
     * 교집합
     */
    public static <T> Set<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        if (collection1 == null || collection2 == null) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        result.retainAll(collection2);
        return result;
    }

    /**
     * 합집합
     */
    public static <T> Set<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> result = new HashSet<>();
        if (collection1 != null) {
            result.addAll(collection1);
        }
        if (collection2 != null) {
            result.addAll(collection2);
        }
        return result;
    }

    /**
     * 차집합 (collection1 - collection2)
     */
    public static <T> Set<T> difference(Collection<T> collection1, Collection<T> collection2) {
        if (collection1 == null) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        if (collection2 != null) {
            result.removeAll(collection2);
        }
        return result;
    }

    // ===== 유틸리티 =====

    /**
     * 컬렉션을 셔플
     */
    public static <T> List<T> shuffle(List<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * 빈 리스트를 null로 변환
     */
    public static <T> List<T> emptyToNull(List<T> list) {
        return isEmpty(list) ? null : list;
    }

    /**
     * null을 빈 리스트로 변환
     */
    public static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * null을 빈 Set으로 변환
     */
    public static <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? new HashSet<>() : set;
    }

    /**
     * null을 빈 Map으로 변환
     */
    public static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }

    /**
     * 리스트의 특정 범위 추출 (안전)
     */
    public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(list.size(), toIndex);
        if (fromIndex >= toIndex) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }
}
