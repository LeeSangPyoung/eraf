package com.eraf.core.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enum 유틸리티
 * DB 코드값과 Enum 매핑에 필수적인 기능 제공
 */
public final class EnumUtils {

    private EnumUtils() {
    }

    // ===== 이름으로 찾기 =====

    /**
     * Enum 이름으로 찾기 (대소문자 구분)
     */
    public static <E extends Enum<E>> E fromName(Class<E> enumClass, String name) {
        if (enumClass == null || name == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Enum 이름으로 찾기 (대소문자 무시)
     */
    public static <E extends Enum<E>> E fromNameIgnoreCase(Class<E> enumClass, String name) {
        if (enumClass == null || name == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(name)) {
                return enumConstant;
            }
        }
        return null;
    }

    /**
     * Enum 이름으로 찾기 (기본값 지정)
     */
    public static <E extends Enum<E>> E fromName(Class<E> enumClass, String name, E defaultValue) {
        E result = fromName(enumClass, name);
        return result != null ? result : defaultValue;
    }

    // ===== 코드로 찾기 (가장 많이 씀!) =====

    /**
     * 코드로 Enum 찾기
     * 예: UserStatus.fromCode("A") -> UserStatus.ACTIVE
     *
     * @param enumClass Enum 클래스
     * @param code 코드값
     * @param codeExtractor Enum에서 코드를 추출하는 함수 (예: UserStatus::getCode)
     */
    public static <E extends Enum<E>, C> E fromCode(Class<E> enumClass, C code,
                                                      Function<E, C> codeExtractor) {
        if (enumClass == null || code == null || codeExtractor == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (code.equals(codeExtractor.apply(enumConstant))) {
                return enumConstant;
            }
        }
        return null;
    }

    /**
     * 코드로 Enum 찾기 (기본값 지정)
     */
    public static <E extends Enum<E>, C> E fromCode(Class<E> enumClass, C code,
                                                      Function<E, C> codeExtractor,
                                                      E defaultValue) {
        E result = fromCode(enumClass, code, codeExtractor);
        return result != null ? result : defaultValue;
    }

    /**
     * 문자열 코드로 Enum 찾기 (대소문자 무시)
     */
    public static <E extends Enum<E>> E fromCodeIgnoreCase(Class<E> enumClass, String code,
                                                             Function<E, String> codeExtractor) {
        if (enumClass == null || code == null || codeExtractor == null) {
            return null;
        }
        for (E enumConstant : enumClass.getEnumConstants()) {
            String enumCode = codeExtractor.apply(enumConstant);
            if (enumCode != null && enumCode.equalsIgnoreCase(code)) {
                return enumConstant;
            }
        }
        return null;
    }

    // ===== ordinal로 찾기 =====

    /**
     * ordinal(순서)로 Enum 찾기
     */
    public static <E extends Enum<E>> E fromOrdinal(Class<E> enumClass, int ordinal) {
        if (enumClass == null) {
            return null;
        }
        E[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            return null;
        }
        return constants[ordinal];
    }

    /**
     * ordinal로 Enum 찾기 (기본값 지정)
     */
    public static <E extends Enum<E>> E fromOrdinal(Class<E> enumClass, int ordinal, E defaultValue) {
        E result = fromOrdinal(enumClass, ordinal);
        return result != null ? result : defaultValue;
    }

    // ===== 리스트/배열 변환 =====

    /**
     * Enum 전체를 List로 변환
     */
    public static <E extends Enum<E>> List<E> toList(Class<E> enumClass) {
        if (enumClass == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(enumClass.getEnumConstants());
    }

    /**
     * Enum 전체를 Set으로 변환
     */
    public static <E extends Enum<E>> Set<E> toSet(Class<E> enumClass) {
        if (enumClass == null) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(enumClass.getEnumConstants()));
    }

    /**
     * Enum 이름 리스트 반환
     */
    public static <E extends Enum<E>> List<String> getNames(Class<E> enumClass) {
        if (enumClass == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // ===== Map 변환 =====

    /**
     * Enum을 Map으로 변환 (코드 → Enum)
     * 예: {​"A": UserStatus.ACTIVE, "I": UserStatus.INACTIVE}
     */
    public static <E extends Enum<E>, K> Map<K, E> toMap(Class<E> enumClass,
                                                           Function<E, K> keyExtractor) {
        if (enumClass == null || keyExtractor == null) {
            return new HashMap<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(keyExtractor, Function.identity()));
    }

    /**
     * Enum을 Map으로 변환 (코드 → 값)
     * 예: {​"A": "활성", "I": "비활성"}
     */
    public static <E extends Enum<E>, K, V> Map<K, V> toMap(Class<E> enumClass,
                                                              Function<E, K> keyExtractor,
                                                              Function<E, V> valueExtractor) {
        if (enumClass == null || keyExtractor == null || valueExtractor == null) {
            return new HashMap<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(keyExtractor, valueExtractor));
    }

    /**
     * Enum 이름을 키로 하는 Map 생성
     */
    public static <E extends Enum<E>> Map<String, E> toMapByName(Class<E> enumClass) {
        return toMap(enumClass, Enum::name);
    }

    /**
     * Enum ordinal을 키로 하는 Map 생성
     */
    public static <E extends Enum<E>> Map<Integer, E> toMapByOrdinal(Class<E> enumClass) {
        return toMap(enumClass, Enum::ordinal);
    }

    // ===== 검증 =====

    /**
     * 유효한 Enum 값인지 확인
     */
    public static <E extends Enum<E>> boolean isValid(Class<E> enumClass, String name) {
        return fromName(enumClass, name) != null;
    }

    /**
     * 유효한 Enum 값인지 확인 (대소문자 무시)
     */
    public static <E extends Enum<E>> boolean isValidIgnoreCase(Class<E> enumClass, String name) {
        return fromNameIgnoreCase(enumClass, name) != null;
    }

    /**
     * 유효한 코드값인지 확인
     */
    public static <E extends Enum<E>, C> boolean isValidCode(Class<E> enumClass, C code,
                                                               Function<E, C> codeExtractor) {
        return fromCode(enumClass, code, codeExtractor) != null;
    }

    // ===== 개수 =====

    /**
     * Enum 상수 개수 반환
     */
    public static <E extends Enum<E>> int count(Class<E> enumClass) {
        if (enumClass == null) {
            return 0;
        }
        return enumClass.getEnumConstants().length;
    }

    // ===== 비교 =====

    /**
     * 두 Enum이 같은지 비교 (null-safe)
     */
    public static <E extends Enum<E>> boolean equals(E enum1, E enum2) {
        if (enum1 == null && enum2 == null) {
            return true;
        }
        if (enum1 == null || enum2 == null) {
            return false;
        }
        return enum1.equals(enum2);
    }

    /**
     * Enum이 주어진 값들 중 하나인지 확인
     */
    @SafeVarargs
    public static <E extends Enum<E>> boolean isOneOf(E enumValue, E... values) {
        if (enumValue == null || values == null || values.length == 0) {
            return false;
        }
        for (E value : values) {
            if (enumValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enum이 주어진 값들에 포함되지 않는지 확인
     */
    @SafeVarargs
    public static <E extends Enum<E>> boolean isNotOneOf(E enumValue, E... values) {
        return !isOneOf(enumValue, values);
    }

    // ===== 필터링 =====

    /**
     * 조건에 맞는 Enum만 필터링
     */
    public static <E extends Enum<E>> List<E> filter(Class<E> enumClass,
                                                       java.util.function.Predicate<E> predicate) {
        if (enumClass == null || predicate == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    // ===== API 응답용 DTO 변환 =====

    /**
     * Enum을 코드-라벨 쌍의 Map 리스트로 변환 (API 응답용)
     * 예: [{​code: "A", label: "활성"}, {​code: "I", label: "비활성"}]
     */
    public static <E extends Enum<E>> List<Map<String, Object>> toCodeLabelList(
            Class<E> enumClass,
            Function<E, String> codeExtractor,
            Function<E, String> labelExtractor) {
        if (enumClass == null || codeExtractor == null || labelExtractor == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("code", codeExtractor.apply(e));
                    map.put("label", labelExtractor.apply(e));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Enum을 코드-라벨-설명 쌍의 Map 리스트로 변환
     */
    public static <E extends Enum<E>> List<Map<String, Object>> toCodeLabelDescList(
            Class<E> enumClass,
            Function<E, String> codeExtractor,
            Function<E, String> labelExtractor,
            Function<E, String> descExtractor) {
        if (enumClass == null || codeExtractor == null || labelExtractor == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("code", codeExtractor.apply(e));
                    map.put("label", labelExtractor.apply(e));
                    if (descExtractor != null) {
                        map.put("description", descExtractor.apply(e));
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ===== 순서 관련 =====

    /**
     * 다음 Enum 반환 (순환)
     */
    public static <E extends Enum<E>> E next(E enumValue) {
        if (enumValue == null) {
            return null;
        }
        E[] constants = (E[]) enumValue.getClass().getEnumConstants();
        int nextOrdinal = (enumValue.ordinal() + 1) % constants.length;
        return constants[nextOrdinal];
    }

    /**
     * 이전 Enum 반환 (순환)
     */
    public static <E extends Enum<E>> E previous(E enumValue) {
        if (enumValue == null) {
            return null;
        }
        E[] constants = (E[]) enumValue.getClass().getEnumConstants();
        int prevOrdinal = (enumValue.ordinal() - 1 + constants.length) % constants.length;
        return constants[prevOrdinal];
    }

    /**
     * 첫 번째 Enum 반환
     */
    public static <E extends Enum<E>> E first(Class<E> enumClass) {
        if (enumClass == null) {
            return null;
        }
        E[] constants = enumClass.getEnumConstants();
        return constants.length > 0 ? constants[0] : null;
    }

    /**
     * 마지막 Enum 반환
     */
    public static <E extends Enum<E>> E last(Class<E> enumClass) {
        if (enumClass == null) {
            return null;
        }
        E[] constants = enumClass.getEnumConstants();
        return constants.length > 0 ? constants[constants.length - 1] : null;
    }

    // ===== 유틸리티 =====

    /**
     * Enum의 간단한 문자열 표현 (디버깅용)
     */
    public static <E extends Enum<E>> String toString(E enumValue) {
        if (enumValue == null) {
            return "null";
        }
        return enumValue.getClass().getSimpleName() + "." + enumValue.name();
    }

    /**
     * Enum 클래스의 모든 값을 콤마로 구분된 문자열로 반환
     */
    public static <E extends Enum<E>> String joinNames(Class<E> enumClass, String delimiter) {
        if (enumClass == null) {
            return "";
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Enum 클래스의 모든 값을 콤마로 구분된 문자열로 반환 (기본 구분자: ,)
     */
    public static <E extends Enum<E>> String joinNames(Class<E> enumClass) {
        return joinNames(enumClass, ", ");
    }
}
