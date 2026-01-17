package com.eraf.core.utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 배열 유틸리티 (null-safe)
 */
public final class ArrayUtils {

    private ArrayUtils() {
    }

    // ===== Null/Empty 체크 =====

    /**
     * 배열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 배열이 null이 아니고 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * int 배열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * long 배열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * double 배열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * boolean 배열이 null이거나 비어있는지 확인
     */
    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    // ===== 크기 =====

    /**
     * 배열 크기 반환 (null-safe)
     */
    public static int length(Object[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * int 배열 크기
     */
    public static int length(int[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * long 배열 크기
     */
    public static int length(long[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * double 배열 크기
     */
    public static int length(double[] array) {
        return array == null ? 0 : array.length;
    }

    // ===== 포함 여부 =====

    /**
     * 배열에 요소가 포함되어 있는지 확인
     */
    public static <T> boolean contains(T[] array, T value) {
        if (isEmpty(array)) {
            return false;
        }
        for (T element : array) {
            if (Objects.equals(element, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * int 배열에 값이 포함되어 있는지 확인
     */
    public static boolean contains(int[] array, int value) {
        if (isEmpty(array)) {
            return false;
        }
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * long 배열에 값이 포함되어 있는지 확인
     */
    public static boolean contains(long[] array, long value) {
        if (isEmpty(array)) {
            return false;
        }
        for (long element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * double 배열에 값이 포함되어 있는지 확인
     */
    public static boolean contains(double[] array, double value) {
        if (isEmpty(array)) {
            return false;
        }
        for (double element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    // ===== 인덱스 찾기 =====

    /**
     * 요소의 인덱스 반환 (없으면 -1)
     */
    public static <T> int indexOf(T[] array, T value) {
        if (isEmpty(array)) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * int 배열에서 값의 인덱스 반환
     */
    public static int indexOf(int[] array, int value) {
        if (isEmpty(array)) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 마지막 인덱스 반환
     */
    public static <T> int lastIndexOf(T[] array, T value) {
        if (isEmpty(array)) {
            return -1;
        }
        for (int i = array.length - 1; i >= 0; i--) {
            if (Objects.equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }

    // ===== 조회 =====

    /**
     * 첫 번째 요소 반환
     */
    public static <T> T first(T[] array) {
        return isEmpty(array) ? null : array[0];
    }

    /**
     * 마지막 요소 반환
     */
    public static <T> T last(T[] array) {
        return isEmpty(array) ? null : array[array.length - 1];
    }

    /**
     * 인덱스로 요소 가져오기 (null-safe)
     */
    public static <T> T get(T[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return null;
        }
        return array[index];
    }

    /**
     * 인덱스로 요소 가져오기 (기본값 지정)
     */
    public static <T> T get(T[] array, int index, T defaultValue) {
        T value = get(array, index);
        return value != null ? value : defaultValue;
    }

    // ===== 배열 생성 =====

    /**
     * 빈 배열 생성
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] empty(Class<T> type) {
        return (T[]) Array.newInstance(type, 0);
    }

    /**
     * 지정된 크기의 배열 생성
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int size) {
        return (T[]) Array.newInstance(type, size);
    }

    /**
     * 요소들로 배열 생성
     */
    @SafeVarargs
    public static <T> T[] of(T... elements) {
        return elements;
    }

    // ===== 추가/제거 =====

    /**
     * 배열 끝에 요소 추가
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, T element) {
        if (array == null) {
            T[] result = (T[]) Array.newInstance(element.getClass(), 1);
            result[0] = element;
            return result;
        }
        T[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }

    /**
     * 배열의 특정 인덱스에 요소 추가
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, int index, T element) {
        if (array == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            T[] result = (T[]) Array.newInstance(element.getClass(), 1);
            result[0] = element;
            return result;
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }

    /**
     * 배열에서 특정 인덱스의 요소 제거
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(T[] array, int index) {
        if (isEmpty(array) || index < 0 || index >= array.length) {
            return array;
        }
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    /**
     * 배열에서 특정 요소 제거 (첫 번째 일치)
     */
    public static <T> T[] removeElement(T[] array, T element) {
        int index = indexOf(array, element);
        if (index == -1) {
            return array;
        }
        return remove(array, index);
    }

    // ===== 변환 =====

    /**
     * 배열을 List로 변환
     */
    public static <T> List<T> toList(T[] array) {
        if (isEmpty(array)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * int 배열을 List로 변환
     */
    public static List<Integer> toList(int[] array) {
        if (isEmpty(array)) {
            return new ArrayList<>();
        }
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    /**
     * long 배열을 List로 변환
     */
    public static List<Long> toList(long[] array) {
        if (isEmpty(array)) {
            return new ArrayList<>();
        }
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    /**
     * double 배열을 List로 변환
     */
    public static List<Double> toList(double[] array) {
        if (isEmpty(array)) {
            return new ArrayList<>();
        }
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    /**
     * 배열을 Set으로 변환
     */
    public static <T> Set<T> toSet(T[] array) {
        if (isEmpty(array)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * Integer 배열을 int 배열로 변환
     */
    public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return null;
        }
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0;
        }
        return result;
    }

    /**
     * int 배열을 Integer 배열로 변환
     */
    public static Integer[] toObject(int[] array) {
        if (array == null) {
            return null;
        }
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Long 배열을 long 배열로 변환
     */
    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0L;
        }
        return result;
    }

    /**
     * long 배열을 Long 배열로 변환
     */
    public static Long[] toObject(long[] array) {
        if (array == null) {
            return null;
        }
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    // ===== 병합 =====

    /**
     * 여러 배열을 하나로 병합
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }
        int totalLength = 0;
        for (T[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        if (totalLength == 0) {
            return (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), 0);
        }

        T[] result = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), totalLength);
        int offset = 0;
        for (T[] array : arrays) {
            if (array != null && array.length > 0) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

    // ===== 서브 배열 =====

    /**
     * 서브 배열 추출
     */
    public static <T> T[] subarray(T[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        startIndex = Math.max(0, startIndex);
        endIndex = Math.min(array.length, endIndex);
        if (startIndex >= endIndex) {
            return empty((Class<T>) array.getClass().getComponentType());
        }
        return Arrays.copyOfRange(array, startIndex, endIndex);
    }

    // ===== 정렬 =====

    /**
     * 배열 정렬 (원본 유지)
     */
    public static <T extends Comparable<T>> T[] sort(T[] array) {
        if (array == null) {
            return null;
        }
        T[] result = Arrays.copyOf(array, array.length);
        Arrays.sort(result);
        return result;
    }

    /**
     * int 배열 정렬 (원본 유지)
     */
    public static int[] sort(int[] array) {
        if (array == null) {
            return null;
        }
        int[] result = Arrays.copyOf(array, array.length);
        Arrays.sort(result);
        return result;
    }

    /**
     * 배열 역순 정렬
     */
    public static <T extends Comparable<T>> T[] sortReverse(T[] array) {
        if (array == null) {
            return null;
        }
        T[] result = Arrays.copyOf(array, array.length);
        Arrays.sort(result, Collections.reverseOrder());
        return result;
    }

    /**
     * 배열 순서 뒤집기
     */
    public static <T> T[] reverse(T[] array) {
        if (array == null) {
            return null;
        }
        T[] result = Arrays.copyOf(array, array.length);
        for (int i = 0; i < result.length / 2; i++) {
            T temp = result[i];
            result[i] = result[result.length - 1 - i];
            result[result.length - 1 - i] = temp;
        }
        return result;
    }

    /**
     * int 배열 순서 뒤집기
     */
    public static int[] reverse(int[] array) {
        if (array == null) {
            return null;
        }
        int[] result = Arrays.copyOf(array, array.length);
        for (int i = 0; i < result.length / 2; i++) {
            int temp = result[i];
            result[i] = result[result.length - 1 - i];
            result[result.length - 1 - i] = temp;
        }
        return result;
    }

    // ===== 유틸리티 =====

    /**
     * 배열을 문자열로 조인
     */
    public static <T> String join(T[] array, String delimiter) {
        if (isEmpty(array)) {
            return "";
        }
        return Arrays.stream(array)
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * int 배열을 문자열로 조인
     */
    public static String join(int[] array, String delimiter) {
        if (isEmpty(array)) {
            return "";
        }
        return Arrays.stream(array)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 배열 복제
     */
    public static <T> T[] clone(T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * int 배열 복제
     */
    public static int[] clone(int[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 배열이 같은지 비교
     */
    public static <T> boolean equals(T[] array1, T[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * int 배열이 같은지 비교
     */
    public static boolean equals(int[] array1, int[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * 배열을 문자열로 변환 (디버깅용)
     */
    public static String toString(Object[] array) {
        return Arrays.toString(array);
    }

    /**
     * int 배열을 문자열로 변환
     */
    public static String toString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * null을 빈 배열로 변환
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] nullToEmpty(T[] array, Class<T> type) {
        return array == null ? (T[]) Array.newInstance(type, 0) : array;
    }

    /**
     * 빈 배열을 null로 변환
     */
    public static <T> T[] emptyToNull(T[] array) {
        return isEmpty(array) ? null : array;
    }
}
