package com.eraf.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 정규식 유틸리티
 * 정규식을 간편하게 사용
 */
public final class RegexUtils {

    private RegexUtils() {
    }

    // 자주 쓰는 패턴 상수
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_PATTERN = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$";
    public static final String URL_PATTERN = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    public static final String IP_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    public static final String NUMERIC_PATTERN = "^[0-9]+$";
    public static final String ALPHA_PATTERN = "^[A-Za-z]+$";
    public static final String ALPHANUMERIC_PATTERN = "^[A-Za-z0-9]+$";
    public static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    public static final String TIME_PATTERN = "^\\d{2}:\\d{2}:\\d{2}$";
    public static final String DATETIME_PATTERN = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

    // ===== 매칭 =====

    /**
     * 문자열이 패턴과 일치하는지 확인
     */
    public static boolean matches(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.matches(regex, input);
    }

    /**
     * 문자열이 패턴과 일치하는지 확인 (대소문자 무시)
     */
    public static boolean matchesIgnoreCase(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input).matches();
    }

    /**
     * 문자열에 패턴이 포함되어 있는지 확인
     */
    public static boolean contains(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.compile(regex).matcher(input).find();
    }

    /**
     * 문자열이 패턴으로 시작하는지 확인
     */
    public static boolean startsWith(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.compile("^" + regex).matcher(input).find();
    }

    /**
     * 문자열이 패턴으로 끝나는지 확인
     */
    public static boolean endsWith(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.compile(regex + "$").matcher(input).find();
    }

    // ===== 추출 =====

    /**
     * 패턴과 일치하는 첫 번째 문자열 추출
     */
    public static String extract(String input, String regex) {
        if (input == null || regex == null) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 패턴과 일치하는 모든 문자열 추출
     */
    public static List<String> extractAll(String input, String regex) {
        List<String> results = new ArrayList<>();
        if (input == null || regex == null) {
            return results;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    /**
     * 패턴의 특정 그룹 추출
     */
    public static String extractGroup(String input, String regex, int groupIndex) {
        if (input == null || regex == null) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find() && groupIndex <= matcher.groupCount()) {
            return matcher.group(groupIndex);
        }
        return null;
    }

    /**
     * 패턴의 모든 그룹을 리스트로 추출
     */
    public static List<String> extractGroups(String input, String regex) {
        List<String> groups = new ArrayList<>();
        if (input == null || regex == null) {
            return groups;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
        }
        return groups;
    }

    // ===== 치환 =====

    /**
     * 패턴과 일치하는 모든 부분을 치환
     */
    public static String replaceAll(String input, String regex, String replacement) {
        if (input == null || regex == null || replacement == null) {
            return input;
        }
        return input.replaceAll(regex, replacement);
    }

    /**
     * 패턴과 일치하는 첫 번째 부분만 치환
     */
    public static String replaceFirst(String input, String regex, String replacement) {
        if (input == null || regex == null || replacement == null) {
            return input;
        }
        return input.replaceFirst(regex, replacement);
    }

    /**
     * 패턴과 일치하는 모든 부분을 제거
     */
    public static String remove(String input, String regex) {
        return replaceAll(input, regex, "");
    }

    // ===== 분할 =====

    /**
     * 패턴을 구분자로 문자열 분할
     */
    public static String[] split(String input, String regex) {
        if (input == null || regex == null) {
            return new String[0];
        }
        return input.split(regex);
    }

    /**
     * 패턴을 구분자로 문자열 분할 (최대 개수 지정)
     */
    public static String[] split(String input, String regex, int limit) {
        if (input == null || regex == null) {
            return new String[0];
        }
        return input.split(regex, limit);
    }

    // ===== 카운트 =====

    /**
     * 패턴과 일치하는 개수 반환
     */
    public static int count(String input, String regex) {
        if (input == null || regex == null) {
            return 0;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    // ===== 검증 (자주 쓰는 패턴) =====

    /**
     * 이메일 형식인지 검증
     */
    public static boolean isEmail(String email) {
        return matches(email, EMAIL_PATTERN);
    }

    /**
     * 한국 휴대폰 번호 형식인지 검증
     */
    public static boolean isPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        String cleaned = phone.replaceAll("[\\s-]", "");
        return matches(cleaned, "^01[0-9]{8,9}$");
    }

    /**
     * URL 형식인지 검증
     */
    public static boolean isUrl(String url) {
        return matches(url, URL_PATTERN);
    }

    /**
     * IP 주소 형식인지 검증
     */
    public static boolean isIpAddress(String ip) {
        return matches(ip, IP_PATTERN);
    }

    /**
     * 숫자만 포함하는지 검증
     */
    public static boolean isNumeric(String str) {
        return matches(str, NUMERIC_PATTERN);
    }

    /**
     * 알파벳만 포함하는지 검증
     */
    public static boolean isAlpha(String str) {
        return matches(str, ALPHA_PATTERN);
    }

    /**
     * 알파벳+숫자만 포함하는지 검증
     */
    public static boolean isAlphanumeric(String str) {
        return matches(str, ALPHANUMERIC_PATTERN);
    }

    /**
     * 날짜 형식(YYYY-MM-DD)인지 검증
     */
    public static boolean isDate(String date) {
        return matches(date, DATE_PATTERN);
    }

    /**
     * 시간 형식(HH:MM:SS)인지 검증
     */
    public static boolean isTime(String time) {
        return matches(time, TIME_PATTERN);
    }

    /**
     * 날짜시간 형식(YYYY-MM-DD HH:MM:SS)인지 검증
     */
    public static boolean isDateTime(String dateTime) {
        return matches(dateTime, DATETIME_PATTERN);
    }

    /**
     * 한글만 포함하는지 검증
     */
    public static boolean isKorean(String str) {
        return matches(str, "^[가-힣]+$");
    }

    /**
     * 영문 대문자만 포함하는지 검증
     */
    public static boolean isUpperCase(String str) {
        return matches(str, "^[A-Z]+$");
    }

    /**
     * 영문 소문자만 포함하는지 검증
     */
    public static boolean isLowerCase(String str) {
        return matches(str, "^[a-z]+$");
    }

    // ===== Pattern 캐싱 =====

    /**
     * Pattern 컴파일 (재사용 가능)
     */
    public static Pattern compile(String regex) {
        return Pattern.compile(regex);
    }

    /**
     * Pattern 컴파일 (플래그 지정)
     */
    public static Pattern compile(String regex, int flags) {
        return Pattern.compile(regex, flags);
    }

    // ===== 정규식 이스케이프 =====

    /**
     * 정규식 특수 문자를 이스케이프 처리
     */
    public static String escape(String str) {
        if (str == null) {
            return null;
        }
        return Pattern.quote(str);
    }

    /**
     * 정규식 특수 문자를 실제로 이스케이프 (\\를 추가)
     */
    public static String escapeSpecialChars(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([\\\\*+?|()\\[\\]{}^$.])", "\\\\$1");
    }

    // ===== 유틸리티 =====

    /**
     * Matcher 생성
     */
    public static Matcher matcher(String input, String regex) {
        if (input == null || regex == null) {
            return null;
        }
        return Pattern.compile(regex).matcher(input);
    }

    /**
     * 유효한 정규식인지 검증
     */
    public static boolean isValidRegex(String regex) {
        if (regex == null) {
            return false;
        }
        try {
            Pattern.compile(regex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
