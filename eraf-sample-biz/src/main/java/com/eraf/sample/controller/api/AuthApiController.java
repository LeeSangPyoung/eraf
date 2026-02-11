package com.eraf.sample.controller.api;

import com.eraf.core.utils.IpUtils;
import com.eraf.security.audit.SecurityAuditEvent;
import com.eraf.security.audit.SecurityAuditLogger;
import com.eraf.security.bot.BotDetectionResult;
import com.eraf.security.bot.BotDetector;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Auth REST API Controller for frontend
 * Maps /auth/* endpoints as expected by frontend
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecurityAuditLogger securityAuditLogger;
    private final BotDetector botDetector;

    // In-memory stores for demo
    private final Map<String, SessionInfo> sessionStore = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final List<SecurityAuditEvent> auditLogs = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final int MAX_SESSIONS_PER_USER = 2;

    // Test users
    private static final Map<String, UserInfo> USERS = Map.of(
            "admin", new UserInfo("admin", "admin123", "U001", List.of("ROLE_ADMIN", "ROLE_USER")),
            "user", new UserInfo("user", "user123", "U002", List.of("ROLE_USER")),
            "manager", new UserInfo("manager", "manager123", "U003", List.of("ROLE_MANAGER", "ROLE_USER"))
    );

    @PostConstruct
    public void init() {
        // Register audit listener to store events in memory for demo purposes
        securityAuditLogger.addListener(event -> auditLogs.add(event));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String username = request.get("username");
        String password = request.get("password");
        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        UserInfo user = USERS.get(username);

        if (user == null || !user.password().equals(password)) {
            securityAuditLogger.logLoginFailure(username, ipAddress, userAgent, "Invalid credentials");
            result.put("success", false);
            result.put("error", "Invalid username or password");
            return result;
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.username(), user.userId(), user.roles());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.username(), user.userId());

        securityAuditLogger.logLoginSuccess(user.username(), ipAddress, userAgent);

        result.put("success", true);
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("username", user.username());
        result.put("userId", user.userId());
        result.put("roles", user.roles());
        result.put("expiresIn", jwtProperties.getAccessTokenExpiration().toSeconds());

        return result;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String refreshToken = request.get("refreshToken");
        String ipAddress = IpUtils.getClientIp(httpRequest);

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                result.put("success", false);
                result.put("error", "Invalid or expired refresh token");
                return result;
            }

            String username = jwtTokenProvider.getUsername(refreshToken);
            UserInfo user = USERS.get(username);

            if (user == null) {
                result.put("success", false);
                result.put("error", "User not found");
                return result;
            }

            String newAccessToken = jwtTokenProvider.createAccessToken(user.username(), user.userId(), user.roles());
            String newRefreshToken = jwtTokenProvider.createRefreshToken(user.username(), user.userId());

            securityAuditLogger.logTokenCreated(username, ipAddress);

            result.put("success", true);
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            result.put("expiresIn", jwtProperties.getAccessTokenExpiration().toSeconds());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Token refresh failed: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/validate")
    public Map<String, Object> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String token = request.get("token");

        try {
            boolean valid = jwtTokenProvider.validateToken(token);
            result.put("valid", valid);

            if (valid) {
                String username = jwtTokenProvider.getUsername(token);
                UserInfo user = USERS.get(username);

                result.put("username", username);
                result.put("userId", jwtTokenProvider.getUserId(token));
                result.put("roles", user != null ? user.roles() : List.of());
                result.put("expiration", jwtTokenProvider.getExpiration(token));
                result.put("menus", getMenusByRoles(user != null ? user.roles() : List.of()));
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @GetMapping("/menu")
    public Map<String, Object> getRoleMenu(@RequestParam String role) {
        Map<String, Object> result = new HashMap<>();
        List<String> menus = getMenusByRoles(List.of(role));
        result.put("role", role);
        result.put("menus", menus);
        return result;
    }

    @PostMapping("/bot/detect")
    public Map<String, Object> detectBot(@RequestBody(required = false) Map<String, String> request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();

        String userAgent = (request != null && request.get("userAgent") != null)
            ? request.get("userAgent")
            : httpRequest.getHeader("User-Agent");

        BotDetectionResult detection = botDetector.detect(new HttpServletRequestWrapper(httpRequest, userAgent));

        result.put("isBot", detection.isBot());
        result.put("isAllowed", detection.isAllowed());
        result.put("botType", detection.getBotType() != null ? detection.getBotType().name() : null);
        result.put("botName", detection.getBotName());
        result.put("confidence", detection.getConfidence());
        result.put("detectionMethod", detection.getDetectionMethod() != null ? detection.getDetectionMethod().name() : null);
        result.put("userAgent", userAgent);

        return result;
    }

    @PostMapping("/session/create")
    public Map<String, Object> createSession(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String userId = request.get("userId");
        String username = request.getOrDefault("username", userId);
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = IpUtils.getClientIp(httpRequest);

        Set<String> existingSessions = userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());

        if (existingSessions.size() >= MAX_SESSIONS_PER_USER) {
            String oldestSessionId = existingSessions.stream()
                    .min(Comparator.comparing(id -> sessionStore.get(id) != null ? sessionStore.get(id).createdAt() : Instant.MAX))
                    .orElse(null);

            if (oldestSessionId != null) {
                sessionStore.remove(oldestSessionId);
                existingSessions.remove(oldestSessionId);
                result.put("kickedSession", oldestSessionId);
            }
        }

        SessionInfo sessionInfo = new SessionInfo(sessionId, userId, username, ipAddress, Instant.now(), Instant.now());
        sessionStore.put(sessionId, sessionInfo);
        existingSessions.add(sessionId);

        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("activeSessions", existingSessions.size());
        result.put("maxSessions", MAX_SESSIONS_PER_USER);

        return result;
    }

    @GetMapping("/session/list")
    public Map<String, Object> listSessions(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();

        Set<String> sessionIds = userSessions.getOrDefault(userId, Collections.emptySet());
        List<Map<String, Object>> sessions = new ArrayList<>();

        for (String sessionId : sessionIds) {
            SessionInfo info = sessionStore.get(sessionId);
            if (info != null) {
                sessions.add(Map.of(
                        "sessionId", info.sessionId(),
                        "ipAddress", info.ipAddress(),
                        "createdAt", info.createdAt().toString(),
                        "lastAccessedAt", info.lastAccessedAt().toString()
                ));
            }
        }

        result.put("userId", userId);
        result.put("sessions", sessions);
        result.put("count", sessions.size());
        result.put("maxSessions", MAX_SESSIONS_PER_USER);

        return result;
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> invalidateSession(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();

        SessionInfo info = sessionStore.remove(sessionId);
        if (info != null) {
            Set<String> userSessionSet = userSessions.get(info.userId());
            if (userSessionSet != null) {
                userSessionSet.remove(sessionId);
            }
            result.put("success", true);
            result.put("message", "Session invalidated");
        } else {
            result.put("success", false);
            result.put("error", "Session not found");
        }

        return result;
    }

    @GetMapping("/audit/logs")
    public Map<String, Object> getAuditLogs(@RequestParam(required = false) String userId) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> logs = auditLogs.stream()
                .filter(event -> userId == null || userId.equals(event.getPrincipal()))
                .sorted(Comparator.comparing(SecurityAuditEvent::getTimestamp).reversed())
                .limit(50)
                .map(event -> {
                    Map<String, Object> log = new HashMap<>();
                    log.put("timestamp", event.getTimestamp().toString());
                    log.put("eventType", event.getEventType().name());
                    log.put("principal", event.getPrincipal());
                    log.put("ipAddress", event.getIpAddress());
                    log.put("success", event.isSuccess());
                    return log;
                })
                .toList();

        result.put("logs", logs);
        result.put("total", logs.size());

        return result;
    }

    private List<String> getMenusByRoles(List<String> roles) {
        List<String> menus = new ArrayList<>(List.of("Dashboard", "Profile"));
        if (roles.contains("ROLE_USER")) menus.addAll(List.of("My Orders", "Settings"));
        if (roles.contains("ROLE_MANAGER")) menus.addAll(List.of("Team Management", "Reports", "Analytics"));
        if (roles.contains("ROLE_ADMIN")) menus.addAll(List.of("User Management", "System Settings", "Audit Logs"));
        return menus;
    }

    record UserInfo(String username, String password, String userId, List<String> roles) {}
    record SessionInfo(String sessionId, String userId, String username, String ipAddress, Instant createdAt, Instant lastAccessedAt) {}

    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final String customUserAgent;

        public HttpServletRequestWrapper(HttpServletRequest request, String customUserAgent) {
            super(request);
            this.customUserAgent = customUserAgent;
        }

        @Override
        public String getHeader(String name) {
            if ("User-Agent".equalsIgnoreCase(name)) {
                return customUserAgent;
            }
            return super.getHeader(name);
        }
    }
}
