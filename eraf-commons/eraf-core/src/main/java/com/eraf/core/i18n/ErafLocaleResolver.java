package com.eraf.core.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * 로케일 결정기
 * 우선순위: 헤더 > 쿠키 > 세션 > 기본값
 */
public class ErafLocaleResolver implements LocaleResolver {

    public static final String LOCALE_SESSION_ATTRIBUTE = "ERAF_LOCALE";
    public static final String LOCALE_COOKIE_NAME = "ERAF_LOCALE";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    private Locale defaultLocale = Locale.KOREA;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. 세션에서 확인
        Object sessionLocale = request.getSession(false) != null ?
                request.getSession().getAttribute(LOCALE_SESSION_ATTRIBUTE) : null;
        if (sessionLocale instanceof Locale) {
            return (Locale) sessionLocale;
        }

        // 2. 쿠키에서 확인
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (LOCALE_COOKIE_NAME.equals(cookie.getName())) {
                    try {
                        return Locale.forLanguageTag(cookie.getValue());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 3. Accept-Language 헤더에서 확인
        String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE_HEADER);
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            try {
                return Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
            } catch (Exception ignored) {
            }
        }

        // 4. 기본 로케일
        return defaultLocale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale == null) {
            locale = defaultLocale;
        }

        // 세션에 저장
        request.getSession(true).setAttribute(LOCALE_SESSION_ATTRIBUTE, locale);

        // ThreadLocal에도 설정
        LocaleContextHolder.setLocale(locale);
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.KOREA;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }
}
