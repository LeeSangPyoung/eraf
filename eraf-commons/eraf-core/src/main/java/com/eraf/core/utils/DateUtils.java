package com.eraf.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 날짜/시간 유틸리티
 */
public final class DateUtils {

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);

    private DateUtils() {
    }

    // ===== 현재 날짜/시간 =====

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    // ===== 문자열 변환 =====

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String format(LocalDate date, String pattern) {
        return date != null ? date.format(DateTimeFormatter.ofPattern(pattern)) : null;
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern(pattern)) : null;
    }

    // ===== 문자열 파싱 =====

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ===== 기간 계산 =====

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }

    public static long yearsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.YEARS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    // ===== 날짜 조작 =====

    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate endOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    public static LocalDate startOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    public static LocalDate endOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    // ===== 타입 변환 =====

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    // ===== 타임존 =====

    public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime, String zoneId) {
        return dateTime.atZone(ZoneId.of(zoneId));
    }

    public static LocalDateTime convertTimezone(LocalDateTime dateTime, String fromZone, String toZone) {
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of(fromZone));
        return zonedDateTime.withZoneSameInstant(ZoneId.of(toZone)).toLocalDateTime();
    }

    // ===== 비교 =====

    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        return date1.isBefore(date2);
    }

    public static boolean isAfter(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2);
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    // ===== 요일 =====

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    public static DayOfWeek getDayOfWeek(LocalDate date) {
        return date.getDayOfWeek();
    }

    // ===== 시작/끝 시간 =====

    /**
     * 날짜의 시작 시간 (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * LocalDateTime의 시작 시간 (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 날짜의 마지막 시간 (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59, 999999999);
    }

    /**
     * LocalDateTime의 마지막 시간 (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(23, 59, 59, 999999999);
    }

    /**
     * 달의 시작 시간
     */
    public static LocalDateTime startOfMonth(LocalDateTime dateTime) {
        return startOfMonth(dateTime.toLocalDate()).atStartOfDay();
    }

    /**
     * 달의 마지막 시간
     */
    public static LocalDateTime endOfMonth(LocalDateTime dateTime) {
        return endOfDay(endOfMonth(dateTime.toLocalDate()));
    }

    // ===== 타임스탬프 =====

    /**
     * 현재 타임스탬프 (밀리초)
     */
    public static long timestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 현재 타임스탬프 (초)
     */
    public static long timestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * LocalDateTime을 타임스탬프로 변환 (밀리초)
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * LocalDate를 타임스탬프로 변환 (밀리초)
     */
    public static long toTimestamp(LocalDate date) {
        return toTimestamp(date.atStartOfDay());
    }

    /**
     * Date를 타임스탬프로 변환 (밀리초)
     */
    public static long toTimestamp(Date date) {
        return date == null ? 0 : date.getTime();
    }

    /**
     * 타임스탬프를 LocalDateTime으로 변환
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 타임스탬프를 LocalDate로 변환
     */
    public static LocalDate fromTimestampToDate(long timestamp) {
        return fromTimestamp(timestamp).toLocalDate();
    }

    /**
     * 타임스탬프를 Date로 변환
     */
    public static Date fromTimestampToJavaDate(long timestamp) {
        return new Date(timestamp);
    }

    // ===== 문자열 변환 (추가) =====

    /**
     * 문자열을 Date로 변환 (java.util.Date)
     */
    public static Date parseToDate(String dateStr) {
        LocalDate localDate = parseDate(dateStr);
        return localDate != null ? toDate(localDate) : null;
    }

    /**
     * 문자열을 Date로 변환 (java.util.Date, 패턴 지정)
     */
    public static Date parseToDate(String dateStr, String pattern) {
        LocalDate localDate = parseDate(dateStr, pattern);
        return localDate != null ? toDate(localDate) : null;
    }

    /**
     * 문자열을 Date로 변환 (DateTime)
     */
    public static Date parseToDateTime(String dateTimeStr) {
        LocalDateTime localDateTime = parseDateTime(dateTimeStr);
        return localDateTime != null ? toDate(localDateTime) : null;
    }

    // ===== ISO 8601 =====

    /**
     * ISO 8601 형식으로 변환 (예: 2024-01-15T10:30:00)
     */
    public static String toIso8601(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * ISO 8601 형식으로 변환 (타임존 포함)
     */
    public static String toIso8601WithZone(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * ISO 8601 형식 파싱
     */
    public static LocalDateTime parseIso8601(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    // ===== 나이 계산 =====

    /**
     * 생년월일로 나이 계산
     */
    public static int calculateAge(LocalDate birthDate) {
        return calculateAge(birthDate, LocalDate.now());
    }

    /**
     * 특정 날짜 기준 나이 계산
     */
    public static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) {
            return 0;
        }
        return (int) yearsBetween(birthDate, referenceDate);
    }

    // ===== 유틸리티 =====

    /**
     * 특정 날짜가 오늘인지 확인
     */
    public static boolean isYesterday(LocalDate date) {
        return date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * 특정 날짜가 내일인지 확인
     */
    public static boolean isTomorrow(LocalDate date) {
        return date.equals(LocalDate.now().plusDays(1));
    }

    /**
     * 현재 시간 (초 단위까지, 밀리초/나노초 제거)
     */
    public static LocalDateTime nowTruncatedToSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * 현재 시간 (분 단위까지, 초/밀리초/나노초 제거)
     */
    public static LocalDateTime nowTruncatedToMinutes() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * 두 날짜가 같은 날인지 확인
     */
    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }

    /**
     * 두 LocalDateTime이 같은 날인지 확인
     */
    public static boolean isSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.toLocalDate().equals(dateTime2.toLocalDate());
    }

    /**
     * 윤년인지 확인
     */
    public static boolean isLeapYear(int year) {
        return java.time.Year.isLeap(year);
    }

    /**
     * 윤년인지 확인
     */
    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    /**
     * 해당 월의 일수 반환
     */
    public static int lengthOfMonth(LocalDate date) {
        return date.lengthOfMonth();
    }

    /**
     * 해당 년도의 일수 반환 (365 or 366)
     */
    public static int lengthOfYear(LocalDate date) {
        return date.lengthOfYear();
    }
}
