package com.eraf.gateway.validation.validator;

import com.eraf.gateway.validation.domain.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Content-Type Validator
 * 허용된 Content-Type 검증
 */
@Slf4j
@Component
public class ContentTypeValidator {

    /**
     * Content-Type 검증
     *
     * @param request            HTTP 요청
     * @param allowedContentTypes 허용된 Content-Type 목록
     * @return 검증 결과
     */
    public ValidationResult validate(HttpServletRequest request, List<String> allowedContentTypes) {
        if (allowedContentTypes == null || allowedContentTypes.isEmpty()) {
            return ValidationResult.success();
        }

        String contentType = request.getContentType();

        // Content-Type이 없는 경우
        if (contentType == null || contentType.isEmpty()) {
            // GET, DELETE 등은 바디가 없을 수 있으므로 허용
            String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method) ||
                "DELETE".equalsIgnoreCase(method) ||
                "HEAD".equalsIgnoreCase(method)) {
                return ValidationResult.success();
            }

            return ValidationResult.failure("Content-Type header is required");
        }

        // Content-Type 정규화 (charset 등 제거)
        String normalizedContentType = normalizeContentType(contentType);

        // 허용 목록 확인
        boolean isAllowed = allowedContentTypes.stream()
                .anyMatch(allowed -> matchesContentType(normalizedContentType, normalizeContentType(allowed)));

        if (!isAllowed) {
            return ValidationResult.failure(
                    String.format("Content-Type '%s' is not allowed. Allowed types: %s",
                            contentType, allowedContentTypes)
            );
        }

        return ValidationResult.success();
    }

    /**
     * Content-Type 정규화
     * "application/json;charset=UTF-8" -> "application/json"
     */
    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        // 세미콜론 이전 부분만 추출
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex > 0) {
            contentType = contentType.substring(0, semicolonIndex);
        }

        return contentType.trim().toLowerCase();
    }

    /**
     * Content-Type 매칭 확인
     * 와일드카드 지원 (예: "application/*", "*\/*")
     */
    private boolean matchesContentType(String contentType, String pattern) {
        if (pattern.equals("*/*")) {
            return true;
        }

        // "application/*" 같은 와일드카드 패턴 처리
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return contentType.startsWith(prefix);
        }

        return contentType.equals(pattern);
    }

    /**
     * 일반적인 Content-Type 상수
     */
    public static final class ContentTypes {
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_XML = "application/xml";
        public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_XML = "text/xml";
        public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
        public static final String APPLICATION_PDF = "application/pdf";

        private ContentTypes() {
        }
    }
}
