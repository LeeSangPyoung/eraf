package com.eraf.notification.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 알림 템플릿 서비스
 *
 * <p>템플릿 CRUD, 렌더링, 캐싱을 제공합니다.</p>
 *
 * <p>사용법:</p>
 * <pre>
 * // 템플릿 렌더링
 * RenderedTemplate result = templateService.render("ORDER_CONFIRMED", Map.of(
 *     "userName", "홍길동",
 *     "orderNumber", "ORD-2024-001"
 * ));
 *
 * // 이메일 발송에 활용
 * emailSender.send(result.getSubject(), result.getBody());
 * </pre>
 */
public class NotificationTemplateService {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateEngine templateEngine;

    // 인메모리 캐시 (templateCode:locale → template)
    private final Map<String, NotificationTemplate> cache = new ConcurrentHashMap<>();

    public NotificationTemplateService(
            NotificationTemplateRepository templateRepository,
            NotificationTemplateEngine templateEngine) {
        this.templateRepository = templateRepository;
        this.templateEngine = templateEngine;
    }

    /**
     * 템플릿 렌더링
     *
     * @param templateCode 템플릿 코드
     * @param variables 변수 맵
     * @return 렌더링 결과
     */
    public RenderedTemplate render(String templateCode, Map<String, Object> variables) {
        return render(templateCode, "ko", variables);
    }

    /**
     * 템플릿 렌더링 (로케일 지정)
     */
    public RenderedTemplate render(String templateCode, String locale, Map<String, Object> variables) {
        NotificationTemplate template = findTemplate(templateCode, locale);

        // 필수 변수 검증
        List<String> missing = templateEngine.validateVariables(template, variables);
        if (!missing.isEmpty()) {
            log.warn("[TemplateService] Missing variables for template {}: {}", templateCode, missing);
        }

        // 제목 렌더링
        String subject = null;
        if (template.getSubjectTemplate() != null) {
            subject = templateEngine.render(template.getSubjectTemplate(), variables);
        }

        // 본문 렌더링
        String body = templateEngine.render(template.getBodyTemplate(), variables);

        return new RenderedTemplate(subject, body, template.isHtml(), template.getTemplateType());
    }

    /**
     * 템플릿 미리보기 (저장 없이 렌더링)
     */
    public RenderedTemplate preview(String subjectTemplate, String bodyTemplate, Map<String, Object> variables) {
        String subject = subjectTemplate != null
                ? templateEngine.render(subjectTemplate, variables)
                : null;
        String body = templateEngine.render(bodyTemplate, variables);
        return new RenderedTemplate(subject, body, false, null);
    }

    // ===== CRUD =====

    /**
     * 템플릿 생성
     */
    public NotificationTemplate create(NotificationTemplate template) {
        if (templateRepository.existsByTemplateCode(template.getTemplateCode())) {
            throw new IllegalArgumentException("Template code already exists: " + template.getTemplateCode());
        }

        NotificationTemplate saved = templateRepository.save(template);
        cache.remove(cacheKey(saved.getTemplateCode(), saved.getLocale()));
        log.info("[TemplateService] Created template: {} ({})", saved.getTemplateCode(), saved.getTemplateType());
        return saved;
    }

    /**
     * 템플릿 수정
     */
    public NotificationTemplate update(Long id, NotificationTemplate updated) {
        NotificationTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

        existing.setName(updated.getName());
        existing.setSubjectTemplate(updated.getSubjectTemplate());
        existing.setBodyTemplate(updated.getBodyTemplate());
        existing.setHtml(updated.isHtml());
        existing.setDescription(updated.getDescription());
        existing.setRequiredVariables(updated.getRequiredVariables());

        NotificationTemplate saved = templateRepository.save(existing);
        cache.remove(cacheKey(saved.getTemplateCode(), saved.getLocale()));
        log.info("[TemplateService] Updated template: {}", saved.getTemplateCode());
        return saved;
    }

    /**
     * 템플릿 삭제
     */
    public void delete(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

        templateRepository.delete(template);
        cache.remove(cacheKey(template.getTemplateCode(), template.getLocale()));
        log.info("[TemplateService] Deleted template: {}", template.getTemplateCode());
    }

    /**
     * 템플릿 활성/비활성 토글
     */
    public NotificationTemplate toggleActive(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id));

        template.setActive(!template.isActive());
        NotificationTemplate saved = templateRepository.save(template);
        cache.remove(cacheKey(saved.getTemplateCode(), saved.getLocale()));
        return saved;
    }

    /**
     * 모든 템플릿 조회
     */
    public List<NotificationTemplate> findAll() {
        return templateRepository.findAll();
    }

    /**
     * 유형별 템플릿 조회
     */
    public List<NotificationTemplate> findByType(NotificationTemplate.TemplateType type) {
        return templateRepository.findByTemplateTypeAndActiveTrue(type);
    }

    /**
     * 코드로 템플릿 조회
     */
    public Optional<NotificationTemplate> findByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode);
    }

    /**
     * ID로 템플릿 조회
     */
    public Optional<NotificationTemplate> findById(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * 캐시 초기화
     */
    public void clearCache() {
        cache.clear();
        log.info("[TemplateService] Cache cleared");
    }

    // ===== Private =====

    private NotificationTemplate findTemplate(String templateCode, String locale) {
        String key = cacheKey(templateCode, locale);

        return cache.computeIfAbsent(key, k -> {
            // 1. 정확한 로케일 매칭
            Optional<NotificationTemplate> template =
                    templateRepository.findByTemplateCodeAndLocaleAndActiveTrue(templateCode, locale);

            if (template.isPresent()) return template.get();

            // 2. 기본 로케일 (ko) 폴백
            if (!"ko".equals(locale)) {
                template = templateRepository.findByTemplateCodeAndLocaleAndActiveTrue(templateCode, "ko");
                if (template.isPresent()) return template.get();
            }

            // 3. 로케일 무시
            template = templateRepository.findByTemplateCodeAndActiveTrue(templateCode);
            if (template.isPresent()) return template.get();

            throw new IllegalArgumentException("Template not found: " + templateCode + " (locale: " + locale + ")");
        });
    }

    private String cacheKey(String templateCode, String locale) {
        return templateCode + ":" + (locale != null ? locale : "ko");
    }

    /**
     * 렌더링 결과
     */
    public static class RenderedTemplate {
        private final String subject;
        private final String body;
        private final boolean html;
        private final NotificationTemplate.TemplateType templateType;

        public RenderedTemplate(String subject, String body, boolean html,
                                NotificationTemplate.TemplateType templateType) {
            this.subject = subject;
            this.body = body;
            this.html = html;
            this.templateType = templateType;
        }

        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public boolean isHtml() { return html; }
        public NotificationTemplate.TemplateType getTemplateType() { return templateType; }
    }
}
