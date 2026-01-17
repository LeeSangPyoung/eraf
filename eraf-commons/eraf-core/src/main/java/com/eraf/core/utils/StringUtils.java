package com.eraf.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 문자열 유틸리티 (null-safe)
 */
public final class StringUtils {

    private StringUtils() {
    }

    // ===== Null/Empty 체크 =====

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    // ===== Null-safe 연산 =====

    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isBlank(trimmed) ? null : trimmed;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    // ===== 길이 제한 =====

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    public static String truncateWithEllipsis(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 바이트 기준으로 자르기
     */
    public static String truncateBytes(String str, int maxBytes, Charset charset) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes(charset);
        if (bytes.length <= maxBytes) {
            return str;
        }
        // 바이트 단위로 잘라서 문자열로 변환
        return new String(bytes, 0, maxBytes, charset).replaceAll("\\uFFFD$", "");
    }

    public static String truncateBytes(String str, int maxBytes) {
        return truncateBytes(str, maxBytes, StandardCharsets.UTF_8);
    }

    // ===== 패딩 =====

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            str = "";
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return String.valueOf(padChar).repeat(pads) + str;
    }

    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            str = "";
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str + String.valueOf(padChar).repeat(pads);
    }

    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String zeroPad(int number, int size) {
        return leftPad(String.valueOf(number), size, '0');
    }

    public static String zeroPad(long number, int size) {
        return leftPad(String.valueOf(number), size, '0');
    }

    // ===== 변환 =====

    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static String toCamelCase(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static String toSnakeCase(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String toKebabCase(String str) {
        return toSnakeCase(str).replace('_', '-');
    }

    // ===== 비교 =====

    public static boolean equals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.contains(searchStr);
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    // ===== 유틸리티 =====

    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    public static String remove(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return str.replace(remove, "");
    }

    public static String removeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", "");
    }

    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    public static int byteLength(String str, Charset charset) {
        return str == null ? 0 : str.getBytes(charset).length;
    }

    public static int byteLength(String str) {
        return byteLength(str, StandardCharsets.UTF_8);
    }

    // ===== 부분 문자열 =====

    /**
     * 안전한 substring (IndexOutOfBoundsException 방지)
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (start >= str.length()) {
            return "";
        }
        return str.substring(start);
    }

    /**
     * 안전한 substring (IndexOutOfBoundsException 방지)
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start >= end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 구분자 이전 문자열 반환
     */
    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 구분자 이후 문자열 반환
     */
    public static String substringAfter(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return str;
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 마지막 구분자 이전 문자열 반환
     */
    public static String substringBeforeLast(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 마지막 구분자 이후 문자열 반환
     */
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 두 구분자 사이의 문자열 반환
     */
    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start == -1) {
            return null;
        }
        int end = str.indexOf(close, start + open.length());
        if (end == -1) {
            return null;
        }
        return str.substring(start + open.length(), end);
    }

    // ===== 분할/조인 =====

    /**
     * 문자열을 구분자로 분할 (null-safe)
     */
    public static String[] split(String str, String separator) {
        if (str == null) {
            return new String[0];
        }
        if (separator == null || separator.isEmpty()) {
            return new String[]{str};
        }
        return str.split(java.util.regex.Pattern.quote(separator));
    }

    /**
     * 문자열을 구분자로 분할하고 트림
     */
    public static String[] splitAndTrim(String str, String separator) {
        String[] parts = split(str, separator);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = trim(parts[i]);
        }
        return parts;
    }

    /**
     * 배열을 구분자로 조인
     */
    public static String join(String[] array, String separator) {
        if (array == null || array.length == 0) {
            return "";
        }
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            if (array[i] != null) {
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Iterable을 구분자로 조인
     */
    public static String join(Iterable<?> iterable, String separator) {
        if (iterable == null) {
            return "";
        }
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object obj : iterable) {
            if (!first) {
                sb.append(separator);
            }
            if (obj != null) {
                sb.append(obj);
            }
            first = false;
        }
        return sb.toString();
    }

    // ===== 시작/끝 확인 =====

    /**
     * 특정 문자열로 시작하는지 확인 (null-safe)
     */
    public static boolean startsWith(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * 대소문자 구분 없이 특정 문자열로 시작하는지 확인
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * 특정 문자열로 끝나는지 확인 (null-safe)
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * 대소문자 구분 없이 특정 문자열로 끝나는지 확인
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.toLowerCase().endsWith(suffix.toLowerCase());
    }

    // ===== 치환 =====

    /**
     * 문자열 치환 (null-safe)
     */
    public static String replace(String str, String searchString, String replacement) {
        if (isEmpty(str) || isEmpty(searchString) || replacement == null) {
            return str;
        }
        return str.replace(searchString, replacement);
    }

    /**
     * 첫 번째 일치만 치환
     */
    public static String replaceFirst(String str, String searchString, String replacement) {
        if (isEmpty(str) || isEmpty(searchString) || replacement == null) {
            return str;
        }
        int index = str.indexOf(searchString);
        if (index == -1) {
            return str;
        }
        return str.substring(0, index) + replacement + str.substring(index + searchString.length());
    }

    /**
     * 마지막 일치만 치환
     */
    public static String replaceLast(String str, String searchString, String replacement) {
        if (isEmpty(str) || isEmpty(searchString) || replacement == null) {
            return str;
        }
        int index = str.lastIndexOf(searchString);
        if (index == -1) {
            return str;
        }
        return str.substring(0, index) + replacement + str.substring(index + searchString.length());
    }

    // ===== 반복 =====

    /**
     * 문자열 반복
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        return str.repeat(count);
    }

    /**
     * 문자 반복
     */
    public static String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        return String.valueOf(ch).repeat(count);
    }

    // ===== 기타 =====

    /**
     * 인덱스 찾기 (null-safe)
     */
    public static int indexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.indexOf(searchStr);
    }

    /**
     * 마지막 인덱스 찾기 (null-safe)
     */
    public static int lastIndexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.lastIndexOf(searchStr);
    }

    /**
     * 문자열의 특정 위치 문자 반환 (null-safe)
     */
    public static Character charAt(String str, int index) {
        if (str == null || index < 0 || index >= str.length()) {
            return null;
        }
        return str.charAt(index);
    }

    /**
     * 대문자로 변환 (null-safe)
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * 소문자로 변환 (null-safe)
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * 공백 정규화 (연속된 공백을 하나로)
     */
    public static String normalizeSpace(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().replaceAll("\\s+", " ");
    }

    /**
     * 줄바꿈을 공백으로 변환
     */
    public static String removeNewlines(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("[\\r\\n]+", " ");
    }

    /**
     * 특정 문자열이 몇 번 나타나는지 카운트
     */
    public static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }

    /**
     * 숫자만 포함하는지 확인
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 알파벳만 포함하는지 확인
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 알파벳과 숫자만 포함하는지 확인
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
