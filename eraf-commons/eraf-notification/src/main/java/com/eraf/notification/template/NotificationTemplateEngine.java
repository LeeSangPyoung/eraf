package com.eraf.notification.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 알림 템플릿 렌더링 엔진
 *
 * <p>변수 치환 형식: {@code {{variableName}}}</p>
 * <p>조건부 블록: {@code {{#if condition}}...{{/if}}}</p>
 * <p>반복 블록: {@code {{#each items}}...{{/each}}}</p>
 *
 * <p>사용법:</p>
 * <pre>
 * NotificationTemplateEngine engine = new NotificationTemplateEngine();
 *
 * Map&lt;String, Object&gt; variables = Map.of(
 *     "userName", "홍길동",
 *     "orderNumber", "ORD-2024-001"
 * );
 *
 * String result = engine.render("안녕하세요 {{userName}}님, 주문번호 {{orderNumber}}", variables);
 * // => "안녕하세요 홍길동님, 주문번호 ORD-2024-001"
 * </pre>
 */
public class NotificationTemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateEngine.class);

    // {{variableName}} 패턴
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+(?:\\.\\w+)*)\\s*}}");

    // {{#if condition}}...{{/if}} 패턴
    private static final Pattern IF_PATTERN = Pattern.compile(
            "\\{\\{#if\\s+(\\w+)\\s*}}(.*?)\\{\\{/if\\s*}}",
            Pattern.DOTALL
    );

    // {{#each items}}...{{/each}} 패턴
    private static final Pattern EACH_PATTERN = Pattern.compile(
            "\\{\\{#each\\s+(\\w+)\\s*}}(.*?)\\{\\{/each\\s*}}",
            Pattern.DOTALL
    );

    // {{defaultValue variableName "기본값"}} 패턴
    private static final Pattern DEFAULT_PATTERN = Pattern.compile(
            "\\{\\{\\s*(\\w+)\\s*\\|\\s*default\\s*:\\s*\"([^\"]*)\"\\s*}}"
    );

    /**
     * 템플릿 렌더링
     *
     * @param template 템플릿 문자열
     * @param variables 변수 맵
     * @return 렌더링된 문자열
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        Map<String, Object> safeVars = variables != null ? variables : Collections.emptyMap();

        String result = template;

        // 1. 조건부 블록 처리
        result = processIfBlocks(result, safeVars);

        // 2. 반복 블록 처리
        result = processEachBlocks(result, safeVars);

        // 3. 기본값 처리
        result = processDefaults(result, safeVars);

        // 4. 변수 치환
        result = processVariables(result, safeVars);

        return result;
    }

    /**
     * 템플릿에서 사용되는 변수 목록 추출
     */
    public Set<String> extractVariables(String template) {
        Set<String> variables = new LinkedHashSet<>();
        if (template == null) return variables;

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    /**
     * 필수 변수 검증
     *
     * @param template 템플릿
     * @param requiredVariables 필수 변수 목록 (쉼표 구분)
     * @param variables 제공된 변수
     * @return 누락된 변수 목록
     */
    public List<String> validateVariables(NotificationTemplate template, Map<String, Object> variables) {
        List<String> missing = new ArrayList<>();

        if (template.getRequiredVariables() == null || template.getRequiredVariables().isEmpty()) {
            return missing;
        }

        String[] required = template.getRequiredVariables().split(",");
        for (String var : required) {
            String trimmed = var.trim();
            if (!trimmed.isEmpty() && !variables.containsKey(trimmed)) {
                missing.add(trimmed);
            }
        }
        return missing;
    }

    /**
     * 조건부 블록 처리
     */
    private String processIfBlocks(String template, Map<String, Object> variables) {
        Matcher matcher = IF_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String condition = matcher.group(1);
            String content = matcher.group(2);

            Object value = variables.get(condition);
            boolean truthy = isTruthy(value);

            matcher.appendReplacement(sb, Matcher.quoteReplacement(truthy ? content : ""));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 반복 블록 처리
     */
    @SuppressWarnings("unchecked")
    private String processEachBlocks(String template, Map<String, Object> variables) {
        Matcher matcher = EACH_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String listName = matcher.group(1);
            String itemTemplate = matcher.group(2);

            Object listObj = variables.get(listName);
            StringBuilder items = new StringBuilder();

            if (listObj instanceof Collection<?> collection) {
                int index = 0;
                for (Object item : collection) {
                    Map<String, Object> itemVars = new HashMap<>(variables);
                    itemVars.put("item", item);
                    itemVars.put("index", index);

                    if (item instanceof Map) {
                        itemVars.putAll((Map<String, Object>) item);
                    }

                    items.append(processVariables(itemTemplate, itemVars));
                    index++;
                }
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(items.toString()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 기본값 처리
     */
    private String processDefaults(String template, Map<String, Object> variables) {
        Matcher matcher = DEFAULT_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultValue = matcher.group(2);

            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : defaultValue;

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 변수 치환
     */
    private String processVariables(String template, Map<String, Object> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = resolveVariable(varName, variables);

            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 중첩 변수 해결 (예: "order.number")
     */
    @SuppressWarnings("unchecked")
    private Object resolveVariable(String varName, Map<String, Object> variables) {
        if (!varName.contains(".")) {
            return variables.get(varName);
        }

        String[] parts = varName.split("\\.");
        Object current = variables.get(parts[0]);

        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(parts[i]);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Truthy 판정
     */
    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return !((String) value).isEmpty();
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof Collection<?>) return !((Collection<?>) value).isEmpty();
        return true;
    }
}
