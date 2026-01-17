package com.eraf.core.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 숫자 유틸리티 (null-safe)
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    // ===== 문자열 → 숫자 변환 (안전) =====

    /**
     * 문자열을 int로 변환, 실패시 기본값 반환
     */
    public static int toInt(String str, int defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 int로 변환, 실패시 0 반환
     */
    public static int toInt(String str) {
        return toInt(str, 0);
    }

    /**
     * 문자열을 long으로 변환, 실패시 기본값 반환
     */
    public static long toLong(String str, long defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 long으로 변환, 실패시 0 반환
     */
    public static long toLong(String str) {
        return toLong(str, 0L);
    }

    /**
     * 문자열을 double로 변환, 실패시 기본값 반환
     */
    public static double toDouble(String str, double defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 double로 변환, 실패시 0.0 반환
     */
    public static double toDouble(String str) {
        return toDouble(str, 0.0);
    }

    /**
     * 문자열을 float로 변환, 실패시 기본값 반환
     */
    public static float toFloat(String str, float defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 float로 변환, 실패시 0.0f 반환
     */
    public static float toFloat(String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * 문자열을 BigDecimal로 변환, 실패시 기본값 반환
     */
    public static BigDecimal toBigDecimal(String str, BigDecimal defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 BigDecimal로 변환, 실패시 0 반환
     */
    public static BigDecimal toBigDecimal(String str) {
        return toBigDecimal(str, BigDecimal.ZERO);
    }

    /**
     * 문자열을 BigInteger로 변환, 실패시 기본값 반환
     */
    public static BigInteger toBigInteger(String str, BigInteger defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigInteger(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 문자열을 BigInteger로 변환, 실패시 0 반환
     */
    public static BigInteger toBigInteger(String str) {
        return toBigInteger(str, BigInteger.ZERO);
    }

    // ===== 검증 =====

    /**
     * 숫자 문자열인지 확인
     */
    public static boolean isNumber(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 정수 문자열인지 확인
     */
    public static boolean isInteger(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 양수인지 확인
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    /**
     * 음수인지 확인
     */
    public static boolean isNegative(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() < 0;
    }

    /**
     * 0인지 확인
     */
    public static boolean isZero(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() == 0;
    }

    // ===== 비교 =====

    /**
     * 최소값 반환
     */
    public static int min(int... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        int min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * 최소값 반환 (long)
     */
    public static long min(long... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        long min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * 최소값 반환 (double)
     */
    public static double min(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * 최대값 반환
     */
    public static int max(int... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        int max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * 최대값 반환 (long)
     */
    public static long max(long... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        long max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * 최대값 반환 (double)
     */
    public static double max(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("값이 없습니다");
        }
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * 값을 범위 내로 제한 (clamp)
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 값을 범위 내로 제한 (long)
     */
    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 값을 범위 내로 제한 (double)
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 범위 내에 있는지 확인
     */
    public static boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * 범위 내에 있는지 확인 (long)
     */
    public static boolean isBetween(long value, long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * 범위 내에 있는지 확인 (double)
     */
    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    // ===== 반올림/올림/내림 =====

    /**
     * 반올림 (소수점 자리수 지정)
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("소수점 자리수는 0 이상이어야 합니다");
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 반올림 (정수로)
     */
    public static long round(double value) {
        return Math.round(value);
    }

    /**
     * 올림
     */
    public static long ceil(double value) {
        return (long) Math.ceil(value);
    }

    /**
     * 내림
     */
    public static long floor(double value) {
        return (long) Math.floor(value);
    }

    /**
     * BigDecimal 반올림
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * BigDecimal 올림
     */
    public static BigDecimal ceil(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.CEILING);
    }

    /**
     * BigDecimal 내림
     */
    public static BigDecimal floor(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.FLOOR);
    }

    // ===== 포맷팅 =====

    /**
     * 숫자를 천단위 콤마 포맷으로 변환
     */
    public static String formatWithComma(Number number) {
        if (number == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(number);
    }

    /**
     * 숫자를 지정된 패턴으로 포맷
     */
    public static String format(Number number, String pattern) {
        if (number == null) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    /**
     * 천단위 콤마가 있는 문자열을 숫자로 변환
     */
    public static long parseWithComma(String str) {
        if (str == null || str.isBlank()) {
            return 0L;
        }
        try {
            NumberFormat nf = NumberFormat.getInstance();
            return nf.parse(str.trim()).longValue();
        } catch (ParseException e) {
            return 0L;
        }
    }

    /**
     * 천단위 콤마가 있는 문자열을 BigDecimal로 변환
     */
    public static BigDecimal parseWithCommaDecimal(String str) {
        if (str == null || str.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            NumberFormat nf = NumberFormat.getInstance();
            Number number = nf.parse(str.trim());
            return new BigDecimal(number.toString());
        } catch (ParseException e) {
            return BigDecimal.ZERO;
        }
    }

    // ===== 변환 =====

    /**
     * Number를 int로 변환
     */
    public static int toInt(Number number) {
        return number == null ? 0 : number.intValue();
    }

    /**
     * Number를 long으로 변환
     */
    public static long toLong(Number number) {
        return number == null ? 0L : number.longValue();
    }

    /**
     * Number를 double로 변환
     */
    public static double toDouble(Number number) {
        return number == null ? 0.0 : number.doubleValue();
    }

    /**
     * Number를 float로 변환
     */
    public static float toFloat(Number number) {
        return number == null ? 0.0f : number.floatValue();
    }

    /**
     * Number를 BigDecimal로 변환
     */
    public static BigDecimal toBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

    // ===== 계산 =====

    /**
     * 퍼센트 계산
     */
    public static double percentage(double value, double percent) {
        return value * percent / 100.0;
    }

    /**
     * 퍼센트 계산 (정수)
     */
    public static long percentage(long value, double percent) {
        return Math.round(value * percent / 100.0);
    }

    /**
     * 비율 계산 (value / total * 100)
     */
    public static double ratio(double value, double total) {
        if (total == 0) {
            return 0.0;
        }
        return (value / total) * 100.0;
    }

    /**
     * 평균 계산
     */
    public static double average(int... values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        long sum = 0;
        for (int value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }

    /**
     * 평균 계산 (long)
     */
    public static double average(long... values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }

    /**
     * 평균 계산 (double)
     */
    public static double average(double... values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * 합계
     */
    public static int sum(int... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }

    /**
     * 합계 (long)
     */
    public static long sum(long... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum;
    }

    /**
     * 합계 (double)
     */
    public static double sum(double... values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    // ===== 기타 =====

    /**
     * 절대값
     */
    public static int abs(int value) {
        return Math.abs(value);
    }

    /**
     * 절대값 (long)
     */
    public static long abs(long value) {
        return Math.abs(value);
    }

    /**
     * 절대값 (double)
     */
    public static double abs(double value) {
        return Math.abs(value);
    }

    /**
     * 부호 반환 (-1, 0, 1)
     */
    public static int signum(int value) {
        return Integer.signum(value);
    }

    /**
     * 부호 반환 (long)
     */
    public static int signum(long value) {
        return Long.signum(value);
    }

    /**
     * 부호 반환 (double)
     */
    public static double signum(double value) {
        return Math.signum(value);
    }

    /**
     * 거듭제곱
     */
    public static double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    /**
     * 제곱근
     */
    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    /**
     * null을 0으로 변환
     */
    public static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * null을 0으로 변환 (long)
     */
    public static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * null을 0으로 변환 (double)
     */
    public static double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    /**
     * 0을 null로 변환
     */
    public static Integer zeroToNull(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    /**
     * 0을 null로 변환 (long)
     */
    public static Long zeroToNull(Long value) {
        return value == null || value == 0L ? null : value;
    }
}
