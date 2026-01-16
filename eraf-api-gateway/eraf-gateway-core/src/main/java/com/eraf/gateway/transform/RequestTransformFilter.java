package com.eraf.gateway.transform;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * 요청 변환 필터
 * 헤더 추가/제거/변경, 쿼리 파라미터 변환 등
 */
@Slf4j
@RequiredArgsConstructor
public class RequestTransformFilter extends OncePerRequestFilter {

    private final List<TransformRule> rules;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        List<TransformRule> applicableRules = rules.stream()
                .filter(r -> r.isEnabled() && r.getType() == TransformRule.TransformType.REQUEST)
                .filter(r -> r.matchesPath(path))
                .sorted(Comparator.comparingInt(TransformRule::getPriority))
                .toList();

        if (applicableRules.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest wrappedRequest = request;

        for (TransformRule rule : applicableRules) {
            wrappedRequest = applyRule(wrappedRequest, rule);
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private HttpServletRequest applyRule(HttpServletRequest request, TransformRule rule) {
        switch (rule.getTarget()) {
            case HEADER:
                return applyHeaderTransform(request, rule);
            case QUERY_PARAM:
                return applyQueryParamTransform(request, rule);
            default:
                return request;
        }
    }

    private HttpServletRequest applyHeaderTransform(HttpServletRequest request, TransformRule rule) {
        return new HttpServletRequestWrapper(request) {
            private final Map<String, String> customHeaders = new HashMap<>();
            private final Set<String> removedHeaders = new HashSet<>();

            {
                switch (rule.getAction()) {
                    case ADD:
                    case REPLACE:
                        customHeaders.put(rule.getKey().toLowerCase(), rule.getValue());
                        break;
                    case REMOVE:
                        removedHeaders.add(rule.getKey().toLowerCase());
                        break;
                    case RENAME:
                        String oldValue = request.getHeader(rule.getKey());
                        if (oldValue != null) {
                            removedHeaders.add(rule.getKey().toLowerCase());
                            customHeaders.put(rule.getValue().toLowerCase(), oldValue);
                        }
                        break;
                }
            }

            @Override
            public String getHeader(String name) {
                if (removedHeaders.contains(name.toLowerCase())) {
                    return null;
                }
                String customValue = customHeaders.get(name.toLowerCase());
                return customValue != null ? customValue : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (removedHeaders.contains(name.toLowerCase())) {
                    return Collections.emptyEnumeration();
                }
                String customValue = customHeaders.get(name.toLowerCase());
                if (customValue != null) {
                    return Collections.enumeration(Collections.singletonList(customValue));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new HashSet<>();
                Enumeration<String> original = super.getHeaderNames();
                while (original.hasMoreElements()) {
                    String name = original.nextElement();
                    if (!removedHeaders.contains(name.toLowerCase())) {
                        names.add(name);
                    }
                }
                names.addAll(customHeaders.keySet());
                return Collections.enumeration(names);
            }
        };
    }

    private HttpServletRequest applyQueryParamTransform(HttpServletRequest request, TransformRule rule) {
        return new HttpServletRequestWrapper(request) {
            private Map<String, String[]> modifiedParams;

            {
                modifiedParams = new HashMap<>(request.getParameterMap());
                switch (rule.getAction()) {
                    case ADD:
                        modifiedParams.put(rule.getKey(), new String[]{rule.getValue()});
                        break;
                    case REMOVE:
                        modifiedParams.remove(rule.getKey());
                        break;
                    case REPLACE:
                        if (modifiedParams.containsKey(rule.getKey())) {
                            modifiedParams.put(rule.getKey(), new String[]{rule.getValue()});
                        }
                        break;
                    case RENAME:
                        String[] oldValues = modifiedParams.remove(rule.getKey());
                        if (oldValues != null) {
                            modifiedParams.put(rule.getValue(), oldValues);
                        }
                        break;
                }
            }

            @Override
            public String getParameter(String name) {
                String[] values = modifiedParams.get(name);
                return values != null && values.length > 0 ? values[0] : null;
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return Collections.unmodifiableMap(modifiedParams);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(modifiedParams.keySet());
            }

            @Override
            public String[] getParameterValues(String name) {
                return modifiedParams.get(name);
            }
        };
    }
}
