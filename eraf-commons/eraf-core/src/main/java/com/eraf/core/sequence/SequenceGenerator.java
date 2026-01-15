package com.eraf.core.sequence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 채번 생성기
 * 기본 구현 (인메모리). 분산 환경에서는 Redis 기반 구현 사용 권장
 */
public class SequenceGenerator {

    private static final Map<String, AtomicLong> sequences = new ConcurrentHashMap<>();
    private static final Map<String, String> lastResetDates = new ConcurrentHashMap<>();

    /**
     * 다음 시퀀스 번호 생성
     *
     * @param name   시퀀스 이름
     * @param prefix 접두사
     * @param reset  리셋 정책
     * @param digits 숫자 자릿수
     * @return 생성된 시퀀스 번호
     */
    public static String next(String name, String prefix, Reset reset, int digits) {
        return next(name, prefix, reset, digits, "-", "yyyyMMdd");
    }

    /**
     * 다음 시퀀스 번호 생성 (상세 옵션)
     */
    public static String next(String name, String prefix, Reset reset, int digits,
                              String dateSeparator, String dateFormat) {
        String resetKey = getResetKey(name, reset, dateFormat);
        checkAndReset(name, resetKey, reset);

        AtomicLong sequence = sequences.computeIfAbsent(name, k -> new AtomicLong(0));
        long nextVal = sequence.incrementAndGet();

        StringBuilder result = new StringBuilder();

        // 접두사
        if (prefix != null && !prefix.isEmpty()) {
            result.append(prefix);
            if (reset != Reset.NEVER) {
                result.append(dateSeparator);
            }
        }

        // 날짜 부분 (리셋 정책에 따라)
        if (reset != Reset.NEVER) {
            String datePart = getDatePart(reset, dateFormat);
            result.append(datePart).append(dateSeparator);
        }

        // 숫자 부분 (0-padding)
        result.append(String.format("%0" + digits + "d", nextVal));

        return result.toString();
    }

    /**
     * 현재 시퀀스 값 조회
     */
    public static long current(String name) {
        AtomicLong sequence = sequences.get(name);
        return sequence != null ? sequence.get() : 0;
    }

    /**
     * 시퀀스 리셋
     */
    public static void reset(String name) {
        sequences.remove(name);
        lastResetDates.remove(name);
    }

    /**
     * 모든 시퀀스 초기화
     */
    public static void resetAll() {
        sequences.clear();
        lastResetDates.clear();
    }

    private static String getResetKey(String name, Reset reset, String dateFormat) {
        if (reset == Reset.NEVER) {
            return name;
        }
        return name + "_" + getDatePart(reset, dateFormat);
    }

    private static String getDatePart(Reset reset, String dateFormat) {
        LocalDate now = LocalDate.now();
        return switch (reset) {
            case DAILY -> now.format(DateTimeFormatter.ofPattern(dateFormat));
            case MONTHLY -> now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case YEARLY -> String.valueOf(now.getYear());
            case NEVER -> "";
        };
    }

    private static void checkAndReset(String name, String resetKey, Reset reset) {
        if (reset == Reset.NEVER) {
            return;
        }

        String lastKey = lastResetDates.get(name);
        if (lastKey == null || !lastKey.equals(resetKey)) {
            sequences.remove(name);
            lastResetDates.put(name, resetKey);
        }
    }
}
