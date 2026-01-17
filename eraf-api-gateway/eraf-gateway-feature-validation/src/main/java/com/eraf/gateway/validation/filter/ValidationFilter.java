package com.eraf.gateway.validation.filter;

import com.eraf.gateway.common.filter.GatewayFilter;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import com.eraf.gateway.validation.domain.ValidationResult;
import com.eraf.gateway.validation.domain.ValidationRule;
import com.eraf.gateway.validation.exception.ValidationException;
import com.eraf.gateway.validation.repository.ValidationRuleRepository;
import com.eraf.gateway.validation.service.ValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Optional;

/**
 * Validation Filter
 * AWS API Gateway 스타일 요청 검증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class ValidationFilter extends GatewayFilter {

    private final ValidationService validationService;
    private final ValidationRuleRepository ruleRepository;
    private final boolean enabled;

    @Override
    protected boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 검증 규칙 조회
        Optional<ValidationRule> ruleOpt = ruleRepository.findByPathAndMethod(path, method);

        if (ruleOpt.isEmpty()) {
            // 검증 규칙이 없으면 통과
            chain.doFilter(request, response);
            return;
        }

        ValidationRule rule = ruleOpt.get();

        if (!rule.isEnabled()) {
            // 규칙이 비활성화되어 있으면 통과
            chain.doFilter(request, response);
            return;
        }

        try {
            // Request Body를 여러 번 읽을 수 있도록 래핑
            HttpServletRequest wrappedRequest = request;
            if (shouldWrapRequest(request, rule)) {
                wrappedRequest = new ContentCachingRequestWrapper(request);
            }

            // 검증 수행
            ValidationResult validationResult = validationService.validateRequest(wrappedRequest, rule);

            if (validationResult.hasErrors()) {
                log.warn("Validation failed for {} {}: {}", method, path, validationResult.getErrors());

                // 검증 실패 응답
                sendValidationErrorResponse(httpResponse, validationResult);
                return;
            }

            // 검증 성공, 다음 필터로 진행
            chain.doFilter(wrappedRequest, response);

        } catch (ValidationException e) {
            log.warn("Validation exception for {} {}: {}", method, path, e.getMessage());
            sendValidationErrorResponse(httpResponse, e.getValidationResult());

        } catch (Exception e) {
            log.error("Unexpected error during validation for {} {}", method, path, e);
            sendGenericErrorResponse(httpResponse, "Validation error: " + e.getMessage());
        }
    }

    /**
     * 요청을 래핑해야 하는지 확인
     * (바디 검증이 필요한 경우에만 래핑)
     */
    private boolean shouldWrapRequest(HttpServletRequest request, ValidationRule rule) {
        // GET, DELETE 등은 바디가 없으므로 래핑 불필요
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) ||
            "DELETE".equalsIgnoreCase(method) ||
            "HEAD".equalsIgnoreCase(method) ||
            "OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        // 바디 검증이 활성화되어 있고, 관련 규칙이 있는 경우에만 래핑
        return rule.isValidateBody() &&
               (rule.needsJsonSchemaValidation() ||
                rule.needsOpenApiValidation() ||
                !rule.getRequiredFields().isEmpty());
    }

    /**
     * 검증 오류 응답 전송
     */
    private void sendValidationErrorResponse(HttpServletResponse response, ValidationResult result) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");

        // 오류 응답 구조:
        // {
        //   "error": "BAD_REQUEST",
        //   "message": "Validation failed",
        //   "errors": ["error1", "error2"],
        //   "fieldErrors": {
        //     "field1": ["error1", "error2"]
        //   }
        // }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"error\":\"BAD_REQUEST\",");
        json.append("\"message\":\"Validation failed\",");

        // errors 배열
        json.append("\"errors\":[");
        boolean first = true;
        for (String error : result.getErrors()) {
            if (!first) json.append(",");
            json.append("\"").append(escapeJson(error)).append("\"");
            first = false;
        }
        json.append("]");

        // fieldErrors 객체
        if (!result.getFieldErrors().isEmpty()) {
            json.append(",\"fieldErrors\":{");
            first = true;
            for (var entry : result.getFieldErrors().entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(escapeJson(entry.getKey())).append("\":[");

                boolean firstError = true;
                for (String error : entry.getValue()) {
                    if (!firstError) json.append(",");
                    json.append("\"").append(escapeJson(error)).append("\"");
                    firstError = false;
                }
                json.append("]");
                first = false;
            }
            json.append("}");
        }

        json.append("}");

        response.getWriter().write(json.toString());
        response.getWriter().flush();
    }

    /**
     * 일반 오류 응답 전송
     */
    private void sendGenericErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");

        String json = String.format(
                "{\"error\":\"BAD_REQUEST\",\"message\":\"%s\"}",
                escapeJson(message)
        );

        response.getWriter().write(json);
        response.getWriter().flush();
    }

    /**
     * JSON 문자열 이스케이프
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
