package com.eraf.gateway.transform;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * 응답 변환 필터
 * 응답 헤더 추가/제거/변경
 */
@Slf4j
@RequiredArgsConstructor
public class ResponseTransformFilter extends OncePerRequestFilter {

    private final List<TransformRule> rules;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        List<TransformRule> applicableRules = rules.stream()
                .filter(r -> r.isEnabled() && r.getType() == TransformRule.TransformType.RESPONSE)
                .filter(r -> r.matchesPath(path))
                .sorted(Comparator.comparingInt(TransformRule::getPriority))
                .toList();

        if (applicableRules.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        TransformResponseWrapper wrappedResponse = new TransformResponseWrapper(response, applicableRules);
        filterChain.doFilter(request, wrappedResponse);
        wrappedResponse.applyTransformations();
    }

    private static class TransformResponseWrapper extends HttpServletResponseWrapper {

        private final List<TransformRule> rules;
        private final Map<String, String> headersToAdd = new HashMap<>();
        private final Set<String> headersToRemove = new HashSet<>();
        private final Map<String, String> headersToRename = new HashMap<>();

        public TransformResponseWrapper(HttpServletResponse response, List<TransformRule> rules) {
            super(response);
            this.rules = rules;
        }

        public void applyTransformations() {
            for (TransformRule rule : rules) {
                if (rule.getTarget() == TransformRule.TransformTarget.HEADER) {
                    switch (rule.getAction()) {
                        case ADD:
                            addHeader(rule.getKey(), rule.getValue());
                            break;
                        case REMOVE:
                            // 헤더 제거는 이미 설정된 후에는 어려움
                            // 대신 빈 값으로 덮어쓰기
                            setHeader(rule.getKey(), "");
                            break;
                        case REPLACE:
                            setHeader(rule.getKey(), rule.getValue());
                            break;
                    }
                }
            }
        }
    }
}
