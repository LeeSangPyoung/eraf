package com.eraf.sample.controller;

import com.eraf.sample.dto.LoginRequest;
import com.eraf.sample.dto.LoginResponse;
import com.eraf.sample.dto.SessionInfo;
import com.eraf.sample.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = authService.login(request, session);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpSession session) {
        LoginResponse response = authService.logout(session);
        return ResponseEntity.ok(response);
    }

    /**
     * 세션 체크
     * GET /api/auth/session
     */
    @GetMapping("/session")
    public ResponseEntity<SessionInfo> checkSession(HttpSession session) {
        SessionInfo sessionInfo = authService.checkSession(session);

        if (sessionInfo.isValid()) {
            return ResponseEntity.ok(sessionInfo);
        } else {
            return ResponseEntity.status(401).body(sessionInfo);
        }
    }

    /**
     * 현재 사용자 정보 (로그인 필요)
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity.status(401).body(
                LoginResponse.builder()
                    .success(false)
                    .message("로그인이 필요합니다.")
                    .build()
            );
        }

        return ResponseEntity.ok(
            LoginResponse.builder()
                .success(true)
                .username(username)
                .sessionId(session.getId())
                .message("인증된 사용자입니다.")
                .build()
        );
    }
}
