package com.eraf.notification.template;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * 알림 템플릿 엔티티
 *
 * <p>이메일, SMS 등의 알림에 사용되는 템플릿을 관리합니다.</p>
 * <p>변수 치환 형식: {@code {{variableName}}}</p>
 *
 * <p>예시:</p>
 * <pre>
 * 안녕하세요 {{userName}}님,
 * {{orderNumber}} 주문이 {{status}}되었습니다.
 * </pre>
 */
@Entity
@Table(name = "notification_templates", indexes = {
        @Index(name = "idx_template_code", columnList = "template_code", unique = true),
        @Index(name = "idx_template_type", columnList = "template_type"),
        @Index(name = "idx_template_active", columnList = "active")
})
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 템플릿 고유 코드 (예: "ORDER_CONFIRMED", "PASSWORD_RESET")
     */
    @Column(name = "template_code", nullable = false, unique = true, length = 100)
    private String templateCode;

    /**
     * 템플릿 유형 (EMAIL, SMS, PUSH, SLACK, TEAMS)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 20)
    private TemplateType templateType;

    /**
     * 템플릿 이름 (관리용)
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 제목 템플릿 (이메일/푸시용)
     */
    @Column(name = "subject_template", length = 500)
    private String subjectTemplate;

    /**
     * 본문 템플릿
     */
    @Column(name = "body_template", columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    /**
     * HTML 여부 (이메일용)
     */
    @Column(name = "is_html")
    private boolean html = false;

    /**
     * 언어 코드 (i18n, 예: "ko", "en")
     */
    @Column(name = "locale", length = 10)
    private String locale = "ko";

    /**
     * 활성 여부
     */
    @Column(name = "active")
    private boolean active = true;

    /**
     * 설명
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 필수 변수 목록 (쉼표 구분, 예: "userName,orderNumber,status")
     */
    @Column(name = "required_variables", length = 1000)
    private String requiredVariables;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public TemplateType getTemplateType() { return templateType; }
    public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubjectTemplate() { return subjectTemplate; }
    public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }

    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }

    public boolean isHtml() { return html; }
    public void setHtml(boolean html) { this.html = html; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredVariables() { return requiredVariables; }
    public void setRequiredVariables(String requiredVariables) { this.requiredVariables = requiredVariables; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    /**
     * 템플릿 유형
     */
    public enum TemplateType {
        EMAIL, SMS, PUSH, SLACK, TEAMS
    }
}
