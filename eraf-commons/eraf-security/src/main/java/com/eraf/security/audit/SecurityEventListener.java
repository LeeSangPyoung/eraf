package com.eraf.security.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring Security 이벤트 리스너
 */
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);

    private final SecurityAuditLogger auditLogger;

    public SecurityEventListener(SecurityAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            auditLogger.logLoginSuccess(
                    auth.getName(),
                    getClientIp(request),
                    request.getHeader("User-Agent")
            );
        } else {
            auditLogger.logLoginSuccess(auth.getName(), null, null);
        }
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        HttpServletRequest request = getCurrentRequest();

        String reason = event.getException() != null ? event.getException().getMessage() : "Unknown";

        if (request != null) {
            auditLogger.logLoginFailure(
                    auth != null ? auth.getName() : "unknown",
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    reason
            );
        } else {
            auditLogger.logLoginFailure(
                    auth != null ? auth.getName() : "unknown",
                    null,
                    null,
                    reason
            );
        }
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            auditLogger.logLogout(auth.getName(), getClientIp(request));
        } else {
            auditLogger.logLogout(auth.getName(), null);
        }
    }

    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        Authentication auth = event.getAuthentication().get();
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            auditLogger.logAccessDenied(
                    auth != null ? auth.getName() : "anonymous",
                    getClientIp(request),
                    request.getRequestURI(),
                    request.getMethod()
            );
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
