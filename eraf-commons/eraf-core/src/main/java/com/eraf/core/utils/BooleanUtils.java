package com.eraf.core.utils;

/**
 * Boolean 유틸리티 (null-safe)
 */
public final class BooleanUtils {

    private BooleanUtils() {
    }

    // ===== Null-safe 체크 =====

    /**
     * true인지 확인 (null-safe, null이면 false)
     */
    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    /**
     * false인지 확인 (null-safe, null이면 false)
     */
    public static boolean isFalse(Boolean bool) {
        return Boolean.FALSE.equals(bool);
    }

    /**
     * true가 아닌지 확인 (false 또는 null)
     */
    public static boolean isNotTrue(Boolean bool) {
        return !isTrue(bool);
    }

    /**
     * false가 아닌지 확인 (true 또는 null)
     */
    public static boolean isNotFalse(Boolean bool) {
        return !isFalse(bool);
    }

    // ===== 문자열 변환 =====

    /**
     * 문자열을 Boolean으로 변환
     * "true", "yes", "y", "1", "on" -> true
     * "false", "no", "n", "0", "off" -> false
     * 그 외 -> null
     */
    public static Boolean toBoolean(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        String lower = str.trim().toLowerCase();
        switch (lower) {
            case "true":
            case "yes":
            case "y":
            case "1":
            case "on":
                return Boolean.TRUE;
            case "false":
            case "no":
            case "n":
            case "0":
            case "off":
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    /**
     * 문자열을 boolean으로 변환 (primitive, null이면 false)
     */
    public static boolean toBooleanDefaultFalse(String str) {
        Boolean result = toBoolean(str);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 문자열을 boolean으로 변환 (primitive, null이면 true)
     */
    public static boolean toBooleanDefaultTrue(String str) {
        Boolean result = toBoolean(str);
        return !Boolean.FALSE.equals(result);
    }

    /**
     * 문자열을 Boolean으로 변환 (커스텀 true/false 값)
     */
    public static Boolean toBoolean(String str, String trueString, String falseString) {
        if (str == null) {
            return null;
        }
        if (str.equals(trueString)) {
            return Boolean.TRUE;
        }
        if (str.equals(falseString)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * 정수를 Boolean으로 변환 (0이 아니면 true)
     */
    public static Boolean toBoolean(int value) {
        return value != 0;
    }

    /**
     * Boolean을 문자열로 변환
     */
    public static String toString(Boolean bool) {
        return bool == null ? null : bool.toString();
    }

    /**
     * Boolean을 문자열로 변환 (커스텀 true/false 값)
     */
    public static String toString(Boolean bool, String trueString, String falseString) {
        if (bool == null) {
            return null;
        }
        return bool ? trueString : falseString;
    }

    /**
     * Boolean을 문자열로 변환 (커스텀 true/false/null 값)
     */
    public static String toString(Boolean bool, String trueString, String falseString, String nullString) {
        if (bool == null) {
            return nullString;
        }
        return bool ? trueString : falseString;
    }

    /**
     * Boolean을 Y/N으로 변환
     */
    public static String toYN(Boolean bool) {
        return toString(bool, "Y", "N");
    }

    /**
     * Boolean을 Yes/No로 변환
     */
    public static String toYesNo(Boolean bool) {
        return toString(bool, "Yes", "No");
    }

    /**
     * Boolean을 ON/OFF로 변환
     */
    public static String toOnOff(Boolean bool) {
        return toString(bool, "ON", "OFF");
    }

    /**
     * Boolean을 1/0으로 변환
     */
    public static String to10(Boolean bool) {
        return toString(bool, "1", "0");
    }

    // ===== 정수 변환 =====

    /**
     * Boolean을 int로 변환 (true=1, false=0, null=0)
     */
    public static int toInt(Boolean bool) {
        return isTrue(bool) ? 1 : 0;
    }

    /**
     * Boolean을 Integer로 변환 (true=1, false=0, null=null)
     */
    public static Integer toInteger(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return bool ? 1 : 0;
    }

    // ===== 논리 연산 =====

    /**
     * AND 연산 (null-safe, null은 false로 간주)
     */
    public static boolean and(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (!isTrue(bool)) {
                return false;
            }
        }
        return true;
    }

    /**
     * OR 연산 (null-safe, null은 false로 간주)
     */
    public static boolean or(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (isTrue(bool)) {
                return true;
            }
        }
        return false;
    }

    /**
     * XOR 연산
     */
    public static boolean xor(Boolean bool1, Boolean bool2) {
        return isTrue(bool1) ^ isTrue(bool2);
    }

    /**
     * NOT 연산 (null-safe)
     */
    public static Boolean not(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return !bool;
    }

    /**
     * NAND 연산
     */
    public static boolean nand(Boolean bool1, Boolean bool2) {
        return !(isTrue(bool1) && isTrue(bool2));
    }

    /**
     * NOR 연산
     */
    public static boolean nor(Boolean bool1, Boolean bool2) {
        return !(isTrue(bool1) || isTrue(bool2));
    }

    // ===== 비교 =====

    /**
     * 두 Boolean이 같은지 비교 (null-safe)
     */
    public static boolean equals(Boolean bool1, Boolean bool2) {
        if (bool1 == null && bool2 == null) {
            return true;
        }
        if (bool1 == null || bool2 == null) {
            return false;
        }
        return bool1.equals(bool2);
    }

    /**
     * 배열의 모든 값이 true인지 확인
     */
    public static boolean allTrue(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (!isTrue(bool)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 배열의 모든 값이 false인지 확인
     */
    public static boolean allFalse(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (!isFalse(bool)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 배열에 하나라도 true가 있는지 확인
     */
    public static boolean anyTrue(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (isTrue(bool)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 배열에 하나라도 false가 있는지 확인
     */
    public static boolean anyFalse(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return false;
        }
        for (Boolean bool : booleans) {
            if (isFalse(bool)) {
                return true;
            }
        }
        return false;
    }

    // ===== 유틸리티 =====

    /**
     * null을 false로 변환
     */
    public static boolean nullToFalse(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }

    /**
     * null을 true로 변환
     */
    public static boolean nullToTrue(Boolean bool) {
        return !Boolean.FALSE.equals(bool);
    }

    /**
     * false를 null로 변환
     */
    public static Boolean falseToNull(Boolean bool) {
        return isTrue(bool) ? Boolean.TRUE : null;
    }

    /**
     * true를 null로 변환
     */
    public static Boolean trueToNull(Boolean bool) {
        return isFalse(bool) ? Boolean.FALSE : null;
    }

    /**
     * Boolean 값 토글
     */
    public static Boolean toggle(Boolean bool) {
        if (bool == null) {
            return null;
        }
        return !bool;
    }

    /**
     * boolean 값 토글
     */
    public static boolean toggle(boolean bool) {
        return !bool;
    }

    /**
     * 두 Boolean 중 하나가 true이면 true
     */
    public static Boolean or(Boolean bool1, Boolean bool2) {
        if (isTrue(bool1) || isTrue(bool2)) {
            return Boolean.TRUE;
        }
        if (bool1 == null || bool2 == null) {
            return null;
        }
        return Boolean.FALSE;
    }

    /**
     * 두 Boolean이 모두 true이면 true
     */
    public static Boolean and(Boolean bool1, Boolean bool2) {
        if (isFalse(bool1) || isFalse(bool2)) {
            return Boolean.FALSE;
        }
        if (bool1 == null || bool2 == null) {
            return null;
        }
        return Boolean.TRUE;
    }

    /**
     * 배열에서 true의 개수
     */
    public static int countTrue(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return 0;
        }
        int count = 0;
        for (Boolean bool : booleans) {
            if (isTrue(bool)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 배열에서 false의 개수
     */
    public static int countFalse(Boolean... booleans) {
        if (booleans == null || booleans.length == 0) {
            return 0;
        }
        int count = 0;
        for (Boolean bool : booleans) {
            if (isFalse(bool)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Y/N 문자열을 Boolean으로 변환
     */
    public static Boolean parseYN(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        String upper = str.trim().toUpperCase();
        if ("Y".equals(upper)) {
            return Boolean.TRUE;
        }
        if ("N".equals(upper)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * 1/0 문자열을 Boolean으로 변환
     */
    public static Boolean parse10(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        String trimmed = str.trim();
        if ("1".equals(trimmed)) {
            return Boolean.TRUE;
        }
        if ("0".equals(trimmed)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
