package com.eraf.core.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 객체 유틸리티 (null-safe)
 */
public final class ObjectUtils {

    private ObjectUtils() {
    }

    // ===== Null 체크 =====

    /**
     * null인지 확인
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * null이 아닌지 확인
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 모든 객체가 null인지 확인
     */
    public static boolean allNull(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }
        for (Object obj : objects) {
            if (obj != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 하나라도 null인지 확인
     */
    public static boolean anyNull(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }
        for (Object obj : objects) {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 모든 객체가 null이 아닌지 확인
     */
    public static boolean allNotNull(Object... objects) {
        return !anyNull(objects);
    }

    /**
     * 모든 객체가 null이 아니고 비어있지 않은지 확인
     */
    public static boolean allNotEmpty(Object... objects) {
        if (objects == null || objects.length == 0) {
            return false;
        }
        for (Object obj : objects) {
            if (isEmpty(obj)) {
                return true;
            }
        }
        return false;
    }

    // ===== 기본값 처리 =====

    /**
     * null이면 기본값 반환
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * null이면 Supplier로 기본값 생성
     */
    public static <T> T defaultIfNull(T value, Supplier<T> defaultSupplier) {
        return value != null ? value : defaultSupplier.get();
    }

    /**
     * 비어있으면 기본값 반환
     */
    public static <T> T defaultIfEmpty(T value, T defaultValue) {
        return !isEmpty(value) ? value : defaultValue;
    }

    /**
     * 첫 번째 null이 아닌 값 반환
     */
    @SafeVarargs
    public static <T> T firstNonNull(T... values) {
        if (values != null) {
            for (T value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    // ===== 비교 =====

    /**
     * null-safe equals
     */
    public static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * null-safe deepEquals (배열 포함)
     */
    public static boolean deepEquals(Object a, Object b) {
        return Objects.deepEquals(a, b);
    }

    /**
     * 동일한 객체인지 확인 (==)
     */
    public static boolean isSame(Object a, Object b) {
        return a == b;
    }

    /**
     * 다른 객체인지 확인 (!=)
     */
    public static boolean notSame(Object a, Object b) {
        return a != b;
    }

    // ===== HashCode =====

    /**
     * null-safe hashCode
     */
    public static int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    /**
     * 여러 객체의 hashCode 계산
     */
    public static int hash(Object... values) {
        return Objects.hash(values);
    }

    // ===== ToString =====

    /**
     * null-safe toString
     */
    public static String toString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * null-safe toString (기본값 지정)
     */
    public static String toString(Object obj, String defaultValue) {
        return obj == null ? defaultValue : obj.toString();
    }

    /**
     * null이면 빈 문자열 반환
     */
    public static String toStringOrEmpty(Object obj) {
        return toString(obj, "");
    }

    // ===== 타입 체크 =====

    /**
     * 특정 타입인지 확인
     */
    public static boolean isInstanceOf(Object obj, Class<?> type) {
        if (obj == null || type == null) {
            return false;
        }
        return type.isInstance(obj);
    }

    /**
     * 여러 타입 중 하나인지 확인
     */
    public static boolean isInstanceOfAny(Object obj, Class<?>... types) {
        if (obj == null || types == null || types.length == 0) {
            return false;
        }
        for (Class<?> type : types) {
            if (type != null && type.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }

    // ===== 타입 변환 =====

    /**
     * 안전한 타입 캐스팅
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> type) {
        if (obj == null || type == null) {
            return null;
        }
        if (type.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 안전한 타입 캐스팅 (기본값 지정)
     */
    public static <T> T cast(Object obj, Class<T> type, T defaultValue) {
        T result = cast(obj, type);
        return result != null ? result : defaultValue;
    }

    // ===== 복제 =====

    /**
     * Cloneable 객체 복제
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            try {
                return (T) obj.getClass().getMethod("clone").invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ===== Optional =====

    /**
     * Optional로 감싸기
     */
    public static <T> Optional<T> optional(T value) {
        return Optional.ofNullable(value);
    }

    /**
     * Optional에서 값 가져오기 (기본값 지정)
     */
    public static <T> T getOrDefault(Optional<T> optional, T defaultValue) {
        return optional != null ? optional.orElse(defaultValue) : defaultValue;
    }

    // ===== Empty 체크 (확장) =====

    /**
     * 객체가 비어있는지 확인 (null, 빈 문자열, 빈 컬렉션 등)
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof java.util.Collection) {
            return ((java.util.Collection<?>) obj).isEmpty();
        }
        if (obj instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(obj) == 0;
        }
        if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        }
        return false;
    }

    /**
     * 객체가 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    // ===== 최대/최소 =====

    /**
     * 두 Comparable 객체 중 최대값
     */
    public static <T extends Comparable<T>> T max(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    /**
     * 두 Comparable 객체 중 최소값
     */
    public static <T extends Comparable<T>> T min(T a, T b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * 여러 Comparable 객체 중 최대값
     */
    @SafeVarargs
    public static <T extends Comparable<T>> T max(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        T max = null;
        for (T value : values) {
            if (value != null) {
                if (max == null || value.compareTo(max) > 0) {
                    max = value;
                }
            }
        }
        return max;
    }

    /**
     * 여러 Comparable 객체 중 최소값
     */
    @SafeVarargs
    public static <T extends Comparable<T>> T min(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        T min = null;
        for (T value : values) {
            if (value != null) {
                if (min == null || value.compareTo(min) < 0) {
                    min = value;
                }
            }
        }
        return min;
    }

    // ===== 범위 체크 =====

    /**
     * 값이 범위 내에 있는지 확인
     */
    public static <T extends Comparable<T>> boolean between(T value, T start, T end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
    }

    /**
     * 값을 범위 내로 제한
     */
    public static <T extends Comparable<T>> T clamp(T value, T min, T max) {
        if (value == null) {
            return min;
        }
        if (min != null && value.compareTo(min) < 0) {
            return min;
        }
        if (max != null && value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    // ===== 유틸리티 =====

    /**
     * null이 아니면 특정 동작 수행
     */
    public static <T> void ifNotNull(T value, java.util.function.Consumer<T> action) {
        if (value != null && action != null) {
            action.accept(value);
        }
    }

    /**
     * null이면 특정 동작 수행
     */
    public static void ifNull(Object value, Runnable action) {
        if (value == null && action != null) {
            action.run();
        }
    }

    /**
     * 클래스 이름 반환 (null-safe)
     */
    public static String getClassName(Object obj) {
        return obj == null ? null : obj.getClass().getName();
    }

    /**
     * 간단한 클래스 이름 반환 (null-safe)
     */
    public static String getSimpleClassName(Object obj) {
        return obj == null ? null : obj.getClass().getSimpleName();
    }

    /**
     * 객체의 identity HashCode 반환
     */
    public static int identityHashCode(Object obj) {
        return System.identityHashCode(obj);
    }

    /**
     * 두 객체가 같은 인스턴스인지 확인
     */
    public static boolean isSameInstance(Object a, Object b) {
        return a == b;
    }

    /**
     * 객체를 문자열로 표현 (디버깅용)
     */
    public static String toDebugString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }
}
