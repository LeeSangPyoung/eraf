package com.eraf.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 금액 유틸리티
 */
public final class MoneyUtils {

    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat COMMA_DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    private static final String[] KOREAN_UNITS = {"", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"};
    private static final String[] KOREAN_POSITIONS = {"", "십", "백", "천"};
    private static final String[] KOREAN_LARGE_UNITS = {"", "만", "억", "조", "경"};

    private MoneyUtils() {
    }

    // ===== 천단위 콤마 =====

    public static String formatWithComma(long amount) {
        return COMMA_FORMAT.format(amount);
    }

    public static String formatWithComma(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return COMMA_DECIMAL_FORMAT.format(amount);
    }

    public static String formatWithComma(double amount) {
        return COMMA_DECIMAL_FORMAT.format(amount);
    }

    // ===== 한글 변환 =====

    /**
     * 금액을 한글로 변환 (예: 1234567 -> "일백이십삼만사천오백육십칠")
     */
    public static String toKorean(long amount) {
        if (amount == 0) {
            return "영";
        }
        if (amount < 0) {
            return "마이너스 " + toKorean(-amount);
        }

        StringBuilder result = new StringBuilder();
        String numStr = String.valueOf(amount);
        int length = numStr.length();

        // 4자리씩 그룹핑
        int groupCount = (length + 3) / 4;
        int firstGroupLength = length % 4 == 0 ? 4 : length % 4;

        int index = 0;
        for (int group = groupCount - 1; group >= 0; group--) {
            int groupLength = (group == groupCount - 1) ? firstGroupLength : 4;
            StringBuilder groupResult = new StringBuilder();

            for (int i = 0; i < groupLength; i++) {
                int digit = numStr.charAt(index++) - '0';
                int position = groupLength - i - 1;

                if (digit != 0) {
                    // '일'은 십, 백, 천 앞에서 생략 (단, 만, 억 등의 단위 앞에서는 유지)
                    if (digit == 1 && position > 0) {
                        groupResult.append(KOREAN_POSITIONS[position]);
                    } else {
                        groupResult.append(KOREAN_UNITS[digit]);
                        groupResult.append(KOREAN_POSITIONS[position]);
                    }
                }
            }

            if (groupResult.length() > 0 && group < KOREAN_LARGE_UNITS.length) {
                result.append(groupResult);
                result.append(KOREAN_LARGE_UNITS[group]);
            }
        }

        return result.toString();
    }

    /**
     * 금액을 한글로 변환 (일금 ~ 정 형식)
     * 예: 3000000 -> "일금 삼백만원정"
     */
    public static String toKoreanFormal(long amount) {
        return "일금 " + toKorean(amount) + "원정";
    }

    // ===== 파싱 =====

    /**
     * 콤마가 포함된 문자열을 숫자로 변환
     */
    public static long parseWithComma(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return 0;
        }
        String cleaned = amountStr.replaceAll("[,\\s]", "");
        return Long.parseLong(cleaned);
    }

    public static BigDecimal parseWithCommaDecimal(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return BigDecimal.ZERO;
        }
        String cleaned = amountStr.replaceAll("[,\\s]", "");
        return new BigDecimal(cleaned);
    }

    // ===== 반올림 =====

    public static BigDecimal round(BigDecimal amount, int scale) {
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundDown(BigDecimal amount, int scale) {
        return amount.setScale(scale, RoundingMode.DOWN);
    }

    public static BigDecimal roundUp(BigDecimal amount, int scale) {
        return amount.setScale(scale, RoundingMode.UP);
    }

    /**
     * 10원 단위 절사
     */
    public static long truncateTo10(long amount) {
        return (amount / 10) * 10;
    }

    /**
     * 100원 단위 절사
     */
    public static long truncateTo100(long amount) {
        return (amount / 100) * 100;
    }

    /**
     * 1000원 단위 절사
     */
    public static long truncateTo1000(long amount) {
        return (amount / 1000) * 1000;
    }

    // ===== 계산 =====

    /**
     * 부가세 계산 (10%)
     */
    public static long calculateVat(long supplyAmount) {
        return Math.round(supplyAmount * 0.1);
    }

    /**
     * 공급가액에서 부가세 분리
     */
    public static long extractVatFromTotal(long totalAmount) {
        return Math.round(totalAmount / 11.0);
    }

    /**
     * 퍼센트 계산
     */
    public static BigDecimal percentage(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static long percentage(long amount, double percent) {
        return Math.round(amount * percent / 100);
    }
}
