package com.eraf.notification.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationTemplateService 테스트
 */
class NotificationTemplateServiceTest {

    private NotificationTemplateService service;
    private NotificationTemplateRepository repository;
    private NotificationTemplateEngine engine;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationTemplateRepository.class);
        engine = new NotificationTemplateEngine();
        service = new NotificationTemplateService(repository, engine);
    }

    @Test
    @DisplayName("템플릿 렌더링 - 기본")
    void testRender() {
        // Given
        NotificationTemplate template = createTemplate(
                "ORDER_CONFIRMED",
                "[{{orderNumber}}] 주문 확인",
                "{{userName}}님, 주문번호 {{orderNumber}}이 확인되었습니다."
        );
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue("ORDER_CONFIRMED", "ko"))
                .thenReturn(Optional.of(template));

        // When
        NotificationTemplateService.RenderedTemplate result =
                service.render("ORDER_CONFIRMED", Map.of(
                        "userName", "홍길동",
                        "orderNumber", "ORD-001"
                ));

        // Then
        assertThat(result.getSubject()).isEqualTo("[ORD-001] 주문 확인");
        assertThat(result.getBody()).isEqualTo("홍길동님, 주문번호 ORD-001이 확인되었습니다.");
    }

    @Test
    @DisplayName("템플릿 렌더링 - 로케일 폴백")
    void testRenderWithLocaleFallback() {
        // Given
        NotificationTemplate template = createTemplate(
                "WELCOME",
                "환영합니다",
                "{{userName}}님 환영합니다."
        );
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue("WELCOME", "en"))
                .thenReturn(Optional.empty());
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue("WELCOME", "ko"))
                .thenReturn(Optional.of(template));

        // When
        NotificationTemplateService.RenderedTemplate result =
                service.render("WELCOME", "en", Map.of("userName", "John"));

        // Then
        assertThat(result.getBody()).isEqualTo("John님 환영합니다.");
    }

    @Test
    @DisplayName("템플릿 렌더링 - 존재하지 않는 템플릿")
    void testRenderNotFound() {
        // Given
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findByTemplateCodeAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.render("NONEXISTENT", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    @DisplayName("템플릿 미리보기")
    void testPreview() {
        // When
        NotificationTemplateService.RenderedTemplate result =
                service.preview(
                        "안녕 {{name}}",
                        "{{name}}님 반갑습니다.",
                        Map.of("name", "홍길동")
                );

        // Then
        assertThat(result.getSubject()).isEqualTo("안녕 홍길동");
        assertThat(result.getBody()).isEqualTo("홍길동님 반갑습니다.");
    }

    @Test
    @DisplayName("템플릿 생성")
    void testCreate() {
        // Given
        NotificationTemplate template = createTemplate("NEW_TEMPLATE", "제목", "본문");
        when(repository.existsByTemplateCode("NEW_TEMPLATE")).thenReturn(false);
        when(repository.save(any())).thenReturn(template);

        // When
        NotificationTemplate created = service.create(template);

        // Then
        assertThat(created.getTemplateCode()).isEqualTo("NEW_TEMPLATE");
        verify(repository).save(template);
    }

    @Test
    @DisplayName("템플릿 생성 - 중복 코드")
    void testCreateDuplicate() {
        // Given
        NotificationTemplate template = createTemplate("EXISTING", "제목", "본문");
        when(repository.existsByTemplateCode("EXISTING")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.create(template))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("템플릿 수정")
    void testUpdate() {
        // Given
        NotificationTemplate existing = createTemplate("TEMPLATE", "Old Subject", "Old Body");
        existing.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        NotificationTemplate updated = createTemplate("TEMPLATE", "New Subject", "New Body");
        when(repository.save(any())).thenReturn(existing);

        // When
        NotificationTemplate result = service.update(1L, updated);

        // Then
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("템플릿 삭제")
    void testDelete() {
        // Given
        NotificationTemplate template = createTemplate("DELETE_ME", "제목", "본문");
        template.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(template));

        // When
        service.delete(1L);

        // Then
        verify(repository).delete(template);
    }

    @Test
    @DisplayName("템플릿 활성/비활성 토글")
    void testToggleActive() {
        // Given
        NotificationTemplate template = createTemplate("TOGGLE", "제목", "본문");
        template.setId(1L);
        template.setActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any())).thenReturn(template);

        // When
        NotificationTemplate toggled = service.toggleActive(1L);

        // Then
        assertThat(toggled.isActive()).isFalse();
    }

    @Test
    @DisplayName("유형별 템플릿 조회")
    void testFindByType() {
        // Given
        List<NotificationTemplate> templates = List.of(
                createTemplate("EMAIL1", "제목1", "본문1"),
                createTemplate("EMAIL2", "제목2", "본문2")
        );
        when(repository.findByTemplateTypeAndActiveTrue(NotificationTemplate.TemplateType.EMAIL))
                .thenReturn(templates);

        // When
        List<NotificationTemplate> result = service.findByType(NotificationTemplate.TemplateType.EMAIL);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("캐시 동작 - 같은 템플릿 두 번 조회")
    void testCacheBehavior() {
        // Given
        NotificationTemplate template = createTemplate("CACHED", "제목", "{{name}}님");
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue("CACHED", "ko"))
                .thenReturn(Optional.of(template));

        // When - 두 번 렌더링
        service.render("CACHED", Map.of("name", "A"));
        service.render("CACHED", Map.of("name", "B"));

        // Then - 리포지토리는 한 번만 호출 (캐시)
        verify(repository, times(1)).findByTemplateCodeAndLocaleAndActiveTrue("CACHED", "ko");
    }

    @Test
    @DisplayName("캐시 초기화 후 리포지토리 재조회")
    void testCacheClear() {
        // Given
        NotificationTemplate template = createTemplate("CACHED2", "제목", "{{name}}님");
        when(repository.findByTemplateCodeAndLocaleAndActiveTrue("CACHED2", "ko"))
                .thenReturn(Optional.of(template));

        service.render("CACHED2", Map.of("name", "A"));

        // When - 캐시 초기화 후 다시 조회
        service.clearCache();
        service.render("CACHED2", Map.of("name", "B"));

        // Then - 리포지토리 두 번 호출
        verify(repository, times(2)).findByTemplateCodeAndLocaleAndActiveTrue("CACHED2", "ko");
    }

    private NotificationTemplate createTemplate(String code, String subject, String body) {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateCode(code);
        template.setTemplateType(NotificationTemplate.TemplateType.EMAIL);
        template.setName(code + " Template");
        template.setSubjectTemplate(subject);
        template.setBodyTemplate(body);
        template.setLocale("ko");
        template.setActive(true);
        return template;
    }
}
