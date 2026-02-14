package com.eraf.i18n;

import com.eraf.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.lang.annotation.Annotation;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class I18nTest {

    // ========== MessageService ==========
    @Nested
    @DisplayName("MessageService")
    class MessageServiceTest {

        @Mock
        private MessageSource messageSource;

        private MessageService service;

        @BeforeEach
        void setUp() {
            service = new MessageService(messageSource);
            LocaleContextHolder.setLocale(Locale.KOREA);
        }

        @AfterEach
        void tearDown() {
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        @DisplayName("메시지 조회 (코드만)")
        void getMessage() {
            when(messageSource.getMessage("hello", null, Locale.KOREA)).thenReturn("안녕하세요");

            assertEquals("안녕하세요", service.get("hello"));
        }

        @Test
        @DisplayName("메시지 조회 (파라미터)")
        void getMessageWithArgs() {
            when(messageSource.getMessage(eq("greeting"), any(Object[].class), eq(Locale.KOREA)))
                    .thenReturn("안녕하세요, 홍길동님");

            assertEquals("안녕하세요, 홍길동님", service.get("greeting", "홍길동"));
        }

        @Test
        @DisplayName("메시지 조회 (로케일 지정)")
        void getMessageWithLocale() {
            when(messageSource.getMessage(eq("hello"), isNull(), eq(Locale.US)))
                    .thenReturn("Hello");

            assertEquals("Hello", service.get("hello", Locale.US, (Object[]) null));
        }

        @Test
        @DisplayName("메시지 없을 때 코드 반환")
        void getMessageNotFound() {
            when(messageSource.getMessage(eq("unknown"), isNull(), any(Locale.class)))
                    .thenThrow(new NoSuchMessageException("unknown"));

            assertEquals("unknown", service.get("unknown"));
        }

        @Test
        @DisplayName("getOrDefault - 메시지 존재")
        void getOrDefaultFound() {
            when(messageSource.getMessage("hello", null, Locale.KOREA)).thenReturn("안녕하세요");

            assertEquals("안녕하세요", service.getOrDefault("hello", "기본"));
        }

        @Test
        @DisplayName("getOrDefault - 메시지 없을 때 기본값 반환")
        void getOrDefaultNotFound() {
            when(messageSource.getMessage(eq("unknown"), isNull(), any(Locale.class)))
                    .thenThrow(new NoSuchMessageException("unknown"));

            assertEquals("기본 메시지", service.getOrDefault("unknown", "기본 메시지"));
        }

        @Test
        @DisplayName("exists - 존재하는 메시지")
        void existsTrue() {
            when(messageSource.getMessage("hello", null, Locale.KOREA)).thenReturn("안녕하세요");

            assertTrue(service.exists("hello"));
        }

        @Test
        @DisplayName("exists - 존재하지 않는 메시지")
        void existsFalse() {
            when(messageSource.getMessage(eq("unknown"), isNull(), any(Locale.class)))
                    .thenThrow(new NoSuchMessageException("unknown"));

            assertFalse(service.exists("unknown"));
        }

        @Test
        @DisplayName("현재 로케일 조회")
        void getCurrentLocale() {
            assertEquals(Locale.KOREA, service.getCurrentLocale());
        }

        @Test
        @DisplayName("로케일 설정")
        void setLocale() {
            service.setLocale(Locale.US);
            assertEquals(Locale.US, service.getCurrentLocale());
        }
    }

    // ========== ErafLocaleResolver ==========
    @Nested
    @DisplayName("ErafLocaleResolver")
    class ErafLocaleResolverTest {

        @Mock
        private HttpServletRequest request;

        @Mock
        private HttpServletResponse response;

        @Mock
        private HttpSession session;

        private ErafLocaleResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new ErafLocaleResolver();
        }

        @AfterEach
        void tearDown() {
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        @DisplayName("세션에서 로케일 결정")
        void resolveFromSession() {
            when(request.getSession(false)).thenReturn(session);
            when(request.getSession()).thenReturn(session);
            when(session.getAttribute(ErafLocaleResolver.LOCALE_SESSION_ATTRIBUTE))
                    .thenReturn(Locale.US);

            assertEquals(Locale.US, resolver.resolveLocale(request));
        }

        @Test
        @DisplayName("쿠키에서 로케일 결정")
        void resolveFromCookie() {
            when(request.getSession(false)).thenReturn(null);
            Cookie cookie = new Cookie(ErafLocaleResolver.LOCALE_COOKIE_NAME, "en-US");
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});

            Locale resolved = resolver.resolveLocale(request);
            assertEquals("en", resolved.getLanguage());
            assertEquals("US", resolved.getCountry());
        }

        @Test
        @DisplayName("Accept-Language 헤더에서 로케일 결정")
        void resolveFromHeader() {
            when(request.getSession(false)).thenReturn(null);
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader(ErafLocaleResolver.ACCEPT_LANGUAGE_HEADER))
                    .thenReturn("ja, en-US;q=0.9");

            Locale resolved = resolver.resolveLocale(request);
            assertEquals("ja", resolved.getLanguage());
        }

        @Test
        @DisplayName("모든 소스 없을 때 기본 로케일 (한국)")
        void resolveDefault() {
            when(request.getSession(false)).thenReturn(null);
            when(request.getCookies()).thenReturn(null);

            assertEquals(Locale.KOREA, resolver.resolveLocale(request));
        }

        @Test
        @DisplayName("setLocale - 세션에 저장")
        void setLocale() {
            when(request.getSession(true)).thenReturn(session);

            resolver.setLocale(request, response, Locale.JAPAN);

            verify(session).setAttribute(ErafLocaleResolver.LOCALE_SESSION_ATTRIBUTE, Locale.JAPAN);
        }

        @Test
        @DisplayName("setLocale - null이면 기본 로케일")
        void setLocaleNull() {
            when(request.getSession(true)).thenReturn(session);

            resolver.setLocale(request, response, null);

            verify(session).setAttribute(ErafLocaleResolver.LOCALE_SESSION_ATTRIBUTE, Locale.KOREA);
        }

        @Test
        @DisplayName("기본 로케일 변경")
        void setDefaultLocale() {
            resolver.setDefaultLocale(Locale.US);
            assertEquals(Locale.US, resolver.getDefaultLocale());
        }

        @Test
        @DisplayName("기본 로케일 null 설정 시 한국어")
        void setDefaultLocaleNull() {
            resolver.setDefaultLocale(null);
            assertEquals(Locale.KOREA, resolver.getDefaultLocale());
        }

        @Test
        @DisplayName("쿠키 배열이 빈 배열이면 헤더로 폴백")
        void resolveFromEmptyCookies() {
            when(request.getSession(false)).thenReturn(null);
            when(request.getCookies()).thenReturn(new Cookie[]{});
            when(request.getHeader(ErafLocaleResolver.ACCEPT_LANGUAGE_HEADER))
                    .thenReturn("fr");

            Locale resolved = resolver.resolveLocale(request);
            assertEquals("fr", resolved.getLanguage());
        }

        @Test
        @DisplayName("세션에 Locale이 아닌 객체가 있으면 무시")
        void resolveSessionNonLocaleAttribute() {
            when(request.getSession(false)).thenReturn(session);
            when(request.getSession()).thenReturn(session);
            when(session.getAttribute(ErafLocaleResolver.LOCALE_SESSION_ATTRIBUTE))
                    .thenReturn("not-a-locale");
            when(request.getCookies()).thenReturn(null);

            assertEquals(Locale.KOREA, resolver.resolveLocale(request));
        }

        @Test
        @DisplayName("Accept-Language 빈 문자열이면 기본값")
        void resolveEmptyAcceptLanguage() {
            when(request.getSession(false)).thenReturn(null);
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader(ErafLocaleResolver.ACCEPT_LANGUAGE_HEADER))
                    .thenReturn("");

            assertEquals(Locale.KOREA, resolver.resolveLocale(request));
        }

        @Test
        @DisplayName("상수 필드 확인")
        void constantFields() {
            assertEquals("ERAF_LOCALE", ErafLocaleResolver.LOCALE_SESSION_ATTRIBUTE);
            assertEquals("ERAF_LOCALE", ErafLocaleResolver.LOCALE_COOKIE_NAME);
            assertEquals("Accept-Language", ErafLocaleResolver.ACCEPT_LANGUAGE_HEADER);
        }
    }

    // ========== MessageAspect ==========
    @Nested
    @DisplayName("MessageAspect")
    class MessageAspectTest {

        @Mock
        private MessageSource messageSource;

        @Mock
        private ProceedingJoinPoint joinPoint;

        private MessageAspect aspect;

        @BeforeEach
        void setUp() {
            aspect = new MessageAspect(messageSource);
            LocaleContextHolder.setLocale(Locale.KOREA);
        }

        @AfterEach
        void tearDown() {
            LocaleContextHolder.resetLocaleContext();
        }

        private Message createMessage(String value, String defaultMessage) {
            return new Message() {
                @Override public String value() { return value; }
                @Override public String defaultMessage() { return defaultMessage; }
                @Override public Class<? extends Annotation> annotationType() { return Message.class; }
            };
        }

        @Test
        @DisplayName("String 반환 - 번역 성공")
        void translateStringResult() throws Throwable {
            when(joinPoint.proceed()).thenReturn("original");
            when(messageSource.getMessage("msg.hello", null, Locale.KOREA))
                    .thenReturn("안녕하세요");

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.hello", ""));
            assertEquals("안녕하세요", result);
        }

        @Test
        @DisplayName("String 반환 - 메시지 없으면 기본 메시지")
        void translateStringFallbackDefault() throws Throwable {
            when(joinPoint.proceed()).thenReturn("original");
            when(messageSource.getMessage("msg.unknown", null, Locale.KOREA))
                    .thenThrow(new NoSuchMessageException("msg.unknown"));

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.unknown", "기본값"));
            assertEquals("기본값", result);
        }

        @Test
        @DisplayName("String 반환 - 기본 메시지도 없으면 코드 반환")
        void translateStringFallbackCode() throws Throwable {
            when(joinPoint.proceed()).thenReturn("original");
            when(messageSource.getMessage("msg.unknown", null, Locale.KOREA))
                    .thenThrow(new NoSuchMessageException("msg.unknown"));

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.unknown", ""));
            assertEquals("msg.unknown", result);
        }

        @Test
        @DisplayName("null 반환 시 그대로 null")
        void translateNullResult() throws Throwable {
            when(joinPoint.proceed()).thenReturn(null);

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.hello", ""));
            assertNull(result);
        }

        @Test
        @DisplayName("ApiResponse 반환 - 성공 응답 번역")
        void translateApiResponseSuccess() throws Throwable {
            when(joinPoint.proceed()).thenReturn(ApiResponse.success("data"));
            when(messageSource.getMessage("msg.success", null, Locale.KOREA))
                    .thenReturn("성공했습니다");

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.success", ""));
            assertInstanceOf(ApiResponse.class, result);
            ApiResponse<?> response = (ApiResponse<?>) result;
            assertTrue(response.isSuccess());
            assertEquals("성공했습니다", response.getMessage());
        }

        @Test
        @DisplayName("ApiResponse 반환 - 에러 응답 번역")
        void translateApiResponseError() throws Throwable {
            when(joinPoint.proceed()).thenReturn(ApiResponse.error("ERR001", "original"));
            when(messageSource.getMessage("msg.error", null, Locale.KOREA))
                    .thenReturn("에러 발생");

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.error", ""));
            assertInstanceOf(ApiResponse.class, result);
            ApiResponse<?> response = (ApiResponse<?>) result;
            assertFalse(response.isSuccess());
            assertEquals("에러 발생", response.getMessage());
        }

        @Test
        @DisplayName("기타 타입 반환 시 그대로 반환")
        void translateOtherTypeResult() throws Throwable {
            when(joinPoint.proceed()).thenReturn(42);

            Object result = aspect.translateMessage(joinPoint, createMessage("msg.hello", ""));
            assertEquals(42, result);
        }

        @Test
        @DisplayName("@TranslateResponse - ApiResponse 메시지 번역")
        void translateResponseMessage() throws Throwable {
            when(joinPoint.proceed()).thenReturn(ApiResponse.success("data", "msg.done"));
            when(messageSource.getMessage("msg.done", null, Locale.KOREA))
                    .thenReturn("완료되었습니다");

            TranslateResponse annotation = new TranslateResponse() {
                @Override public Class<? extends Annotation> annotationType() { return TranslateResponse.class; }
            };

            Object result = aspect.translateResponseMessage(joinPoint, annotation);
            assertInstanceOf(ApiResponse.class, result);
            ApiResponse<?> response = (ApiResponse<?>) result;
            assertEquals("완료되었습니다", response.getMessage());
        }

        @Test
        @DisplayName("@TranslateResponse - 메시지 없으면 그대로")
        void translateResponseNullMessage() throws Throwable {
            when(joinPoint.proceed()).thenReturn(ApiResponse.success("data"));

            TranslateResponse annotation = new TranslateResponse() {
                @Override public Class<? extends Annotation> annotationType() { return TranslateResponse.class; }
            };

            Object result = aspect.translateResponseMessage(joinPoint, annotation);
            assertInstanceOf(ApiResponse.class, result);
        }

        @Test
        @DisplayName("@TranslateResponse - ApiResponse 아닌 경우 그대로")
        void translateResponseNonApiResponse() throws Throwable {
            when(joinPoint.proceed()).thenReturn("plain string");

            TranslateResponse annotation = new TranslateResponse() {
                @Override public Class<? extends Annotation> annotationType() { return TranslateResponse.class; }
            };

            Object result = aspect.translateResponseMessage(joinPoint, annotation);
            assertEquals("plain string", result);
        }
    }

    // ========== Message 어노테이션 ==========
    @Nested
    @DisplayName("Message 어노테이션")
    class MessageAnnotationTest {

        @Test
        @DisplayName("기본값 확인")
        void defaultValues() throws NoSuchMethodException {
            Message msg = MessageAnnotationTest.class.getDeclaredMethod("annotatedMethod")
                    .getAnnotation(Message.class);
            assertEquals("test.code", msg.value());
            assertEquals("", msg.defaultMessage());
        }

        @Test
        @DisplayName("커스텀 기본 메시지")
        void customDefaultMessage() throws NoSuchMethodException {
            Message msg = MessageAnnotationTest.class.getDeclaredMethod("annotatedMethodWithDefault")
                    .getAnnotation(Message.class);
            assertEquals("test.code2", msg.value());
            assertEquals("기본 메시지", msg.defaultMessage());
        }

        @Message("test.code")
        void annotatedMethod() {}

        @Message(value = "test.code2", defaultMessage = "기본 메시지")
        void annotatedMethodWithDefault() {}
    }

    // ========== TranslateResponse 어노테이션 ==========
    @Nested
    @DisplayName("TranslateResponse 어노테이션")
    class TranslateResponseAnnotationTest {

        @Test
        @DisplayName("어노테이션 존재 확인")
        void annotationPresent() throws NoSuchMethodException {
            assertTrue(TranslateResponseAnnotationTest.class.getDeclaredMethod("annotatedMethod")
                    .isAnnotationPresent(TranslateResponse.class));
        }

        @TranslateResponse
        void annotatedMethod() {}
    }
}
