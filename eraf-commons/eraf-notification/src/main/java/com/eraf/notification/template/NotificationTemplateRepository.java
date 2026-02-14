package com.eraf.notification.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 템플릿 JPA 리포지토리
 */
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    Optional<NotificationTemplate> findByTemplateCodeAndLocale(String templateCode, String locale);

    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(String templateCode);

    Optional<NotificationTemplate> findByTemplateCodeAndLocaleAndActiveTrue(String templateCode, String locale);

    List<NotificationTemplate> findByTemplateType(NotificationTemplate.TemplateType templateType);

    List<NotificationTemplate> findByTemplateTypeAndActiveTrue(NotificationTemplate.TemplateType templateType);

    List<NotificationTemplate> findByActiveTrue();

    boolean existsByTemplateCode(String templateCode);
}
