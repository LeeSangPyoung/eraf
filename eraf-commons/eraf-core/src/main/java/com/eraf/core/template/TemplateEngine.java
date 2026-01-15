package com.eraf.core.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 간단한 템플릿 엔진
 * ${변수} 또는 #{변수} 형식의 변수 치환 지원
 */
public final class TemplateEngine {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}|#\\{([^}]+)}");

    private TemplateEngine() {
    }

    /**
     * 템플릿 렌더링
     *
     * @param template  템플릿 문자열
     * @param variables 변수 맵
     * @return 렌더링된 문자열
     */
    public static String render(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Object value = getValue(variables, varName);
            String replacement = value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 템플릿 렌더링 (객체 기반)
     */
    public static String render(String template, Object... keyValues) {
        if (template == null || keyValues == null || keyValues.length == 0) {
            return template;
        }

        Map<String, Object> variables = new java.util.HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            variables.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }

        return render(template, variables);
    }

    /**
     * 중첩 키 지원 (예: user.name)
     */
    @SuppressWarnings("unchecked")
    private static Object getValue(Map<String, Object> variables, String key) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            Object current = variables;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(part);
                } else {
                    return null;
                }
            }
            return current;
        }
        return variables.get(key);
    }

    /**
     * 조건부 렌더링 지원
     * {{#if condition}}내용{{/if}}
     */
    public static String renderWithConditions(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }

        // 먼저 조건부 블록 처리
        Pattern ifPattern = Pattern.compile("\\{\\{#if\\s+([^}]+)}}(.*?)\\{\\{/if}}", Pattern.DOTALL);
        Matcher ifMatcher = ifPattern.matcher(template);
        StringBuilder result = new StringBuilder();

        while (ifMatcher.find()) {
            String condition = ifMatcher.group(1).trim();
            String content = ifMatcher.group(2);

            Object value = getValue(variables, condition);
            boolean isTrue = value != null &&
                    (!(value instanceof Boolean) || (Boolean) value) &&
                    (!(value instanceof String) || !((String) value).isEmpty());

            ifMatcher.appendReplacement(result, isTrue ? Matcher.quoteReplacement(content) : "");
        }
        ifMatcher.appendTail(result);

        // 그 다음 변수 치환
        return render(result.toString(), variables);
    }

    /**
     * 반복 렌더링 지원
     * {{#each items}}내용{{/each}}
     */
    @SuppressWarnings("unchecked")
    public static String renderWithLoop(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }

        Pattern eachPattern = Pattern.compile("\\{\\{#each\\s+([^}]+)}}(.*?)\\{\\{/each}}", Pattern.DOTALL);
        Matcher eachMatcher = eachPattern.matcher(template);
        StringBuilder result = new StringBuilder();

        while (eachMatcher.find()) {
            String listName = eachMatcher.group(1).trim();
            String itemTemplate = eachMatcher.group(2);

            Object listObj = variables.get(listName);
            if (listObj instanceof Iterable) {
                StringBuilder items = new StringBuilder();
                int index = 0;
                for (Object item : (Iterable<?>) listObj) {
                    Map<String, Object> itemVars = new java.util.HashMap<>(variables);
                    itemVars.put("this", item);
                    itemVars.put("index", index++);
                    if (item instanceof Map) {
                        itemVars.putAll((Map<String, Object>) item);
                    }
                    items.append(render(itemTemplate, itemVars));
                }
                eachMatcher.appendReplacement(result, Matcher.quoteReplacement(items.toString()));
            } else {
                eachMatcher.appendReplacement(result, "");
            }
        }
        eachMatcher.appendTail(result);

        return render(result.toString(), variables);
    }
}
