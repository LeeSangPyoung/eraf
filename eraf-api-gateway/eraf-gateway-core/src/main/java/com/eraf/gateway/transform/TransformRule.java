package com.eraf.gateway.transform;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 요청/응답 변환 규칙
 */
@Getter
@Builder
public class TransformRule {

    private final String id;
    private final String name;
    private final String pathPattern;
    private final TransformType type;
    private final TransformTarget target;
    private final TransformAction action;
    private final String key;
    private final String value;
    private final Map<String, String> mapping;
    private final boolean enabled;
    private final int priority;

    public enum TransformType {
        REQUEST,
        RESPONSE
    }

    public enum TransformTarget {
        HEADER,
        QUERY_PARAM,
        BODY
    }

    public enum TransformAction {
        ADD,
        REMOVE,
        REPLACE,
        RENAME
    }

    public boolean matchesPath(String path) {
        if (pathPattern == null || pathPattern.isEmpty() || pathPattern.equals("/**")) {
            return true;
        }
        if (pathPattern.endsWith("/**")) {
            return path.startsWith(pathPattern.substring(0, pathPattern.length() - 3));
        }
        if (pathPattern.endsWith("/*")) {
            String prefix = pathPattern.substring(0, pathPattern.length() - 2);
            return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
        }
        return path.equals(pathPattern);
    }
}
