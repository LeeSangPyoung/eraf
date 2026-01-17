package com.eraf.sample.service;

import com.eraf.core.crypto.Password;
import com.eraf.sample.dto.LoginRequest;
import com.eraf.sample.dto.LoginResponse;
import com.eraf.sample.dto.SessionInfo;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuthService {

    // 테스트용 사용자 저장소 (실제로는 DB 사용)
    // 비밀번호는 bcrypt 해시로 저장
    private static final Map<String, String> USERS = new ConcurrentHashMap<>();

    static {
        // 테스트 계정 - bcrypt 해시로 저장 (eraf-core Password 유틸 사용)
        USERS.put("admin", Password.hash("admin123"));
        USERS.put("user", Password.hash("user123"));
        USERS.put("test", Password.hash("test123"));

        // 로그로 해시 확인 (개발용)
        System.out.println("=== 테스트 계정 (비밀번호는 bcrypt 해시로 저장됨) ===");
        System.out.println("admin/admin123, user/user123, test/test123");
    }

    /**
     * 로그인 처리
     */
    public LoginResponse login(LoginRequest request, HttpSession session) {
        String username = request.getUsername();
        String password = request.getPassword();

        log.info("Login attempt: username={}", username);

        // 사용자 인증 - bcrypt 해시 검증 (eraf-core Password 유틸 사용)
        String storedHash = USERS.get(username);
        if (storedHash == null || !Password.verify(password, storedHash)) {
            log.warn("Login failed: invalid credentials for username={}", username);
            return LoginResponse.builder()
                    .success(false)
                    .message("아이디 또는 비밀번호가 올바르지 않습니다.")
                    .build();
        }

        // 세션에 사용자 정보 저장
        session.setAttribute("username", username);
        session.setAttribute("loginTime", LocalDateTime.now());
        session.setMaxInactiveInterval(30 * 60); // 30분

        log.info("Login success: username={}, sessionId={}", username, session.getId());

        return LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .sessionId(session.getId())
                .username(username)
                .build();
    }

    /**
     * 로그아웃 처리
     */
    public LoginResponse logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String sessionId = session.getId();

        log.info("Logout: username={}, sessionId={}", username, sessionId);

        // 세션 무효화
        session.invalidate();

        return LoginResponse.builder()
                .success(true)
                .message("로그아웃 되었습니다.")
                .build();
    }

    /**
     * 세션 체크
     */
    public SessionInfo checkSession(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            log.debug("Session check: no active session");
            return SessionInfo.builder()
                    .valid(false)
                    .build();
        }

        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(session.getCreationTime()),
                ZoneId.systemDefault()
        );

        LocalDateTime lastAccessedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(session.getLastAccessedTime()),
                ZoneId.systemDefault()
        );

        log.debug("Session check: valid session for username={}", username);

        return SessionInfo.builder()
                .valid(true)
                .sessionId(session.getId())
                .username(username)
                .createdAt(createdAt)
                .lastAccessedAt(lastAccessedAt)
                .maxInactiveIntervalSeconds(session.getMaxInactiveInterval())
                .build();
    }
}
