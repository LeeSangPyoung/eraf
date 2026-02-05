package com.eraf.sample.controller.security;

import com.eraf.core.utils.IpUtils;
import com.eraf.security.audit.SecurityAuditEvent;
import com.eraf.security.audit.SecurityAuditLogger;
import com.eraf.security.bot.BotDetectionResult;
import com.eraf.security.bot.BotDetector;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 인증/보안 기능 컨트롤러 (#13~#20)
 *
 * #13 JWT 로그인 + Access Token
 * #14 Refresh Token 발급/갱신
 * #15 역할 기반 메뉴 표시
 * #16 봇 탐지 API
 * #17 세션 생성/관리
 * #18 동시 세션 제한
 * #19 세션 클러스터링 (Redis)
 * #20 보안 감사 로그
 */
@Slf4j
@Controller
@RequestMapping("/security/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecurityAuditLogger securityAuditLogger;
    private final BotDetector botDetector;

    // 메모리 기반 감사 로그 저장소 (데모용)
    private final List<SecurityAuditEvent> auditLogs = new CopyOnWriteArrayList<>();

    // 메모리 기반 세션 저장소 (데모용 - 실제로는 Redis 사용)
    private final Map<String, SessionInfo> sessionStore = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    // 동시 세션 제한
    private static final int MAX_SESSIONS_PER_USER = 2;

    // 테스트용 사용자 정보
    private static final Map<String, UserInfo> USERS = Map.of(
            "admin", new UserInfo("admin", "admin123", "U001", List.of("ROLE_ADMIN", "ROLE_USER")),
            "user", new UserInfo("user", "user123", "U002", List.of("ROLE_USER")),
            "manager", new UserInfo("manager", "manager123", "U003", List.of("ROLE_MANAGER", "ROLE_USER"))
    );

    @GetMapping
    public String authPage(Model model, HttpServletRequest request) {
        // 초기 데이터 설정
        model.addAttribute("users", USERS.keySet());

        // 감사 로그 리스너 등록 (한 번만)
        if (auditLogs.isEmpty()) {
            securityAuditLogger.addListener(auditLogs::add);
        }

        return "security/auth";
    }

    // ===== #13 JWT 로그인 + Access Token =====
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        UserInfo user = USERS.get(request.username());

        if (user == null || !user.password().equals(request.password())) {
            securityAuditLogger.logLoginFailure(request.username(), ipAddress, userAgent, "Invalid credentials");
            result.put("success", false);
            result.put("error", "Invalid username or password");
            return result;
        }

        // Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.username(), user.userId(), user.roles());

        // Refresh Token 생성
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

    // ===== #14 Refresh Token 갱신 =====
    @PostMapping("/refresh")
    @ResponseBody
    public Map<String, Object> refreshToken(@RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String ipAddress = IpUtils.getClientIp(httpRequest);

        try {
            if (!jwtTokenProvider.validateToken(request.refreshToken())) {
                result.put("success", false);
                result.put("error", "Invalid or expired refresh token");
                return result;
            }

            String username = jwtTokenProvider.getUsername(request.refreshToken());
            String userId = jwtTokenProvider.getUserId(request.refreshToken());
            UserInfo user = USERS.get(username);

            if (user == null) {
                result.put("success", false);
                result.put("error", "User not found");
                return result;
            }

            // 새로운 Access Token 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(user.username(), user.userId(), user.roles());

            // 새로운 Refresh Token 발급
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

    // ===== #15 역할 기반 메뉴 표시 =====
    @PostMapping("/validate-token")
    @ResponseBody
    public Map<String, Object> validateToken(@RequestBody TokenRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean valid = jwtTokenProvider.validateToken(request.token());
            result.put("valid", valid);

            if (valid) {
                String username = jwtTokenProvider.getUsername(request.token());
                String userId = jwtTokenProvider.getUserId(request.token());
                UserInfo user = USERS.get(username);

                result.put("username", username);
                result.put("userId", userId);
                result.put("roles", user != null ? user.roles() : List.of());
                result.put("expiration", jwtTokenProvider.getExpiration(request.token()));

                // 역할별 메뉴 목록
                List<String> menus = getMenusByRoles(user != null ? user.roles() : List.of());
                result.put("menus", menus);
            }

        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #16 봇 탐지 API =====
    @GetMapping("/bot-detect")
    @ResponseBody
    public Map<String, Object> detectBot(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        BotDetectionResult detection = botDetector.detect(request);

        result.put("isBot", detection.isBot());
        result.put("isAllowed", detection.isAllowed());
        result.put("botType", detection.getBotType() != null ? detection.getBotType().name() : null);
        result.put("botName", detection.getBotName());
        result.put("confidence", detection.getConfidence());
        result.put("detectionMethod", detection.getDetectionMethod() != null ? detection.getDetectionMethod().name() : null);
        result.put("userAgent", request.getHeader("User-Agent"));

        return result;
    }

    @PostMapping("/bot-detect")
    @ResponseBody
    public Map<String, Object> detectBotWithCustomUserAgent(@RequestBody BotDetectRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();

        // Custom User-Agent를 사용한 테스트용 요청 래퍼
        BotDetectionResult detection = botDetector.detect(new HttpServletRequestWrapper(httpRequest, request.userAgent()));

        result.put("isBot", detection.isBot());
        result.put("isAllowed", detection.isAllowed());
        result.put("botType", detection.getBotType() != null ? detection.getBotType().name() : null);
        result.put("botName", detection.getBotName());
        result.put("confidence", detection.getConfidence());
        result.put("detectionMethod", detection.getDetectionMethod() != null ? detection.getDetectionMethod().name() : null);
        result.put("userAgent", request.userAgent());

        return result;
    }

    // ===== #17 세션 생성/관리 =====
    @PostMapping("/session/create")
    @ResponseBody
    public Map<String, Object> createSession(@RequestBody SessionRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String sessionId = UUID.randomUUID().toString();
        String ipAddress = IpUtils.getClientIp(httpRequest);

        // 동시 세션 제한 확인
        Set<String> existingSessions = userSessions.computeIfAbsent(request.userId(), k -> ConcurrentHashMap.newKeySet());

        // #18 동시 세션 제한 - 최대 세션 수 초과 시 가장 오래된 세션 제거
        if (existingSessions.size() >= MAX_SESSIONS_PER_USER) {
            String oldestSessionId = existingSessions.stream()
                    .min(Comparator.comparing(id -> sessionStore.get(id).createdAt()))
                    .orElse(null);

            if (oldestSessionId != null) {
                sessionStore.remove(oldestSessionId);
                existingSessions.remove(oldestSessionId);
                result.put("kickedSession", oldestSessionId);
            }
        }

        // 새 세션 생성
        SessionInfo sessionInfo = new SessionInfo(
                sessionId,
                request.userId(),
                request.username(),
                ipAddress,
                Instant.now(),
                Instant.now()
        );
        sessionStore.put(sessionId, sessionInfo);
        existingSessions.add(sessionId);

        securityAuditLogger.log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.SESSION_CREATED)
                .principal(request.username())
                .ipAddress(ipAddress)
                .details(Map.of("sessionId", sessionId))
                .success(true)
                .build());

        result.put("success", true);
        result.put("sessionId", sessionId);
        result.put("activeSessions", existingSessions.size());
        result.put("maxSessions", MAX_SESSIONS_PER_USER);

        return result;
    }

    @GetMapping("/session/list/{userId}")
    @ResponseBody
    public Map<String, Object> listSessions(@PathVariable String userId) {
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
    @ResponseBody
    public Map<String, Object> invalidateSession(@PathVariable String sessionId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String ipAddress = IpUtils.getClientIp(request);

        SessionInfo info = sessionStore.remove(sessionId);
        if (info != null) {
            Set<String> userSessionSet = userSessions.get(info.userId());
            if (userSessionSet != null) {
                userSessionSet.remove(sessionId);
            }

            securityAuditLogger.log(SecurityAuditEvent.builder(SecurityAuditEvent.EventType.LOGOUT)
                    .principal(info.username())
                    .ipAddress(ipAddress)
                    .details(Map.of("sessionId", sessionId))
                    .success(true)
                    .build());

            result.put("success", true);
            result.put("message", "Session invalidated");
        } else {
            result.put("success", false);
            result.put("error", "Session not found");
        }

        return result;
    }

    // ===== #19 세션 클러스터링 (Redis) - 데모에서는 메모리 기반으로 시뮬레이션 =====
    @GetMapping("/session/cluster-info")
    @ResponseBody
    public Map<String, Object> getClusterInfo() {
        Map<String, Object> result = new HashMap<>();

        // 클러스터 정보 시뮬레이션
        result.put("clusterEnabled", true);
        result.put("storageType", "Redis (Simulated in Memory for Demo)");
        result.put("totalSessions", sessionStore.size());
        result.put("totalUsers", userSessions.size());

        // 노드 정보 시뮬레이션
        result.put("nodes", List.of(
                Map.of("nodeId", "node-1", "host", "localhost:6379", "role", "master", "sessions", sessionStore.size()),
                Map.of("nodeId", "node-2", "host", "localhost:6380", "role", "replica", "sessions", 0)
        ));

        return result;
    }

    // ===== #20 보안 감사 로그 =====
    @GetMapping("/audit-logs")
    @ResponseBody
    public Map<String, Object> getAuditLogs(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> logs = auditLogs.stream()
                .sorted(Comparator.comparing(SecurityAuditEvent::getTimestamp).reversed())
                .limit(limit)
                .map(event -> {
                    Map<String, Object> log = new HashMap<>();
                    log.put("timestamp", event.getTimestamp().toString());
                    log.put("eventType", event.getEventType().name());
                    log.put("principal", event.getPrincipal());
                    log.put("ipAddress", event.getIpAddress());
                    log.put("userAgent", event.getUserAgent());
                    log.put("requestUri", event.getRequestUri());
                    log.put("method", event.getMethod());
                    log.put("success", event.isSuccess());
                    log.put("details", event.getDetails());
                    return log;
                })
                .toList();

        result.put("logs", logs);
        result.put("total", auditLogs.size());

        return result;
    }

    @PostMapping("/audit-log/test")
    @ResponseBody
    public Map<String, Object> createTestAuditLog(@RequestBody AuditLogRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> result = new HashMap<>();
        String ipAddress = IpUtils.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            SecurityAuditEvent.EventType eventType = SecurityAuditEvent.EventType.valueOf(request.eventType());

            securityAuditLogger.log(SecurityAuditEvent.builder(eventType)
                    .principal(request.principal())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestUri(request.requestUri())
                    .method(request.method())
                    .success(request.success())
                    .details(request.details())
                    .build());

            result.put("success", true);
            result.put("message", "Audit log created");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== 헬퍼 메서드 =====
    private List<String> getMenusByRoles(List<String> roles) {
        List<String> menus = new ArrayList<>();

        // 기본 메뉴 (모든 사용자)
        menus.add("Dashboard");
        menus.add("Profile");

        if (roles.contains("ROLE_USER")) {
            menus.add("My Orders");
            menus.add("Settings");
        }

        if (roles.contains("ROLE_MANAGER")) {
            menus.add("Team Management");
            menus.add("Reports");
            menus.add("Analytics");
        }

        if (roles.contains("ROLE_ADMIN")) {
            menus.add("User Management");
            menus.add("System Settings");
            menus.add("Audit Logs");
            menus.add("Security Settings");
        }

        return menus;
    }

    // ===== Request/Response DTOs =====
    record LoginRequest(String username, String password) {}
    record RefreshRequest(String refreshToken) {}
    record TokenRequest(String token) {}
    record BotDetectRequest(String userAgent) {}
    record SessionRequest(String userId, String username) {}
    record AuditLogRequest(String eventType, String principal, String requestUri, String method, boolean success, Map<String, Object> details) {}
    record UserInfo(String username, String password, String userId, List<String> roles) {}
    record SessionInfo(String sessionId, String userId, String username, String ipAddress, Instant createdAt, Instant lastAccessedAt) {}

    // Custom HttpServletRequest Wrapper for testing bot detection with custom User-Agent
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
