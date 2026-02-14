package com.eraf.mongo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * MongoDB Auditing 자동 설정
 *
 * ErafContext가 classpath에 있으면 현재 사용자 정보를 자동으로 가져옵니다.
 * Spring Security가 있으면 SecurityContextHolder에서 가져옵니다.
 * 둘 다 없으면 "system"을 기본값으로 사용합니다.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorProvider() {
        return new MongoAuditorAware();
    }

    /**
     * 현재 사용자 조회
     * 1순위: ErafContext.getCurrentUserId()
     * 2순위: Spring Security SecurityContextHolder
     * 3순위: "system" (기본값)
     */
    static class MongoAuditorAware implements AuditorAware<String> {

        private static final String ERAF_CONTEXT_CLASS = "com.eraf.core.context.ErafContext";
        private static final String SECURITY_CONTEXT_CLASS = "org.springframework.security.core.context.SecurityContextHolder";

        private final Method erafGetCurrentUserId;
        private final Method securityGetContext;

        MongoAuditorAware() {
            this.erafGetCurrentUserId = resolveErafContext();
            this.securityGetContext = resolveSecurityContext();
        }

        @Override
        public Optional<String> getCurrentAuditor() {
            // 1순위: ErafContext
            if (erafGetCurrentUserId != null) {
                try {
                    String userId = (String) erafGetCurrentUserId.invoke(null);
                    if (userId != null && !userId.isEmpty()) {
                        return Optional.of(userId);
                    }
                } catch (Exception e) {
                    // ErafContext 조회 실패 시 다음 방법 시도
                }
            }

            // 2순위: Spring Security
            if (securityGetContext != null) {
                try {
                    Object securityContext = securityGetContext.invoke(null);
                    Method getAuthentication = securityContext.getClass().getMethod("getAuthentication");
                    Object authentication = getAuthentication.invoke(securityContext);
                    if (authentication != null) {
                        Method isAuthenticated = authentication.getClass().getMethod("isAuthenticated");
                        Method getName = authentication.getClass().getMethod("getName");
                        Boolean authenticated = (Boolean) isAuthenticated.invoke(authentication);
                        if (Boolean.TRUE.equals(authenticated)) {
                            String name = (String) getName.invoke(authentication);
                            if (name != null && !"anonymousUser".equals(name)) {
                                return Optional.of(name);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Security 조회 실패 시 기본값 사용
                }
            }

            // 3순위: 기본값
            return Optional.of("system");
        }

        private static Method resolveErafContext() {
            try {
                Class<?> clazz = Class.forName(ERAF_CONTEXT_CLASS);
                return clazz.getMethod("getCurrentUserId");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                return null;
            }
        }

        private static Method resolveSecurityContext() {
            try {
                Class<?> clazz = Class.forName(SECURITY_CONTEXT_CLASS);
                return clazz.getMethod("getContext");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                return null;
            }
        }
    }
}
