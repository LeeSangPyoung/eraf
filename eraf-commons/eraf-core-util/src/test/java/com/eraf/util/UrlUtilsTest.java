package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UrlUtils - URL 유틸리티")
class UrlUtilsTest {

    @Nested
    @DisplayName("인코딩/디코딩")
    class EncodeDecode {

        @Test
        @DisplayName("encode UTF-8")
        void encode() {
            assertEquals("hello+world", UrlUtils.encode("hello world"));
            assertEquals("%ED%95%9C%EA%B8%80", UrlUtils.encode("한글"));
            assertNull(UrlUtils.encode(null));
        }

        @Test
        @DisplayName("decode UTF-8")
        void decode() {
            assertEquals("hello world", UrlUtils.decode("hello+world"));
            assertEquals("한글", UrlUtils.decode("%ED%95%9C%EA%B8%80"));
            assertNull(UrlUtils.decode(null));
        }

        @Test
        @DisplayName("라운드트립")
        void roundTrip() {
            String original = "hello world 한글 !@#$";
            assertEquals(original, UrlUtils.decode(UrlUtils.encode(original)));
        }
    }

    @Nested
    @DisplayName("URL 생성")
    class UrlBuilding {

        @Test
        @DisplayName("buildUrl - base + params")
        void buildUrlWithParams() {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("q", "test");
            params.put("page", "1");
            String url = UrlUtils.buildUrl("http://example.com", params);
            assertTrue(url.startsWith("http://example.com?"));
            assertTrue(url.contains("q=test"));
            assertTrue(url.contains("page=1"));
        }

        @Test
        @DisplayName("buildUrl - 이미 쿼리 있는 URL")
        void buildUrlExistingQuery() {
            Map<String, String> params = Map.of("b", "2");
            String url = UrlUtils.buildUrl("http://example.com?a=1", params);
            assertTrue(url.contains("a=1"));
            assertTrue(url.contains("b=2"));
            assertTrue(url.contains("&"));
        }

        @Test
        @DisplayName("buildUrl - base + path + params")
        void buildUrlWithPath() {
            Map<String, String> params = Map.of("q", "test");
            String url = UrlUtils.buildUrl("http://example.com", "/api/search", params);
            assertTrue(url.contains("/api/search"));
            assertTrue(url.contains("q=test"));
        }

        @Test
        @DisplayName("buildUrl - null/empty params")
        void buildUrlNullParams() {
            assertEquals("http://example.com", UrlUtils.buildUrl("http://example.com", (Map<String, String>) null));
            assertEquals("http://example.com", UrlUtils.buildUrl("http://example.com", Map.of()));
        }
    }

    @Nested
    @DisplayName("쿼리 스트링")
    class QueryString {

        @Test
        @DisplayName("buildQueryString")
        void buildQueryString() {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("a", "1");
            params.put("b", "2");
            String qs = UrlUtils.buildQueryString(params);
            assertTrue(qs.contains("a=1"));
            assertTrue(qs.contains("b=2"));
            assertEquals("", UrlUtils.buildQueryString(null));
        }

        @Test
        @DisplayName("parseQueryString")
        void parseQueryString() {
            Map<String, String> params = UrlUtils.parseQueryString("a=1&b=2");
            assertEquals("1", params.get("a"));
            assertEquals("2", params.get("b"));
        }

        @Test
        @DisplayName("parseQueryString - ? prefix")
        void parseQueryStringWithPrefix() {
            Map<String, String> params = UrlUtils.parseQueryString("?a=1");
            assertEquals("1", params.get("a"));
        }

        @Test
        @DisplayName("parseQueryString - null/empty")
        void parseQueryStringNull() {
            assertTrue(UrlUtils.parseQueryString(null).isEmpty());
            assertTrue(UrlUtils.parseQueryString("").isEmpty());
        }

        @Test
        @DisplayName("parseQueryStringMultiValue")
        void parseQueryStringMultiValue() {
            Map<String, List<String>> params = UrlUtils.parseQueryStringMultiValue("a=1&a=2&b=3");
            assertEquals(2, params.get("a").size());
            assertEquals(1, params.get("b").size());
        }
    }

    @Nested
    @DisplayName("URL 파싱")
    class UrlParsing {

        @Test
        @DisplayName("getDomain")
        void getDomain() {
            assertEquals("example.com", UrlUtils.getDomain("http://example.com/path"));
            assertNull(UrlUtils.getDomain(":::invalid"));
        }

        @Test
        @DisplayName("getProtocol")
        void getProtocol() {
            assertEquals("https", UrlUtils.getProtocol("https://example.com"));
            assertNull(UrlUtils.getProtocol(":::invalid"));
        }

        @Test
        @DisplayName("getPort")
        void getPort() {
            assertEquals(8080, UrlUtils.getPort("http://example.com:8080/path"));
            assertEquals(-1, UrlUtils.getPort("http://example.com/path"));
        }

        @Test
        @DisplayName("getPath")
        void getPath() {
            assertEquals("/api/test", UrlUtils.getPath("http://example.com/api/test?q=1"));
            assertNull(UrlUtils.getPath(":::invalid"));
        }

        @Test
        @DisplayName("getQueryString")
        void getQueryString() {
            assertEquals("q=test", UrlUtils.getQueryString("http://example.com?q=test"));
            assertNull(UrlUtils.getQueryString("http://example.com"));
        }

        @Test
        @DisplayName("getQueryParams / getQueryParam")
        void getQueryParams() {
            Map<String, String> params = UrlUtils.getQueryParams("http://example.com?a=1&b=2");
            assertEquals("1", params.get("a"));

            assertEquals("1", UrlUtils.getQueryParam("http://example.com?a=1", "a"));
        }
    }

    @Nested
    @DisplayName("URL 조작")
    class UrlManipulation {

        @Test
        @DisplayName("addQueryParam")
        void addQueryParam() {
            String url = UrlUtils.addQueryParam("http://example.com", "key", "value");
            assertTrue(url.contains("key=value"));
            assertNull(UrlUtils.addQueryParam(null, "key", "value"));
        }

        @Test
        @DisplayName("removeQueryParam")
        void removeQueryParam() {
            String url = UrlUtils.removeQueryParam("http://example.com?a=1&b=2", "a");
            assertFalse(url.contains("a=1"));
            assertTrue(url.contains("b=2"));
        }

        @Test
        @DisplayName("removeQueryString")
        void removeQueryString() {
            assertEquals("http://example.com", UrlUtils.removeQueryString("http://example.com?a=1"));
            assertEquals("http://example.com", UrlUtils.removeQueryString("http://example.com"));
            assertNull(UrlUtils.removeQueryString(null));
        }
    }

    @Nested
    @DisplayName("검증")
    class Validation {

        @Test
        @DisplayName("isValidUrl")
        void isValidUrl() {
            assertTrue(UrlUtils.isValidUrl("http://example.com"));
            assertTrue(UrlUtils.isValidUrl("https://example.com/path"));
            assertFalse(UrlUtils.isValidUrl("not-url"));
            assertFalse(UrlUtils.isValidUrl(null));
            assertFalse(UrlUtils.isValidUrl(""));
        }

        @Test
        @DisplayName("isHttpUrl / isHttpsUrl")
        void isHttpUrl() {
            assertTrue(UrlUtils.isHttpUrl("http://example.com"));
            assertTrue(UrlUtils.isHttpUrl("https://example.com"));
            assertFalse(UrlUtils.isHttpUrl("ftp://example.com"));

            assertTrue(UrlUtils.isHttpsUrl("https://example.com"));
            assertFalse(UrlUtils.isHttpsUrl("http://example.com"));
        }

        @Test
        @DisplayName("isLocalhost")
        void isLocalhost() {
            assertTrue(UrlUtils.isLocalhost("http://localhost:8080"));
            assertTrue(UrlUtils.isLocalhost("http://127.0.0.1:8080"));
            assertFalse(UrlUtils.isLocalhost("http://example.com"));
        }
    }

    @Nested
    @DisplayName("정규화")
    class Normalization {

        @Test
        @DisplayName("normalize")
        void normalize() {
            assertNotNull(UrlUtils.normalize("http://example.com/a/../b"));
            assertNull(UrlUtils.normalize(null));
        }

        @Test
        @DisplayName("joinPath")
        void joinPath() {
            assertEquals("http://example.com/api/test", UrlUtils.joinPath("http://example.com", "api", "test"));
            assertEquals("http://example.com/api", UrlUtils.joinPath("http://example.com/", "/api"));
            assertEquals("", UrlUtils.joinPath((String[]) null));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("getBaseUrl")
        void getBaseUrl() {
            assertEquals("http://example.com", UrlUtils.getBaseUrl("http://example.com/path?q=1"));
            assertEquals("http://example.com:8080", UrlUtils.getBaseUrl("http://example.com:8080/path"));
            assertNull(UrlUtils.getBaseUrl("invalid"));
        }

        @Test
        @DisplayName("toAbsoluteUrl")
        void toAbsoluteUrl() {
            assertEquals("http://example.com/api", UrlUtils.toAbsoluteUrl("http://example.com", "/api"));
            assertEquals("http://other.com", UrlUtils.toAbsoluteUrl("http://example.com", "http://other.com"));
        }

        @Test
        @DisplayName("getExtension")
        void getExtension() {
            assertEquals("html", UrlUtils.getExtension("http://example.com/page.html"));
            assertEquals("js", UrlUtils.getExtension("http://example.com/script.js?v=1"));
            assertNull(UrlUtils.getExtension("http://example.com/path"));
        }
    }
}
