package com.eraf.http;

import com.eraf.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpResponseUtilsTest {

    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Nested
    @DisplayName("sendError")
    class SendErrorTests {

        @Test
        @DisplayName("ErrorCode로 에러 응답 전송")
        void sendErrorWithErrorCode() throws IOException {
            ErrorCode errorCode = new ErrorCode() {
                @Override public String getCode() { return "USER_NOT_FOUND"; }
                @Override public String getMessage() { return "사용자를 찾을 수 없습니다"; }
                @Override public int getStatus() { return 404; }
            };

            HttpResponseUtils.sendError(response, errorCode);
            printWriter.flush();

            verify(response).setStatus(404);
            verify(response).setContentType("application/json;charset=UTF-8");

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertFalse(json.get("success").asBoolean());
            assertEquals("USER_NOT_FOUND", json.get("code").asText());
            assertEquals("사용자를 찾을 수 없습니다", json.get("message").asText());
        }

        @Test
        @DisplayName("ErrorCode + 커스텀 메시지로 에러 응답 전송")
        void sendErrorWithCustomMessage() throws IOException {
            ErrorCode errorCode = new ErrorCode() {
                @Override public String getCode() { return "VALIDATION_ERROR"; }
                @Override public String getMessage() { return "기본 메시지"; }
                @Override public int getStatus() { return 400; }
            };

            HttpResponseUtils.sendError(response, errorCode, "이름은 필수입니다");
            printWriter.flush();

            verify(response).setStatus(400);

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertFalse(json.get("success").asBoolean());
            assertEquals("VALIDATION_ERROR", json.get("code").asText());
            assertEquals("이름은 필수입니다", json.get("message").asText());
        }

        @Test
        @DisplayName("직접 지정으로 에러 응답 전송")
        void sendErrorDirect() throws IOException {
            HttpResponseUtils.sendError(response, 500, "INTERNAL_ERROR", "서버 오류");
            printWriter.flush();

            verify(response).setStatus(500);
            verify(response).setContentType("application/json;charset=UTF-8");

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertFalse(json.get("success").asBoolean());
            assertEquals("INTERNAL_ERROR", json.get("code").asText());
            assertEquals("서버 오류", json.get("message").asText());
        }
    }

    @Nested
    @DisplayName("sendSuccess")
    class SendSuccessTests {

        @Test
        @DisplayName("데이터 없이 성공 응답")
        void sendSuccessNoData() throws IOException {
            HttpResponseUtils.sendSuccess(response);
            printWriter.flush();

            verify(response).setStatus(200);
            verify(response).setContentType("application/json;charset=UTF-8");

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertTrue(json.get("success").asBoolean());
        }

        @Test
        @DisplayName("데이터 포함 성공 응답")
        void sendSuccessWithData() throws IOException {
            HttpResponseUtils.sendSuccess(response, "hello");
            printWriter.flush();

            verify(response).setStatus(200);

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertTrue(json.get("success").asBoolean());
            assertEquals("hello", json.get("data").asText());
        }

        @Test
        @DisplayName("데이터 + 메시지 포함 성공 응답")
        void sendSuccessWithDataAndMessage() throws IOException {
            HttpResponseUtils.sendSuccess(response, 42, "조회 성공");
            printWriter.flush();

            verify(response).setStatus(200);

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertTrue(json.get("success").asBoolean());
            assertEquals(42, json.get("data").asInt());
            assertEquals("조회 성공", json.get("message").asText());
        }

        @Test
        @DisplayName("객체 데이터 성공 응답")
        void sendSuccessWithObjectData() throws IOException {
            record UserDto(String name, int age) {}
            HttpResponseUtils.sendSuccess(response, new UserDto("홍길동", 30));
            printWriter.flush();

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertTrue(json.get("success").asBoolean());
            assertEquals("홍길동", json.get("data").get("name").asText());
            assertEquals(30, json.get("data").get("age").asInt());
        }
    }

    @Nested
    @DisplayName("sendJson")
    class SendJsonTests {

        @Test
        @DisplayName("커스텀 상태 코드와 JSON 응답")
        void sendJsonCustomStatus() throws IOException {
            HttpResponseUtils.sendJson(response, 201, java.util.Map.of("id", 1));
            printWriter.flush();

            verify(response).setStatus(201);
            verify(response).setContentType("application/json;charset=UTF-8");

            JsonNode json = objectMapper.readTree(stringWriter.toString());
            assertEquals(1, json.get("id").asInt());
        }

        @Test
        @DisplayName("문자열 JSON 응답")
        void sendJsonString() throws IOException {
            HttpResponseUtils.sendJson(response, 200, "plain text");
            printWriter.flush();

            verify(response).setStatus(200);
            String written = stringWriter.toString();
            assertEquals("\"plain text\"", written);
        }
    }

    @Test
    @DisplayName("getObjectMapper - 인스턴스 반환")
    void getObjectMapper() {
        ObjectMapper om = HttpResponseUtils.getObjectMapper();
        assertNotNull(om);
    }
}
