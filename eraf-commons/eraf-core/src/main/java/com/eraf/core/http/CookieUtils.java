package com.eraf.core.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * 쿠키 유틸리티
 */
public final class CookieUtils {

    private CookieUtils() {
    }

    /**
     * 쿠키 조회
     */
    public static Optional<String> get(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * 쿠키 조회 (Cookie 객체)
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }

    /**
     * 쿠키 설정 (기본)
     */
    public static void set(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 설정 (만료 시간)
     */
    public static void set(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 설정 (상세 옵션)
     */
    public static void set(HttpServletResponse response, String name, String value,
                          int maxAgeSeconds, String path, String domain,
                          boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setPath(path);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제
     */
    public static void delete(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제 (경로 지정)
     */
    public static void delete(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제 (경로 + 도메인 지정)
     */
    public static void delete(HttpServletResponse response, String name, String path, String domain) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(path);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
