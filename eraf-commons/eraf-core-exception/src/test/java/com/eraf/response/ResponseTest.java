package com.eraf.response;

import com.eraf.exception.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    // ========== ApiResponse ==========
    @Nested
    @DisplayName("ApiResponse")
    class ApiResponseTest {

        @Test
        @DisplayName("성공 응답 (데이터 없음)")
        void successNoData() {
            ApiResponse<Void> response = ApiResponse.success();

            assertTrue(response.isSuccess());
            assertNull(response.getData());
            assertNull(response.getCode());
            assertNull(response.getMessage());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("성공 응답 (데이터 포함)")
        void successWithData() {
            ApiResponse<String> response = ApiResponse.success("result");

            assertTrue(response.isSuccess());
            assertEquals("result", response.getData());
        }

        @Test
        @DisplayName("성공 응답 (데이터 + 메시지)")
        void successWithDataAndMessage() {
            ApiResponse<Integer> response = ApiResponse.success(42, "계산 완료");

            assertTrue(response.isSuccess());
            assertEquals(42, response.getData());
            assertEquals("계산 완료", response.getMessage());
        }

        @Test
        @DisplayName("에러 응답 (코드 + 메시지)")
        void errorWithCodeAndMessage() {
            ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", "리소스 없음");

            assertFalse(response.isSuccess());
            assertNull(response.getData());
            assertEquals("NOT_FOUND", response.getCode());
            assertEquals("리소스 없음", response.getMessage());
        }

        @Test
        @DisplayName("에러 응답 (ErrorCode)")
        void errorWithErrorCode() {
            ApiResponse<Void> response = ApiResponse.error(CommonErrorCode.UNAUTHORIZED);

            assertFalse(response.isSuccess());
            assertEquals("UNAUTHORIZED", response.getCode());
            assertEquals("인증이 필요합니다.", response.getMessage());
        }

        @Test
        @DisplayName("에러 응답 (ErrorCode + 커스텀 메시지)")
        void errorWithErrorCodeAndCustomMessage() {
            ApiResponse<Void> response = ApiResponse.error(CommonErrorCode.FORBIDDEN, "관리자만 접근 가능");

            assertFalse(response.isSuccess());
            assertEquals("FORBIDDEN", response.getCode());
            assertEquals("관리자만 접근 가능", response.getMessage());
        }
    }

    // ========== ErrorResponse ==========
    @Nested
    @DisplayName("ErrorResponse (RFC 7807)")
    class ErrorResponseTest {

        @Test
        @DisplayName("빌더로 생성")
        void builderBasic() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(400)
                    .code("VALIDATION_ERROR")
                    .message("입력값 검증 실패")
                    .build();

            assertEquals(400, response.getStatus());
            assertEquals("VALIDATION_ERROR", response.getCode());
            assertEquals("입력값 검증 실패", response.getMessage());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("필드 에러 포함")
        void withFieldErrors() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(400)
                    .code("VALIDATION_ERROR")
                    .message("검증 실패")
                    .fieldError("email", "이메일 형식 오류")
                    .fieldError("name", "이름 필수")
                    .build();

            assertNotNull(response.getFieldErrors());
            assertEquals(2, response.getFieldErrors().size());
            assertEquals("이메일 형식 오류", response.getFieldErrors().get("email"));
        }

        @Test
        @DisplayName("필드 에러 맵으로 추가")
        void withFieldErrorsMap() {
            Map<String, String> errors = Map.of("a", "err1", "b", "err2");
            ErrorResponse response = ErrorResponse.builder()
                    .status(400)
                    .code("VALIDATION_ERROR")
                    .message("검증 실패")
                    .fieldErrors(errors)
                    .build();

            assertEquals(2, response.getFieldErrors().size());
        }

        @Test
        @DisplayName("null 필드 에러 맵 무시")
        void nullFieldErrorsMap() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(400)
                    .code("ERR")
                    .message("msg")
                    .fieldErrors(null)
                    .build();

            assertNull(response.getFieldErrors());
        }

        @Test
        @DisplayName("detail과 instance 설정")
        void withDetailAndInstance() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(500)
                    .code("INTERNAL_ERROR")
                    .message("서버 오류")
                    .detail("상세 설명")
                    .instance("/api/users/123")
                    .build();

            assertEquals("상세 설명", response.getDetail());
            assertEquals("/api/users/123", response.getInstance());
        }

        @Test
        @DisplayName("필드 에러 없으면 fieldErrors는 null")
        void noFieldErrors() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(500)
                    .code("ERR")
                    .message("msg")
                    .build();

            assertNull(response.getFieldErrors());
        }
    }

    // ========== PageResponse ==========
    @Nested
    @DisplayName("PageResponse")
    class PageResponseTest {

        @Test
        @DisplayName("기본 페이징 응답")
        void basicPageResponse() {
            List<String> items = List.of("a", "b", "c");
            PageResponse<String> page = PageResponse.of(items, 0, 10, 25);

            assertEquals(items, page.getContent());
            assertEquals(0, page.getPage());
            assertEquals(10, page.getSize());
            assertEquals(25, page.getTotal());
            assertEquals(3, page.getTotalPages());
            assertEquals(3, page.getNumberOfElements());
            assertFalse(page.isEmpty());
        }

        @Test
        @DisplayName("첫 페이지 플래그")
        void firstPage() {
            PageResponse<String> page = PageResponse.of(List.of("a"), 0, 10, 25);

            assertTrue(page.isFirst());
            assertFalse(page.isLast());
            assertTrue(page.isHasNext());
            assertFalse(page.isHasPrevious());
        }

        @Test
        @DisplayName("마지막 페이지 플래그")
        void lastPage() {
            PageResponse<String> page = PageResponse.of(List.of("a"), 2, 10, 25);

            assertFalse(page.isFirst());
            assertTrue(page.isLast());
            assertFalse(page.isHasNext());
            assertTrue(page.isHasPrevious());
        }

        @Test
        @DisplayName("중간 페이지")
        void middlePage() {
            PageResponse<String> page = PageResponse.of(List.of("a", "b"), 1, 10, 30);

            assertFalse(page.isFirst());
            assertFalse(page.isLast());
            assertTrue(page.isHasNext());
            assertTrue(page.isHasPrevious());
        }

        @Test
        @DisplayName("빈 응답")
        void emptyResponse() {
            PageResponse<String> page = PageResponse.empty();

            assertTrue(page.isEmpty());
            assertEquals(0, page.getNumberOfElements());
            assertEquals(0, page.getPage());
            assertEquals(0, page.getTotal());
            assertEquals(0, page.getTotalPages());
        }

        @Test
        @DisplayName("빈 응답 (size 지정)")
        void emptyWithSize() {
            PageResponse<String> page = PageResponse.empty(20);

            assertTrue(page.isEmpty());
            assertEquals(20, page.getSize());
            assertEquals(0, page.getTotal());
            assertEquals(0, page.getTotalPages());
        }

        @Test
        @DisplayName("null content 처리")
        void nullContent() {
            PageResponse<String> page = PageResponse.of(null, 0, 10, 0);

            assertNotNull(page.getContent());
            assertTrue(page.isEmpty());
        }

        @Test
        @DisplayName("size 0일 때 totalPages 0")
        void sizeZero() {
            PageResponse<String> page = PageResponse.of(Collections.emptyList(), 0, 0, 0);
            assertEquals(0, page.getTotalPages());
        }

        @Test
        @DisplayName("단일 페이지 (total <= size)")
        void singlePage() {
            PageResponse<String> page = PageResponse.of(List.of("a", "b"), 0, 10, 2);

            assertEquals(1, page.getTotalPages());
            assertTrue(page.isFirst());
            assertTrue(page.isLast());
            assertFalse(page.isHasNext());
            assertFalse(page.isHasPrevious());
        }
    }
}
