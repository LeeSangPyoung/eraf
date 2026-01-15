package com.eraf.core.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 다국어 메시지 서비스
 */
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 메시지 조회 (현재 로케일)
     */
    public String get(String code) {
        return get(code, (Object[]) null);
    }

    /**
     * 메시지 조회 (파라미터 포함)
     */
    public String get(String code, Object... args) {
        return get(code, LocaleContextHolder.getLocale(), args);
    }

    /**
     * 메시지 조회 (로케일 지정)
     */
    public String get(String code, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    /**
     * 메시지 조회 (기본값 지정)
     */
    public String getOrDefault(String code, String defaultMessage) {
        return getOrDefault(code, defaultMessage, (Object[]) null);
    }

    /**
     * 메시지 조회 (기본값 + 파라미터)
     */
    public String getOrDefault(String code, String defaultMessage, Object... args) {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return defaultMessage;
        }
    }

    /**
     * 메시지 존재 여부
     */
    public boolean exists(String code) {
        try {
            messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }

    /**
     * 현재 로케일 조회
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * 로케일 설정
     */
    public void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }
}
