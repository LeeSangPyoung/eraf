package com.eraf.core.utils;

import java.util.regex.Pattern;

/**
 * 경로 패턴 매칭 유틸리티
 *
 * 지원 패턴:
 * - "/**" : 모든 하위 경로 매칭 (예: /api/** → /api/users, /api/users/1)
 * - "/*"  : 한 단계 하위만 매칭 (예: /api/* → /api/users, 단 /api/users/1 제외)
 * - "/exact" : 정확히 일치
 */
public final class PathMatcher {

    private PathMatcher() {
    }

    /**
     * 경로가 패턴과 일치하는지 확인
     *
     * @param path 확인할 경로
     * @param pattern 패턴 (지원: /**, /*, exact match)
     * @return 일치 여부
     */
    public static boolean matches(String path, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        if (path == null) {
            return false;
        }

        // 모든 경로 매칭
        if ("/**".equals(pattern)) {
            return true;
        }

        // 모든 하위 경로 매칭 (/api/**)
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix);
        }

        // 한 단계 하위만 매칭 (/api/*)
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            if (!path.startsWith(prefix)) {
                return false;
            }
            String remaining = path.substring(prefix.length());
            // 남은 부분이 비어있거나, 슬래시로 시작하고 더 이상 슬래시가 없어야 함
            if (remaining.isEmpty()) {
                return false; // prefix 자체는 매칭 안됨 (/api/* 에서 /api는 매칭 안됨)
            }
            if (remaining.startsWith("/")) {
                remaining = remaining.substring(1);
            }
            return !remaining.contains("/");
        }

        // 정확한 매칭
        return path.equals(pattern);
    }

    /**
     * 경로가 여러 패턴 중 하나와 일치하는지 확인
     */
    public static boolean matchesAny(String path, Iterable<String> patterns) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (matches(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 경로가 여러 패턴 중 하나와 일치하는지 확인
     */
    public static boolean matchesAny(String path, String... patterns) {
        if (patterns == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (matches(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ant 스타일 패턴을 정규식으로 변환
     *
     * @param antPattern Ant 스타일 패턴 (**, *, ? 지원)
     * @return 컴파일된 Pattern
     */
    public static Pattern toRegex(String antPattern) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < antPattern.length(); i++) {
            char c = antPattern.charAt(i);

            if (c == '*') {
                if (i + 1 < antPattern.length() && antPattern.charAt(i + 1) == '*') {
                    // ** : 모든 문자 (슬래시 포함)
                    regex.append(".*");
                    i++; // 다음 * 건너뛰기
                } else {
                    // * : 슬래시를 제외한 모든 문자
                    regex.append("[^/]*");
                }
            } else if (c == '?') {
                // ? : 슬래시를 제외한 단일 문자
                regex.append("[^/]");
            } else if (c == '.' || c == '(' || c == ')' || c == '[' || c == ']'
                    || c == '{' || c == '}' || c == '+' || c == '^' || c == '$' || c == '|') {
                // 정규식 특수문자 이스케이프
                regex.append("\\").append(c);
            } else {
                regex.append(c);
            }
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    /**
     * 정규식 패턴으로 경로 매칭
     */
    public static boolean matchesRegex(String path, Pattern pattern) {
        if (path == null || pattern == null) {
            return false;
        }
        return pattern.matcher(path).matches();
    }
}
