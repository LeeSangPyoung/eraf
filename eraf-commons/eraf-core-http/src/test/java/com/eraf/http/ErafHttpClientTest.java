package com.eraf.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ErafHttpClientTest {

    @Nested
    @DisplayName("Builder 패턴")
    class BuilderTests {

        @Test
        @DisplayName("create()로 인스턴스 생성")
        void createInstance() {
            ErafHttpClient client = ErafHttpClient.create();
            assertNotNull(client);
        }

        @Test
        @DisplayName("빌더 체이닝 - baseUrl, timeout, retry, header, bearerToken")
        void builderChaining() {
            ErafHttpClient client = ErafHttpClient.create()
                    .baseUrl("http://localhost:8080")
                    .timeout(Duration.ofSeconds(10))
                    .retry(3)
                    .header("X-Custom", "value")
                    .bearerToken("mytoken");
            assertNotNull(client);
        }
    }

    @Nested
    @DisplayName("buildUrl - URL 조합 로직")
    class BuildUrlTests {

        @Test
        @DisplayName("baseUrl 없으면 path 그대로")
        void noBaseUrl() throws Exception {
            ErafHttpClient client = ErafHttpClient.create();
            String result = invokeBuildUrl(client, "/api/test");
            assertEquals("/api/test", result);
        }

        @Test
        @DisplayName("baseUrl(끝/)  + path(시작/) -> 슬래시 중복 제거")
        void baseUrlTrailingSlashPathLeadingSlash() throws Exception {
            ErafHttpClient client = ErafHttpClient.create().baseUrl("http://localhost:8080/");
            String result = invokeBuildUrl(client, "/api/test");
            assertEquals("http://localhost:8080/api/test", result);
        }

        @Test
        @DisplayName("baseUrl(끝 없음) + path(시작 없음) -> 슬래시 추가")
        void baseUrlNoSlashPathNoSlash() throws Exception {
            ErafHttpClient client = ErafHttpClient.create().baseUrl("http://localhost:8080");
            String result = invokeBuildUrl(client, "api/test");
            assertEquals("http://localhost:8080/api/test", result);
        }

        @Test
        @DisplayName("baseUrl(끝 없음) + path(시작/) -> 그대로 결합")
        void baseUrlNoSlashPathWithSlash() throws Exception {
            ErafHttpClient client = ErafHttpClient.create().baseUrl("http://localhost:8080");
            String result = invokeBuildUrl(client, "/api/test");
            assertEquals("http://localhost:8080/api/test", result);
        }

        @Test
        @DisplayName("baseUrl(끝/) + path(시작 없음) -> 그대로 결합")
        void baseUrlWithSlashPathNoSlash() throws Exception {
            ErafHttpClient client = ErafHttpClient.create().baseUrl("http://localhost:8080/");
            String result = invokeBuildUrl(client, "api/test");
            assertEquals("http://localhost:8080/api/test", result);
        }

        @Test
        @DisplayName("빈 baseUrl은 path 그대로 반환")
        void emptyBaseUrl() throws Exception {
            ErafHttpClient client = ErafHttpClient.create().baseUrl("");
            String result = invokeBuildUrl(client, "/api/test");
            assertEquals("/api/test", result);
        }

        private String invokeBuildUrl(ErafHttpClient client, String path) throws Exception {
            Method method = ErafHttpClient.class.getDeclaredMethod("buildUrl", String.class);
            method.setAccessible(true);
            return (String) method.invoke(client, path);
        }
    }

    @Nested
    @DisplayName("HttpException")
    class HttpExceptionTests {

        @Test
        @DisplayName("상태 코드와 메시지 저장")
        void httpException() {
            ErafHttpClient.HttpException ex = new ErafHttpClient.HttpException(404, "Not Found");
            assertEquals(404, ex.getStatusCode());
            assertEquals("Not Found", ex.getMessage());
        }

        @Test
        @DisplayName("IOException 상속 확인")
        void isIOException() {
            ErafHttpClient.HttpException ex = new ErafHttpClient.HttpException(500, "Error");
            assertInstanceOf(java.io.IOException.class, ex);
        }
    }
}
