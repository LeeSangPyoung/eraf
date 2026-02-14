package com.eraf.http;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    // ========== HttpUtils ==========
    @Nested
    @DisplayName("HttpUtils")
    class HttpUtilsTests {

        @Test
        @DisplayName("getClientIp - X-Forwarded-For 헤더")
        void getClientIpFromXForwardedFor() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
            assertEquals("192.168.1.1", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("getClientIp - X-Forwarded-For 다중 IP (첫번째)")
        void getClientIpMultipleIps() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2, 10.0.0.3");
            assertEquals("10.0.0.1", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("getClientIp - 헤더 없을 때 remoteAddr")
        void getClientIpFallback() {
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            assertEquals("127.0.0.1", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("getClientIp - unknown 값 무시")
        void getClientIpIgnoresUnknown() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
            when(request.getRemoteAddr()).thenReturn("192.168.0.1");
            assertEquals("192.168.0.1", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("getUserAgent")
        void getUserAgent() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Chrome/120");
            assertEquals("Mozilla/5.0 Chrome/120", HttpUtils.getUserAgent(request));
        }

        @Test
        @DisplayName("isAjax - true")
        void isAjaxTrue() {
            when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
            assertTrue(HttpUtils.isAjax(request));
        }

        @Test
        @DisplayName("isAjax - false")
        void isAjaxFalse() {
            assertFalse(HttpUtils.isAjax(request));
        }

        @Test
        @DisplayName("isMobile - Android")
        void isMobileAndroid() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Linux; Android 14)");
            assertTrue(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("isMobile - Desktop")
        void isMobileDesktop() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0)");
            assertFalse(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("isMobile - null UserAgent")
        void isMobileNullUA() {
            assertFalse(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("getBrowser - Chrome")
        void getBrowserChrome() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Chrome/120.0");
            assertEquals("Chrome", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - Edge")
        void getBrowserEdge() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Edg/120.0");
            assertEquals("Edge", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - Firefox")
        void getBrowserFirefox() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Firefox/120.0");
            assertEquals("Firefox", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - Safari")
        void getBrowserSafari() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Safari/605");
            assertEquals("Safari", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - Unknown")
        void getBrowserUnknown() {
            assertEquals("Unknown", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getOS - Windows")
        void getOSWindows() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0)");
            assertEquals("Windows", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - macOS")
        void getOSMac() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Mac OS X)");
            assertEquals("macOS", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - Linux")
        void getOSLinux() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (X11; Linux x86_64)");
            assertEquals("Linux", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - Android")
        void getOSAndroid() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Linux; Android 14)");
            assertEquals("Android", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - iOS (iPhone)")
        void getOSiOS() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPhone; CPU iPhone OS)");
            assertEquals("iOS", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getReferer")
        void getReferer() {
            when(request.getHeader("Referer")).thenReturn("https://example.com/page");
            assertEquals("https://example.com/page", HttpUtils.getReferer(request));
        }

        @Test
        @DisplayName("getRequestUrl - 쿼리스트링 있음")
        void getRequestUrlWithQuery() {
            StringBuffer sb = new StringBuffer("https://example.com/api/users");
            when(request.getRequestURL()).thenReturn(sb);
            when(request.getQueryString()).thenReturn("page=1&size=10");
            assertEquals("https://example.com/api/users?page=1&size=10", HttpUtils.getRequestUrl(request));
        }

        @Test
        @DisplayName("getRequestUrl - 쿼리스트링 없음")
        void getRequestUrlWithoutQuery() {
            StringBuffer sb = new StringBuffer("https://example.com/api/users");
            when(request.getRequestURL()).thenReturn(sb);
            assertEquals("https://example.com/api/users", HttpUtils.getRequestUrl(request));
        }

        @Test
        @DisplayName("extractBearerToken - 토큰 존재")
        void extractBearerTokenPresent() {
            when(request.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJIUzI1NiJ9.test");
            assertEquals("eyJhbGciOiJIUzI1NiJ9.test", HttpUtils.extractBearerToken(request));
        }

        @Test
        @DisplayName("extractBearerToken - 헤더 없음")
        void extractBearerTokenMissing() {
            assertNull(HttpUtils.extractBearerToken(request));
        }

        @Test
        @DisplayName("extractBearerToken - Bearer 접두사 없음")
        void extractBearerTokenNotBearer() {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
            assertNull(HttpUtils.extractBearerToken(request));
        }
    }

    // ========== CookieUtils ==========
    @Nested
    @DisplayName("CookieUtils")
    class CookieUtilsTests {

        @Test
        @DisplayName("get - 쿠키 존재")
        void getCookieValue() {
            Cookie cookie = new Cookie("token", "abc123");
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});

            Optional<String> result = CookieUtils.get(request, "token");
            assertTrue(result.isPresent());
            assertEquals("abc123", result.get());
        }

        @Test
        @DisplayName("get - 쿠키 없음")
        void getCookieValueNotFound() {
            when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "val")});

            Optional<String> result = CookieUtils.get(request, "token");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("get - 쿠키 배열 null")
        void getCookieValueNullCookies() {
            when(request.getCookies()).thenReturn(null);

            Optional<String> result = CookieUtils.get(request, "token");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getCookie - Cookie 객체 반환")
        void getCookieObject() {
            Cookie cookie = new Cookie("session", "xyz");
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});

            Optional<Cookie> result = CookieUtils.getCookie(request, "session");
            assertTrue(result.isPresent());
            assertEquals("session", result.get().getName());
        }

        @Test
        @DisplayName("set - 기본 쿠키 설정")
        void setCookie() {
            CookieUtils.set(response, "token", "value123");

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals("token", cookie.getName());
            assertEquals("value123", cookie.getValue());
            assertEquals("/", cookie.getPath());
            assertTrue(cookie.isHttpOnly());
        }

        @Test
        @DisplayName("set - maxAge 포함")
        void setCookieWithMaxAge() {
            CookieUtils.set(response, "token", "value", 3600);

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals(3600, cookie.getMaxAge());
        }

        @Test
        @DisplayName("delete - 쿠키 삭제")
        void deleteCookie() {
            CookieUtils.delete(response, "token");

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals("token", cookie.getName());
            assertNull(cookie.getValue());
            assertEquals(0, cookie.getMaxAge());
            assertEquals("/", cookie.getPath());
        }

        @Test
        @DisplayName("delete - 커스텀 path")
        void deleteCookieWithPath() {
            CookieUtils.delete(response, "token", "/api");

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals("/api", cookie.getPath());
            assertEquals(0, cookie.getMaxAge());
        }

        @Test
        @DisplayName("getCookie - 쿠키 배열 null")
        void getCookieObjectNullCookies() {
            when(request.getCookies()).thenReturn(null);
            Optional<Cookie> result = CookieUtils.getCookie(request, "session");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getCookie - 쿠키 없음")
        void getCookieObjectNotFound() {
            when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "val")});
            Optional<Cookie> result = CookieUtils.getCookie(request, "session");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("set - 상세 옵션 (domain, httpOnly, secure)")
        void setCookieWithAllOptions() {
            CookieUtils.set(response, "token", "val", 7200, "/api", "example.com", true, true);

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals("token", cookie.getName());
            assertEquals("val", cookie.getValue());
            assertEquals(7200, cookie.getMaxAge());
            assertEquals("/api", cookie.getPath());
            assertEquals("example.com", cookie.getDomain());
            assertTrue(cookie.isHttpOnly());
            assertTrue(cookie.getSecure());
        }

        @Test
        @DisplayName("set - 상세 옵션 (domain null)")
        void setCookieWithNullDomain() {
            CookieUtils.set(response, "token", "val", 3600, "/", null, false, false);

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertNull(cookie.getDomain());
            assertFalse(cookie.isHttpOnly());
            assertFalse(cookie.getSecure());
        }

        @Test
        @DisplayName("delete - path + domain")
        void deleteCookieWithPathAndDomain() {
            CookieUtils.delete(response, "token", "/api", "example.com");

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertEquals("token", cookie.getName());
            assertNull(cookie.getValue());
            assertEquals("/api", cookie.getPath());
            assertEquals("example.com", cookie.getDomain());
            assertEquals(0, cookie.getMaxAge());
        }

        @Test
        @DisplayName("delete - path + null domain")
        void deleteCookieWithPathAndNullDomain() {
            CookieUtils.delete(response, "token", "/", null);

            ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(captor.capture());

            Cookie cookie = captor.getValue();
            assertNull(cookie.getDomain());
            assertEquals(0, cookie.getMaxAge());
        }

        @Test
        @DisplayName("get - 여러 쿠키 중 일치하는 것 반환")
        void getCookieFromMultiple() {
            Cookie[] cookies = {
                new Cookie("a", "1"),
                new Cookie("b", "2"),
                new Cookie("c", "3")
            };
            when(request.getCookies()).thenReturn(cookies);

            Optional<String> result = CookieUtils.get(request, "b");
            assertTrue(result.isPresent());
            assertEquals("2", result.get());
        }
    }

    // ========== HttpUtils 추가 ==========
    @Nested
    @DisplayName("HttpUtils - 추가 케이스")
    class HttpUtilsAdditionalTests {

        @Test
        @DisplayName("getClientIp - Proxy-Client-IP 헤더")
        void getClientIpFromProxyClientIp() {
            // X-Forwarded-For는 null (기본값), Proxy-Client-IP에서 IP 반환
            lenient().when(request.getHeader(anyString())).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.5");
            assertEquals("10.0.0.5", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("getBrowser - Opera")
        void getBrowserOpera() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 OPR/100.0");
            assertEquals("Opera", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - Internet Explorer")
        void getBrowserIE() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (compatible; MSIE 11.0)");
            assertEquals("Internet Explorer", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getBrowser - IE Trident")
        void getBrowserIETrident() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Trident/7.0");
            assertEquals("Internet Explorer", HttpUtils.getBrowser(request));
        }

        @Test
        @DisplayName("getOS - Unknown")
        void getOSUnknown() {
            when(request.getHeader("User-Agent")).thenReturn("CustomBot/1.0");
            assertEquals("Unknown", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - null UserAgent")
        void getOSNullUA() {
            assertEquals("Unknown", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("getOS - iOS iPad")
        void getOSiPad() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPad; CPU OS 17_0)");
            assertEquals("iOS", HttpUtils.getOS(request));
        }

        @Test
        @DisplayName("isMobile - iPhone")
        void isMobileiPhone() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPhone; CPU iPhone OS)");
            assertTrue(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("isMobile - iPad")
        void isMobileiPad() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPad; CPU OS 17_0)");
            assertTrue(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("isMobile - iPod")
        void isMobileiPod() {
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (iPod touch; CPU iPhone OS)");
            assertTrue(HttpUtils.isMobile(request));
        }

        @Test
        @DisplayName("getReferer - null")
        void getRefererNull() {
            assertNull(HttpUtils.getReferer(request));
        }

        @Test
        @DisplayName("getUserAgent - null")
        void getUserAgentNull() {
            assertNull(HttpUtils.getUserAgent(request));
        }

        @Test
        @DisplayName("getClientIp - 빈 문자열 무시")
        void getClientIpIgnoresEmpty() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("");
            when(request.getRemoteAddr()).thenReturn("192.168.0.1");
            assertEquals("192.168.0.1", HttpUtils.getClientIp(request));
        }

        @Test
        @DisplayName("extractBearerToken - 'Bearer ' 정확히 7글자 후 토큰")
        void extractBearerTokenExact() {
            when(request.getHeader("Authorization")).thenReturn("Bearer x");
            assertEquals("x", HttpUtils.extractBearerToken(request));
        }
    }
}
