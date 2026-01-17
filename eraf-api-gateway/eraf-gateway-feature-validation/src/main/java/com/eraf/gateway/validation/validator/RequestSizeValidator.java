package com.eraf.gateway.validation.validator;

import com.eraf.gateway.validation.domain.ValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Request Size Validator
 * 요청 바디 크기 검증
 */
@Slf4j
@Component
public class RequestSizeValidator {

    /**
     * 요청 바디 크기 검증
     *
     * @param request     HTTP 요청
     * @param maxBodySize 최대 바디 크기 (bytes)
     * @return 검증 결과
     */
    public ValidationResult validate(HttpServletRequest request, long maxBodySize) {
        try {
            // Content-Length 헤더로 빠른 검증
            int contentLength = request.getContentLength();
            if (contentLength > 0) {
                if (contentLength > maxBodySize) {
                    return ValidationResult.failure(
                            String.format("Request body size (%d bytes) exceeds maximum allowed size (%d bytes)",
                                    contentLength, maxBodySize)
                    );
                }
                return ValidationResult.success();
            }

            // Content-Length가 없는 경우, 실제로 읽어보며 검증
            return validateByReading(request, maxBodySize);

        } catch (Exception e) {
            log.error("Failed to validate request body size", e);
            return ValidationResult.failure("Failed to validate request body size: " + e.getMessage());
        }
    }

    /**
     * 실제로 읽어보며 크기 검증
     * (Content-Length가 없는 경우)
     */
    private ValidationResult validateByReading(HttpServletRequest request, long maxBodySize) throws IOException {
        try (InputStream inputStream = request.getInputStream()) {
            long totalBytes = 0;
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes > maxBodySize) {
                    return ValidationResult.failure(
                            String.format("Request body size exceeds maximum allowed size (%d bytes)", maxBodySize)
                    );
                }
            }

            return ValidationResult.success();
        }
    }

    /**
     * Content-Length만 검증 (빠른 검증)
     */
    public ValidationResult validateContentLength(HttpServletRequest request, long maxBodySize) {
        int contentLength = request.getContentLength();

        if (contentLength < 0) {
            // Content-Length가 없으면 검증 패스
            return ValidationResult.success();
        }

        if (contentLength > maxBodySize) {
            return ValidationResult.failure(
                    String.format("Request body size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            contentLength, maxBodySize)
            );
        }

        return ValidationResult.success();
    }

    /**
     * 바이트 크기를 사람이 읽기 쉬운 형식으로 변환
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
