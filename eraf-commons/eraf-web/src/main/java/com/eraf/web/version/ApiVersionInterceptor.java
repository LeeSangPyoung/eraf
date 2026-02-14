package com.eraf.web.version;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API 버전 Deprecated 경고 인터셉터
 */
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiVersionInterceptor.class);

    private final ApiVersionProperties properties;

    public ApiVersionInterceptor(ApiVersionProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ApiVersion apiVersion = handlerMethod.getMethodAnnotation(ApiVersion.class);

        if (apiVersion == null) {
            apiVersion = handlerMethod.getBeanType().getAnnotation(ApiVersion.class);
        }

        if (apiVersion != null && apiVersion.deprecated()) {
            if (properties.isAddDeprecationWarning()) {
                response.addHeader("X-API-Deprecated", "true");
                response.addHeader("X-API-Deprecated-Version", apiVersion.value());

                if (!apiVersion.deprecatedMessage().isEmpty()) {
                    response.addHeader("X-API-Deprecated-Message", apiVersion.deprecatedMessage());
                }

                if (!apiVersion.replacedBy().isEmpty()) {
                    response.addHeader("X-API-Replaced-By", apiVersion.replacedBy());
                }
            }

            log.warn("Deprecated API called: {} {}, version: {}, message: {}",
                    request.getMethod(), request.getRequestURI(),
                    apiVersion.value(), apiVersion.deprecatedMessage());
        }

        return true;
    }
}
