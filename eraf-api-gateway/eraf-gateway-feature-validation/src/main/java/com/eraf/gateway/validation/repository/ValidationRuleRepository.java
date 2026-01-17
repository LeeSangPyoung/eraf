package com.eraf.gateway.validation.repository;

import org.springframework.util.AntPathMatcher;
import com.eraf.gateway.common.repository.GatewayRepository;
import com.eraf.gateway.validation.domain.ValidationRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validation Rule Repository
 * 검증 규칙 저장소 (In-Memory)
 */
@Slf4j
@Repository
public class ValidationRuleRepository implements GatewayRepository<ValidationRule> {

    private final ConcurrentHashMap<String, ValidationRule> rules = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 경로와 메서드에 매칭되는 검증 규칙 조회
     *
     * @param path   요청 경로
     * @param method HTTP 메서드
     * @return 매칭되는 검증 규칙 (없으면 빈 Optional)
     */
    public Optional<ValidationRule> findByPathAndMethod(String path, String method) {
        for (ValidationRule rule : rules.values()) {
            if (pathMatcher.match(rule.getPathPattern(), path) && rule.matchesMethod(method)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    /**
     * 경로 패턴으로 규칙 조회
     */
    public Optional<ValidationRule> findByPathPattern(String pathPattern) {
        return Optional.ofNullable(rules.get(pathPattern));
    }

    /**
     * 모든 규칙 조회
     */
    public List<ValidationRule> findAll() {
        return new ArrayList<>(rules.values());
    }

    /**
     * 활성화된 규칙만 조회
     */
    public List<ValidationRule> findAllEnabled() {
        return rules.values().stream()
                .filter(ValidationRule::isEnabled)
                .toList();
    }

    /**
     * 규칙 저장
     */
    @Override
    public ValidationRule save(ValidationRule rule) {
        if (rule.getPathPattern() == null) {
            throw new IllegalArgumentException("Path pattern cannot be null");
        }

        long now = System.currentTimeMillis();
        if (rule.getCreatedAt() == null) {
            rule = ValidationRule.builder()
                    .pathPattern(rule.getPathPattern())
                    .method(rule.getMethod())
                    .jsonSchema(rule.getJsonSchema())
                    .openApiOperationId(rule.getOpenApiOperationId())
                    .maxBodySize(rule.getMaxBodySize())
                    .allowedContentTypes(rule.getAllowedContentTypes())
                    .requiredHeaders(rule.getRequiredHeaders())
                    .requiredQueryParams(rule.getRequiredQueryParams())
                    .requiredFields(rule.getRequiredFields())
                    .enabled(rule.isEnabled())
                    .strictMode(rule.isStrictMode())
                    .validateHeaders(rule.isValidateHeaders())
                    .validateQueryParams(rule.isValidateQueryParams())
                    .validateBody(rule.isValidateBody())
                    .metadata(rule.getMetadata())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        } else {
            rule = ValidationRule.builder()
                    .pathPattern(rule.getPathPattern())
                    .method(rule.getMethod())
                    .jsonSchema(rule.getJsonSchema())
                    .openApiOperationId(rule.getOpenApiOperationId())
                    .maxBodySize(rule.getMaxBodySize())
                    .allowedContentTypes(rule.getAllowedContentTypes())
                    .requiredHeaders(rule.getRequiredHeaders())
                    .requiredQueryParams(rule.getRequiredQueryParams())
                    .requiredFields(rule.getRequiredFields())
                    .enabled(rule.isEnabled())
                    .strictMode(rule.isStrictMode())
                    .validateHeaders(rule.isValidateHeaders())
                    .validateQueryParams(rule.isValidateQueryParams())
                    .validateBody(rule.isValidateBody())
                    .metadata(rule.getMetadata())
                    .createdAt(rule.getCreatedAt())
                    .updatedAt(now)
                    .build();
        }

        rules.put(rule.getPathPattern(), rule);
        log.debug("Saved validation rule for path: {}", rule.getPathPattern());
        return rule;
    }

    /**
     * 규칙 삭제
     */
    @Override
    public void delete(String pathPattern) {
        rules.remove(pathPattern);
        log.debug("Deleted validation rule for path: {}", pathPattern);
    }

    /**
     * 모든 규칙 삭제
     */
    @Override
    public void deleteAll() {
        rules.clear();
        log.debug("Deleted all validation rules");
    }

    /**
     * 규칙 존재 여부 확인
     */
    @Override
    public boolean exists(String pathPattern) {
        return rules.containsKey(pathPattern);
    }

    /**
     * 규칙 개수 조회
     */
    @Override
    public long count() {
        return rules.size();
    }
}
