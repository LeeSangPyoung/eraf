# ERAF Session

JWT + Redis 기반 세션 관리 라이브러리

## 개요

ERAF Session은 Spring Session과 JWT를 통합하여 확장 가능하고 안전한 세션 관리를 제공합니다.

### 주요 기능

- **JWT 토큰 기반 인증**: Access Token + Refresh Token 발급
- **Redis 세션 저장**: 분산 환경에서 세션 공유
- **동시 세션 제어**: 사용자당 최대 세션 수 제한
- **자동 구성**: Spring Boot Auto-Configuration 지원
- **세션 이벤트**: 생성/만료/무효화 이벤트 발행
- **쿠키 설정**: 보안 쿠키 설정 자동 적용
- **토큰 갱신**: Refresh Token으로 Access Token 재발급

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-session</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Redis 의존성 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
eraf:
  session:
    # 세션 타임아웃
    timeout: 30m

    # Redis 네임스페이스
    redis-namespace: "spring:session"

    # 쿠키 설정
    cookie-name: "SESSION"
    cookie-path: "/"
    cookie-domain: ""
    secure-cookie: true
    http-only-cookie: true
    cookie-max-age: 1800
    cookie-same-site: "Lax"

    # 동시 세션 제어
    concurrent-session:
      max-sessions: 1
      kick-old: true

    # JWT 설정
    jwt:
      enabled: true
      secret: "${JWT_SECRET:your-secret-key-min-256-bits}"
      expiration: 30m
      refresh-expiration: 7d
      header-name: "Authorization"
      token-prefix: "Bearer "

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 2. 로그인 및 토큰 발급

```java
import com.eraf.session.ErafJwtTokenProvider;
import com.eraf.session.ErafSessionService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ErafJwtTokenProvider jwtTokenProvider;
    private final ErafSessionService sessionService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpSession session) {
        // 1. 사용자 인증
        User user = authService.authenticate(request.getUsername(), request.getPassword());

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // 2. JWT 토큰 쌍 생성
        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "roles", user.getRoles()
        );

        ErafJwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.createTokenPair(
            user.getId().toString(),
            claims
        );

        // 3. 세션 등록 (동시 세션 제어)
        sessionService.registerSession(
            user.getId().toString(),
            session.getId()
        );

        // 4. 세션에 사용자 정보 저장
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        return new LoginResponse(
            tokenPair.getAccessToken(),
            tokenPair.getRefreshToken(),
            user
        );
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            // 사용자의 모든 세션 무효화
            sessionService.invalidateAllSessions(userId);
        }

        session.invalidate();
    }

    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@RequestBody TokenRefreshRequest request) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // 사용자 ID 추출
        String userId = jwtTokenProvider.getUserId(request.getRefreshToken());

        // 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, null);

        return new TokenRefreshResponse(newAccessToken);
    }
}
```

### 3. JWT 인증 필터

```java
import com.eraf.session.ErafJwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ErafJwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(ErafJwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        String token = jwtTokenProvider.resolveToken(bearerToken);

        // 2. 토큰 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 사용자 정보 추출
            String userId = jwtTokenProvider.getUserId(token);
            Map<String, Object> claims = jwtTokenProvider.getClaims(token);

            // 4. Spring Security 인증 객체 생성
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
```

### 4. Security 설정

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ErafJwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())

            // 세션 정책: STATELESS (JWT 기반)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )

            // JWT 필터 추가
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```

### 5. 세션 서비스 활용

```java
@Service
public class UserService {

    private final ErafSessionService sessionService;

    /**
     * 사용자의 활성 세션 수 조회
     */
    public long getActiveSessionCount(String userId) {
        return sessionService.getActiveSessionCount(userId);
    }

    /**
     * 사용자의 모든 세션 강제 로그아웃
     */
    public void forceLogoutUser(String userId) {
        sessionService.invalidateAllSessions(userId);
        log.info("User {} force logged out from all sessions", userId);
    }

    /**
     * 세션 유효성 확인
     */
    public boolean isSessionValid(String sessionId) {
        return sessionService.isSessionValid(sessionId);
    }

    /**
     * 세션 갱신 (타임아웃 연장)
     */
    public void refreshSession(String sessionId) {
        sessionService.refreshSession(sessionId);
    }
}
```

### 6. 세션 이벤트 리스닝

```java
import com.eraf.session.SessionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener {

    @EventListener
    public void onSessionCreated(SessionEvent.SessionCreatedEvent event) {
        log.info("Session created: userId={}, sessionId={}",
            event.getUserId(), event.getSessionId());

        // 사용자 활동 기록
        auditService.logSessionCreated(event.getUserId(), event.getSessionId());
    }

    @EventListener
    public void onSessionExpired(SessionEvent.SessionExpiredEvent event) {
        log.info("Session expired: userId={}, sessionId={}",
            event.getUserId(), event.getSessionId());

        // 세션 만료 통계
        analyticsService.recordSessionExpiration(event.getUserId());
    }

    @EventListener
    public void onSessionInvalidated(SessionEvent.SessionInvalidatedEvent event) {
        log.info("Session invalidated: userId={}, sessionId={}",
            event.getUserId(), event.getSessionId());

        // 로그아웃 기록
        auditService.logLogout(event.getUserId(), event.getSessionId());
    }
}
```

## 설정

### 세션 설정

```yaml
eraf:
  session:
    # 세션 타임아웃 (Duration 형식: 30m, 1h, 2d 등)
    timeout: 30m

    # Redis 네임스페이스 (다중 애플리케이션 분리)
    redis-namespace: "spring:session"
```

### 쿠키 설정

```yaml
eraf:
  session:
    # 쿠키 이름
    cookie-name: "SESSION"

    # 쿠키 경로
    cookie-path: "/"

    # 쿠키 도메인 (서브도메인 간 공유 시 설정)
    cookie-domain: ".example.com"

    # Secure 쿠키 (HTTPS만 전송)
    secure-cookie: true

    # HttpOnly 쿠키 (JavaScript 접근 차단)
    http-only-cookie: true

    # 쿠키 최대 수명 (초)
    cookie-max-age: 1800

    # SameSite 정책 (None, Lax, Strict)
    cookie-same-site: "Lax"
```

### 동시 세션 제어

```yaml
eraf:
  session:
    concurrent-session:
      # 사용자당 최대 세션 수
      max-sessions: 1

      # 기존 세션 무효화 여부
      kick-old: true
```

- `kick-old: true`: 새 로그인 시 기존 세션 무효화
- `kick-old: false`: 최대 세션 수 도달 시 새 로그인 거부

### JWT 설정

```yaml
eraf:
  session:
    jwt:
      # JWT 활성화
      enabled: true

      # 서명 시크릿 키 (최소 256비트)
      secret: "${JWT_SECRET:your-secret-key-min-256-bits}"

      # Access Token 만료 시간
      expiration: 30m

      # Refresh Token 만료 시간
      refresh-expiration: 7d

      # Authorization 헤더 이름
      header-name: "Authorization"

      # 토큰 접두사
      token-prefix: "Bearer "
```

## JWT 토큰 구조

### Access Token

```json
{
  "sub": "user-123",
  "jti": "550e8400-e29b-41d4-a716-446655440000",
  "type": "access",
  "userId": 123,
  "username": "john.doe",
  "email": "john@example.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1735689600,
  "exp": 1735691400
}
```

### Refresh Token

```json
{
  "sub": "user-123",
  "jti": "550e8400-e29b-41d4-a716-446655440001",
  "type": "refresh",
  "iat": 1735689600,
  "exp": 1736294400
}
```

## 실전 예제

### 로그인 및 세션 관리

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;
    private final ErafJwtTokenProvider jwtTokenProvider;
    private final ErafSessionService sessionService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 1. 사용자 인증
        User user = authService.authenticate(
            request.getUsername(),
            request.getPassword()
        );

        // 2. 세션 생성
        HttpSession session = httpRequest.getSession(true);

        // 3. JWT 토큰 발급
        Map<String, Object> claims = buildClaims(user);
        ErafJwtTokenProvider.TokenPair tokens = jwtTokenProvider.createTokenPair(
            user.getId().toString(),
            claims
        );

        // 4. 세션 등록 (동시 세션 제어)
        sessionService.registerSession(
            user.getId().toString(),
            session.getId()
        );

        // 5. 세션에 사용자 정보 저장
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("loginTime", Instant.now());

        // 6. 로그인 이벤트 발행
        eventPublisher.publishEvent(new UserLoggedInEvent(this, user.getId()));

        return ResponseEntity.ok(new LoginResponse(
            tokens.getAccessToken(),
            tokens.getRefreshToken(),
            user
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            // 모든 세션 무효화
            sessionService.invalidateAllSessions(userId);

            // 로그아웃 이벤트
            eventPublisher.publishEvent(new UserLoggedOutEvent(this, userId));
        }

        session.invalidate();
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));
        return claims;
    }
}
```

### 토큰 갱신

```java
@RestController
@RequestMapping("/api/auth")
public class TokenRefreshController {

    private final ErafJwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @RequestBody TokenRefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 만료 확인
        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. 사용자 ID 추출
        String userId = jwtTokenProvider.getUserId(refreshToken);

        // 4. 사용자 정보 조회
        User user = userService.findById(Long.parseLong(userId))
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        // 5. 새 Access Token 발급
        Map<String, Object> claims = buildClaims(user);
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, claims);

        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken));
    }
}
```

### 관리자 기능

```java
@RestController
@RequestMapping("/admin/sessions")
@PreAuthorize("hasRole('ADMIN')")
public class SessionAdminController {

    private final ErafSessionService sessionService;
    private final UserService userService;

    @GetMapping("/users/{userId}/sessions")
    public ResponseEntity<SessionInfo> getUserSessions(@PathVariable String userId) {
        long activeCount = sessionService.getActiveSessionCount(userId);

        return ResponseEntity.ok(new SessionInfo(
            userId,
            activeCount,
            sessionService.isSessionValid(userId)
        ));
    }

    @DeleteMapping("/users/{userId}/sessions")
    public ResponseEntity<Void> logoutUser(@PathVariable String userId) {
        sessionService.invalidateAllSessions(userId);
        log.info("Admin force logout: userId={}", userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/active-users")
    public ResponseEntity<List<ActiveUserInfo>> getActiveUsers() {
        // Redis에서 활성 사용자 조회
        List<User> users = userService.getAllUsers();

        List<ActiveUserInfo> activeUsers = users.stream()
            .filter(user -> sessionService.getActiveSessionCount(user.getId().toString()) > 0)
            .map(user -> new ActiveUserInfo(
                user.getId(),
                user.getUsername(),
                sessionService.getActiveSessionCount(user.getId().toString())
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(activeUsers);
    }
}
```

### 동시 접속 제한

```java
@Component
public class ConcurrentSessionInterceptor implements HandlerInterceptor {

    private final ErafSessionService sessionService;
    private final ErafSessionProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return true;
        }

        // 세션 수 확인
        long activeCount = sessionService.getActiveSessionCount(userId);
        int maxSessions = properties.getConcurrentSession().getMaxSessions();

        if (activeCount > maxSessions) {
            log.warn("User {} exceeded max session limit: {} > {}",
                userId, activeCount, maxSessions);

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Too many concurrent sessions");
            return false;
        }

        return true;
    }
}
```

## Redis 키 구조

### 사용자 세션 목록

```
{redis-namespace}:user:sessions:{userId} -> Set<sessionId>
```

### 세션-사용자 매핑

```
{redis-namespace}:session:user:{sessionId} -> userId
```

### Spring Session 데이터

```
{redis-namespace}:sessions:{sessionId} -> Session Data
```

## 보안 모범 사례

### 1. 시크릿 키 관리

```yaml
# 환경 변수 사용 (권장)
eraf:
  session:
    jwt:
      secret: "${JWT_SECRET}"

# 최소 256비트 키 사용
# 개발: jwt-secret-key-for-development-only-min-256-bits
# 운영: 환경 변수 또는 Vault 사용
```

### 2. HTTPS 사용

```yaml
eraf:
  session:
    secure-cookie: true  # HTTPS에서만 쿠키 전송

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

### 3. SameSite 설정

```yaml
eraf:
  session:
    cookie-same-site: "Strict"  # CSRF 보호 강화
```

### 4. 토큰 만료 시간 최소화

```yaml
eraf:
  session:
    jwt:
      expiration: 15m           # Access Token: 짧게
      refresh-expiration: 7d    # Refresh Token: 길게
```

### 5. Refresh Token 재사용 방지

```java
@Service
public class TokenRotationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String USED_TOKENS_KEY = "used:refresh:tokens:";

    public boolean isTokenUsed(String refreshToken) {
        String key = USED_TOKENS_KEY + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markTokenAsUsed(String refreshToken, Duration expiration) {
        String key = USED_TOKENS_KEY + refreshToken;
        redisTemplate.opsForValue().set(key, "1", expiration);
    }
}
```

## 제약사항

- Redis 서버 필요
- Servlet 기반 웹 애플리케이션만 지원 (WebFlux 미지원)
- JWT 시크릿 키는 최소 256비트 이상

## 참고 자료

- [Spring Session](https://spring.io/projects/spring-session)
- [JWT.io](https://jwt.io/)
- [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
- ERAF Platform 개발 로드맵: `/docs/eraf-platform-roadmap.xlsx`

## 라이선스

Copyright 2024 ERAF Platform
