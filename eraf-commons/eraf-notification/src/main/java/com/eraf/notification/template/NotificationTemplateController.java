package com.eraf.notification.template;

import com.eraf.response.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 알림 템플릿 관리 API
 */
@RestController
@RequestMapping("/admin/notification/templates")
@ConditionalOnBean(NotificationTemplateService.class)
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;
    private final NotificationTemplateEngine templateEngine;

    public NotificationTemplateController(
            NotificationTemplateService templateService,
            NotificationTemplateEngine templateEngine) {
        this.templateService = templateService;
        this.templateEngine = templateEngine;
    }

    /**
     * 전체 템플릿 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> list() {
        return ResponseEntity.ok(ApiResponse.success(templateService.findAll()));
    }

    /**
     * 유형별 템플릿 조회
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> listByType(
            @PathVariable NotificationTemplate.TemplateType type) {
        return ResponseEntity.ok(ApiResponse.success(templateService.findByType(type)));
    }

    /**
     * 템플릿 상세 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplate>> getById(@PathVariable Long id) {
        return templateService.findById(id)
                .map(t -> ResponseEntity.ok(ApiResponse.success(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 템플릿 상세 조회 (코드)
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<NotificationTemplate>> getByCode(@PathVariable String code) {
        return templateService.findByCode(code)
                .map(t -> ResponseEntity.ok(ApiResponse.success(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 템플릿 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationTemplate>> create(
            @RequestBody NotificationTemplate template) {
        NotificationTemplate created = templateService.create(template);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    /**
     * 템플릿 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationTemplate>> update(
            @PathVariable Long id,
            @RequestBody NotificationTemplate template) {
        NotificationTemplate updated = templateService.update(id, template);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 템플릿 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted: " + id));
    }

    /**
     * 템플릿 활성/비활성 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<NotificationTemplate>> toggle(@PathVariable Long id) {
        NotificationTemplate toggled = templateService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(toggled));
    }

    /**
     * 템플릿 렌더링 (미리보기)
     */
    @PostMapping("/{code}/render")
    public ResponseEntity<ApiResponse<NotificationTemplateService.RenderedTemplate>> render(
            @PathVariable String code,
            @RequestBody Map<String, Object> variables) {
        NotificationTemplateService.RenderedTemplate rendered = templateService.render(code, variables);
        return ResponseEntity.ok(ApiResponse.success(rendered));
    }

    /**
     * 로케일 지정 렌더링
     */
    @PostMapping("/{code}/render/{locale}")
    public ResponseEntity<ApiResponse<NotificationTemplateService.RenderedTemplate>> renderWithLocale(
            @PathVariable String code,
            @PathVariable String locale,
            @RequestBody Map<String, Object> variables) {
        NotificationTemplateService.RenderedTemplate rendered =
                templateService.render(code, locale, variables);
        return ResponseEntity.ok(ApiResponse.success(rendered));
    }

    /**
     * 인라인 미리보기 (저장 없이 렌더링)
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<NotificationTemplateService.RenderedTemplate>> preview(
            @RequestBody PreviewRequest request) {
        NotificationTemplateService.RenderedTemplate rendered =
                templateService.preview(request.subjectTemplate, request.bodyTemplate, request.variables);
        return ResponseEntity.ok(ApiResponse.success(rendered));
    }

    /**
     * 템플릿 변수 추출
     */
    @PostMapping("/extract-variables")
    public ResponseEntity<ApiResponse<Set<String>>> extractVariables(
            @RequestBody Map<String, String> request) {
        String template = request.getOrDefault("template", "");
        Set<String> variables = templateEngine.extractVariables(template);
        return ResponseEntity.ok(ApiResponse.success(variables));
    }

    /**
     * 캐시 초기화
     */
    @DeleteMapping("/cache")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        templateService.clearCache();
        return ResponseEntity.ok(ApiResponse.success("Template cache cleared"));
    }

    /**
     * 미리보기 요청 DTO
     */
    public static class PreviewRequest {
        public String subjectTemplate;
        public String bodyTemplate;
        public Map<String, Object> variables;
    }
}
