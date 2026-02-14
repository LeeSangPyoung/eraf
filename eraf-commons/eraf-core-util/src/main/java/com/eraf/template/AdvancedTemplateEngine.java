package com.eraf.util.template;

import java.util.Map;

/**
 * 고급 템플릿 엔진 인터페이스
 * Thymeleaf, Freemarker 등 구현체를 위한 추상화
 */
public interface AdvancedTemplateEngine {

    /**
     * 템플릿 이름으로 렌더링
     *
     * @param templateName 템플릿 이름 (파일명 또는 경로)
     * @param variables    변수 맵
     * @return 렌더링된 문자열
     */
    String render(String templateName, Map<String, Object> variables);

    /**
     * 템플릿 문자열 직접 렌더링
     *
     * @param templateContent 템플릿 내용
     * @param variables       변수 맵
     * @return 렌더링된 문자열
     */
    String renderInline(String templateContent, Map<String, Object> variables);

    /**
     * 지원하는 템플릿 엔진 이름
     */
    String getEngineName();
}
