package com.eraf.core.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 세션 유틸리티
 */
public final class SessionUtils {

    private static final String USER_KEY = "CURRENT_USER";

    private SessionUtils() {
    }

    /**
     * 현재 요청의 HttpSession 조회
     */
    public static Optional<HttpSession> getSession() {
        return getSession(false);
    }

    /**
     * 현재 요청의 HttpSession 조회 (생성 옵션)
     */
    public static Optional<HttpSession> getSession(boolean create) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(request.getSession(create));
    }

    /**
     * 세션 속성 조회
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(String key) {
        return getSession()
                .map(session -> (T) session.getAttribute(key));
    }

    /**
     * 세션 속성 조회 (기본값)
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        return getSession()
                .map(session -> (T) session.getAttribute(key))
                .orElse(defaultValue);
    }

    /**
     * 세션 속성 설정
     */
    public static void set(String key, Object value) {
        getSession(true).ifPresent(session -> session.setAttribute(key, value));
    }

    /**
     * 세션 속성 제거
     */
    public static void remove(String key) {
        getSession().ifPresent(session -> session.removeAttribute(key));
    }

    /**
     * 현재 사용자 조회
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getCurrentUser() {
        return get(USER_KEY);
    }

    /**
     * 현재 사용자 설정
     */
    public static void setCurrentUser(Object user) {
        set(USER_KEY, user);
    }

    /**
     * 세션 무효화 (로그아웃)
     */
    public static void invalidate() {
        getSession().ifPresent(HttpSession::invalidate);
    }

    /**
     * 세션 ID 조회
     */
    public static Optional<String> getSessionId() {
        return getSession().map(HttpSession::getId);
    }

    /**
     * 세션 생성 시간 조회
     */
    public static Optional<Long> getCreationTime() {
        return getSession().map(HttpSession::getCreationTime);
    }

    /**
     * 마지막 접근 시간 조회
     */
    public static Optional<Long> getLastAccessedTime() {
        return getSession().map(HttpSession::getLastAccessedTime);
    }

    /**
     * 세션 타임아웃 설정 (초)
     */
    public static void setMaxInactiveInterval(int seconds) {
        getSession(true).ifPresent(session -> session.setMaxInactiveInterval(seconds));
    }

    /**
     * 새 세션 여부
     */
    public static boolean isNew() {
        return getSession().map(HttpSession::isNew).orElse(true);
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
