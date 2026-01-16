package com.eraf.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtils - 날짜/시간 유틸리티 테스트")
class DateUtilsTest {

    @Nested
    @DisplayName("현재 날짜/시간")
    class CurrentDateTime {

        @Test
        @DisplayName("오늘 날짜 반환")
        void today() {
            LocalDate today = DateUtils.today();

            assertEquals(LocalDate.now(), today);
        }

        @Test
        @DisplayName("현재 시간 반환")
        void now() {
            LocalDateTime now = DateUtils.now();

            assertNotNull(now);
            assertEquals(LocalDate.now(), now.toLocalDate());
        }

        @Test
        @DisplayName("현재 Instant 반환")
        void nowInstant() {
            Instant instant = DateUtils.nowInstant();

            assertNotNull(instant);
        }
    }

    @Nested
    @DisplayName("문자열 포맷팅")
    class Formatting {

        @Test
        @DisplayName("LocalDate 기본 포맷")
        void formatLocalDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            String formatted = DateUtils.format(date);

            assertEquals("2024-01-15", formatted);
        }

        @Test
        @DisplayName("LocalDate 커스텀 포맷")
        void formatLocalDateCustom() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            String formatted = DateUtils.format(date, "yyyy/MM/dd");

            assertEquals("2024/01/15", formatted);
        }

        @Test
        @DisplayName("LocalDateTime 기본 포맷")
        void formatLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

            String formatted = DateUtils.format(dateTime);

            assertEquals("2024-01-15 14:30:45", formatted);
        }

        @Test
        @DisplayName("LocalDateTime 커스텀 포맷")
        void formatLocalDateTimeCustom() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

            String formatted = DateUtils.format(dateTime, "yyyy년 MM월 dd일 HH시 mm분");

            assertEquals("2024년 01월 15일 14시 30분", formatted);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void formatNull() {
            assertNull(DateUtils.format((LocalDate) null));
            assertNull(DateUtils.format((LocalDateTime) null));
        }
    }

    @Nested
    @DisplayName("문자열 파싱")
    class Parsing {

        @Test
        @DisplayName("날짜 문자열 파싱")
        void parseDate() {
            LocalDate date = DateUtils.parseDate("2024-01-15");

            assertEquals(LocalDate.of(2024, 1, 15), date);
        }

        @Test
        @DisplayName("커스텀 패턴 날짜 파싱")
        void parseDateCustom() {
            LocalDate date = DateUtils.parseDate("2024/01/15", "yyyy/MM/dd");

            assertEquals(LocalDate.of(2024, 1, 15), date);
        }

        @Test
        @DisplayName("날짜시간 문자열 파싱")
        void parseDateTime() {
            LocalDateTime dateTime = DateUtils.parseDateTime("2024-01-15 14:30:45");

            assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30, 45), dateTime);
        }

        @Test
        @DisplayName("잘못된 형식 파싱 시 null 반환")
        void parseInvalid() {
            assertNull(DateUtils.parseDate("invalid"));
            assertNull(DateUtils.parseDateTime("invalid"));
        }

        @Test
        @DisplayName("null/빈 문자열 파싱 시 null 반환")
        void parseNullOrEmpty() {
            assertNull(DateUtils.parseDate(null));
            assertNull(DateUtils.parseDate(""));
            assertNull(DateUtils.parseDate("   "));
        }
    }

    @Nested
    @DisplayName("기간 계산")
    class PeriodCalculation {

        @Test
        @DisplayName("일수 계산")
        void daysBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 10);

            assertEquals(9, DateUtils.daysBetween(start, end));
        }

        @Test
        @DisplayName("월수 계산")
        void monthsBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 6, 1);

            assertEquals(5, DateUtils.monthsBetween(start, end));
        }

        @Test
        @DisplayName("년수 계산")
        void yearsBetween() {
            LocalDate start = LocalDate.of(2020, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 1);

            assertEquals(4, DateUtils.yearsBetween(start, end));
        }

        @Test
        @DisplayName("시간 계산")
        void hoursBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 15, 30);

            assertEquals(5, DateUtils.hoursBetween(start, end));
        }

        @Test
        @DisplayName("분 계산")
        void minutesBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 45);

            assertEquals(45, DateUtils.minutesBetween(start, end));
        }
    }

    @Nested
    @DisplayName("날짜 조작")
    class DateManipulation {

        @Test
        @DisplayName("일 추가")
        void addDays() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            assertEquals(LocalDate.of(2024, 1, 25), DateUtils.addDays(date, 10));
            assertEquals(LocalDate.of(2024, 1, 5), DateUtils.addDays(date, -10));
        }

        @Test
        @DisplayName("월 추가")
        void addMonths() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            assertEquals(LocalDate.of(2024, 4, 15), DateUtils.addMonths(date, 3));
        }

        @Test
        @DisplayName("년 추가")
        void addYears() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            assertEquals(LocalDate.of(2026, 1, 15), DateUtils.addYears(date, 2));
        }

        @Test
        @DisplayName("월 시작일")
        void startOfMonth() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            assertEquals(LocalDate.of(2024, 1, 1), DateUtils.startOfMonth(date));
        }

        @Test
        @DisplayName("월 마지막일")
        void endOfMonth() {
            LocalDate date = LocalDate.of(2024, 1, 15);

            assertEquals(LocalDate.of(2024, 1, 31), DateUtils.endOfMonth(date));
        }

        @Test
        @DisplayName("년 시작일")
        void startOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertEquals(LocalDate.of(2024, 1, 1), DateUtils.startOfYear(date));
        }

        @Test
        @DisplayName("년 마지막일")
        void endOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertEquals(LocalDate.of(2024, 12, 31), DateUtils.endOfYear(date));
        }
    }

    @Nested
    @DisplayName("타입 변환")
    class TypeConversion {

        @Test
        @DisplayName("LocalDate to Date")
        void localDateToDate() {
            LocalDate localDate = LocalDate.of(2024, 1, 15);

            Date date = DateUtils.toDate(localDate);

            assertNotNull(date);
        }

        @Test
        @DisplayName("LocalDateTime to Date")
        void localDateTimeToDate() {
            LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 15, 14, 30);

            Date date = DateUtils.toDate(localDateTime);

            assertNotNull(date);
        }

        @Test
        @DisplayName("Date to LocalDate")
        void dateToLocalDate() {
            Date date = new Date();

            LocalDate localDate = DateUtils.toLocalDate(date);

            assertEquals(LocalDate.now(), localDate);
        }

        @Test
        @DisplayName("Date to LocalDateTime")
        void dateToLocalDateTime() {
            Date date = new Date();

            LocalDateTime localDateTime = DateUtils.toLocalDateTime(date);

            assertNotNull(localDateTime);
            assertEquals(LocalDate.now(), localDateTime.toLocalDate());
        }

        @Test
        @DisplayName("Instant to LocalDateTime")
        void instantToLocalDateTime() {
            Instant instant = Instant.now();

            LocalDateTime localDateTime = DateUtils.toLocalDateTime(instant);

            assertNotNull(localDateTime);
        }

        @Test
        @DisplayName("LocalDateTime to Instant")
        void localDateTimeToInstant() {
            LocalDateTime localDateTime = LocalDateTime.now();

            Instant instant = DateUtils.toInstant(localDateTime);

            assertNotNull(instant);
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("isBefore")
        void isBefore() {
            LocalDate date1 = LocalDate.of(2024, 1, 1);
            LocalDate date2 = LocalDate.of(2024, 1, 15);

            assertTrue(DateUtils.isBefore(date1, date2));
            assertFalse(DateUtils.isBefore(date2, date1));
        }

        @Test
        @DisplayName("isAfter")
        void isAfter() {
            LocalDate date1 = LocalDate.of(2024, 1, 1);
            LocalDate date2 = LocalDate.of(2024, 1, 15);

            assertFalse(DateUtils.isAfter(date1, date2));
            assertTrue(DateUtils.isAfter(date2, date1));
        }

        @Test
        @DisplayName("isBetween")
        void isBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);
            LocalDate between = LocalDate.of(2024, 1, 15);
            LocalDate outside = LocalDate.of(2024, 2, 15);

            assertTrue(DateUtils.isBetween(between, start, end));
            assertTrue(DateUtils.isBetween(start, start, end)); // 경계 포함
            assertTrue(DateUtils.isBetween(end, start, end)); // 경계 포함
            assertFalse(DateUtils.isBetween(outside, start, end));
        }

        @Test
        @DisplayName("isToday")
        void isToday() {
            assertTrue(DateUtils.isToday(LocalDate.now()));
            assertFalse(DateUtils.isToday(LocalDate.now().minusDays(1)));
        }

        @Test
        @DisplayName("isPast")
        void isPast() {
            assertTrue(DateUtils.isPast(LocalDate.now().minusDays(1)));
            assertFalse(DateUtils.isPast(LocalDate.now()));
            assertFalse(DateUtils.isPast(LocalDate.now().plusDays(1)));
        }

        @Test
        @DisplayName("isFuture")
        void isFuture() {
            assertTrue(DateUtils.isFuture(LocalDate.now().plusDays(1)));
            assertFalse(DateUtils.isFuture(LocalDate.now()));
            assertFalse(DateUtils.isFuture(LocalDate.now().minusDays(1)));
        }
    }

    @Nested
    @DisplayName("요일 체크")
    class DayOfWeekCheck {

        @Test
        @DisplayName("주말 체크")
        void isWeekend() {
            LocalDate saturday = LocalDate.of(2024, 1, 13); // 토요일
            LocalDate sunday = LocalDate.of(2024, 1, 14); // 일요일
            LocalDate monday = LocalDate.of(2024, 1, 15); // 월요일

            assertTrue(DateUtils.isWeekend(saturday));
            assertTrue(DateUtils.isWeekend(sunday));
            assertFalse(DateUtils.isWeekend(monday));
        }

        @Test
        @DisplayName("평일 체크")
        void isWeekday() {
            LocalDate saturday = LocalDate.of(2024, 1, 13); // 토요일
            LocalDate monday = LocalDate.of(2024, 1, 15); // 월요일

            assertFalse(DateUtils.isWeekday(saturday));
            assertTrue(DateUtils.isWeekday(monday));
        }

        @Test
        @DisplayName("요일 반환")
        void getDayOfWeek() {
            LocalDate monday = LocalDate.of(2024, 1, 15);

            assertEquals(DayOfWeek.MONDAY, DateUtils.getDayOfWeek(monday));
        }
    }

    @Nested
    @DisplayName("타임존 변환")
    class TimezoneConversion {

        @Test
        @DisplayName("타임존 변환")
        void convertTimezone() {
            LocalDateTime seoulTime = LocalDateTime.of(2024, 1, 15, 12, 0); // 서울 정오

            LocalDateTime utcTime = DateUtils.convertTimezone(seoulTime, "Asia/Seoul", "UTC");

            // 서울은 UTC+9
            assertEquals(LocalDateTime.of(2024, 1, 15, 3, 0), utcTime);
        }

        @Test
        @DisplayName("ZonedDateTime 변환")
        void toZonedDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 12, 0);

            ZonedDateTime zonedDateTime = DateUtils.toZonedDateTime(dateTime, "Asia/Seoul");

            assertEquals(ZoneId.of("Asia/Seoul"), zonedDateTime.getZone());
        }
    }
}
