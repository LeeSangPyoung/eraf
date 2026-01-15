package com.eraf.core.i18n;

import com.eraf.core.response.ApiResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 메시지 국제화 AOP Aspect
 * @Message 어노테이션이 붙은 메서드의 반환값을 다국어 처리
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class MessageAspect {

    private static final Logger log = LoggerFactory.getLogger(MessageAspect.class);

    private final MessageSource messageSource;

    public MessageAspect(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * @Message 어노테이션이 붙은 메서드의 반환값 처리
     */
    @Around("@annotation(message)")
    public Object translateMessage(ProceedingJoinPoint joinPoint, Message message) throws Throwable {
        Object result = joinPoint.proceed();

        if (result == null) {
            return null;
        }

        String messageCode = message.value();
        String defaultMessage = message.defaultMessage();
        Locale locale = LocaleContextHolder.getLocale();

        // String 반환 타입인 경우
        if (result instanceof String) {
            return translateString(messageCode, defaultMessage, locale);
        }

        // ApiResponse 반환 타입인 경우 message 필드 번역
        if (result instanceof ApiResponse<?> response) {
            return translateApiResponse(response, messageCode, defaultMessage, locale);
        }

        return result;
    }

    /**
     * @TranslateResponse 어노테이션이 붙은 메서드 - ApiResponse의 메시지 자동 번역
     */
    @Around("@annotation(translateResponse)")
    public Object translateResponseMessage(ProceedingJoinPoint joinPoint, TranslateResponse translateResponse) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof ApiResponse<?> response) {
            String message = response.getMessage();
            if (message != null && !message.isEmpty()) {
                Locale locale = LocaleContextHolder.getLocale();
                String translated = translateString(message, message, locale);
                return ApiResponse.success(response.getData(), translated);
            }
        }

        return result;
    }

    private String translateString(String code, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            if (defaultMessage != null && !defaultMessage.isEmpty()) {
                return defaultMessage;
            }
            log.debug("No message found for code '{}', using code as fallback", code);
            return code;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ApiResponse<T> translateApiResponse(ApiResponse<T> response, String code, String defaultMessage, Locale locale) {
        String translatedMessage = translateString(code, defaultMessage, locale);

        if (response.isSuccess()) {
            return ApiResponse.success(response.getData(), translatedMessage);
        } else {
            return (ApiResponse<T>) ApiResponse.error(response.getCode(), translatedMessage);
        }
    }
}
