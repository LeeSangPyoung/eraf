# ERAF Security

통합 보안 솔루션을 제공하는 모듈입니다.

## 기능

- **JWT 인증**: 토큰 기반 인증 및 권한 관리
- **RBAC**: 역할 기반 접근 제어
- **API Key**: API 키 인증
- **OAuth2/OIDC**: 소셜 로그인 및 SSO
- **IP 접근 제어**: 화이트리스트/블랙리스트
- **봇 탐지**: User-Agent 기반 봇 차단
- **CORS**: Cross-Origin 정책 관리
- **보안 감사**: 인증/인가 이벤트 로깅

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-security</artifactId>
</dependency>
```

---

## 1. JWT 인증

### 설정

```yaml
eraf:
  security:
    jwt:
      secret: ${JWT_SECRET}
      access-token-validity: 3600000    # 1시간 (ms)
      refresh-token-validity: 86400000  # 24시간 (ms)
      issuer: eraf-system
```

### 사용법

#### 토큰 생성

```java
import com.eraf.security.jwt.JwtTokenProvider;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public String login(String username, String password) {
        // 인증 로직 (생략)

        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "username", user.getUsername(),
            "roles", user.getRoles()
        );

        return jwtTokenProvider.generateToken(username, claims);
    }

    public String refreshToken(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsername(refreshToken);
            return jwtTokenProvider.generateToken(username);
        }
        throw new InvalidTokenException();
    }
}
```

#### 토큰 검증

```java
@RestController
public class UserController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/users/me")
    public UserResponse getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // "Bearer " 제거

        if (!jwtTokenProvider.validateToken(jwt)) {
            throw new UnauthorizedException();
        }

        String username = jwtTokenProvider.getUsername(jwt);
        Long userId = jwtTokenProvider.getClaim(jwt, "userId", Long.class);

        return userService.getUser(userId);
    }
}
```

#### SecurityContext 통합

```java
import com.eraf.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 2. RBAC (역할 기반 접근 제어)

### 애노테이션 기반

```java
import com.eraf.security.rbac.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/users")
    @HasRole("ADMIN")  // ADMIN 역할 필요
    public List<User> getUsers() {
        return userService.findAll();
    }

    @DeleteMapping("/users/{id}")
    @HasAnyRole({"ADMIN", "SUPER_ADMIN"})  // 둘 중 하나
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    @PutMapping("/settings")
    @HasPermission("SETTINGS_WRITE")  // 권한 필요
    public void updateSettings(@RequestBody Settings settings) {
        settingsService.update(settings);
    }

    @GetMapping("/reports")
    @HasAnyPermission({"REPORT_READ", "REPORT_WRITE"})
    public List<Report> getReports() {
        return reportService.findAll();
    }
}
```

### 프로그래밍 방식

```java
import com.eraf.security.rbac.RolePermissionRegistry;

@Service
public class UserService {

    private final RolePermissionRegistry registry;

    public void updateUser(Long userId, UserRequest request) {
        User currentUser = getCurrentUser();

        // 권한 확인
        if (!registry.hasPermission(currentUser.getRoles(), "USER_WRITE")) {
            throw new AccessDeniedException("권한이 없습니다");
        }

        // 자기 자신이거나 ADMIN인 경우만
        if (!userId.equals(currentUser.getId()) &&
            !registry.hasRole(currentUser.getRoles(), "ADMIN")) {
            throw new AccessDeniedException("다른 사용자를 수정할 수 없습니다");
        }

        userRepository.update(userId, request);
    }
}
```

### 역할-권한 매핑 설정

```yaml
eraf:
  security:
    rbac:
      role-permissions:
        ADMIN:
          - USER_READ
          - USER_WRITE
          - SETTINGS_READ
          - SETTINGS_WRITE
        USER:
          - USER_READ
          - SETTINGS_READ
```

또는 코드로 등록:

```java
@Configuration
public class RbacConfig {

    @Bean
    public RolePermissionRegistry rolePermissionRegistry() {
        RolePermissionRegistry registry = new RolePermissionRegistry();

        registry.registerRolePermissions("ADMIN", Set.of(
            "USER_READ", "USER_WRITE",
            "SETTINGS_READ", "SETTINGS_WRITE"
        ));

        registry.registerRolePermissions("USER", Set.of(
            "USER_READ", "SETTINGS_READ"
        ));

        return registry;
    }
}
```

---

## 3. API Key 인증

### 설정

```yaml
eraf:
  security:
    api-key:
      enabled: true
      header-name: X-API-Key
      valid-keys:
        - key: api-key-123
          name: Mobile App
        - key: api-key-456
          name: Partner System
```

### 사용법

```java
import com.eraf.security.apikey.ApiKeyAuthenticationFilter;

@Configuration
public class ApiKeySecurityConfig {

    @Bean
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            ApiKeyAuthenticationFilter apiKeyFilter) throws Exception {

        return http
            .securityMatcher("/api/public/**")
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .build();
    }
}
```

**클라이언트 요청 예시**:
```bash
curl -H "X-API-Key: api-key-123" https://api.example.com/api/public/data
```

---

## 4. OAuth2/OIDC

### 설정

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: profile, email
          keycloak:
            client-id: ${KEYCLOAK_CLIENT_ID}
            client-secret: ${KEYCLOAK_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            scope: openid, profile, email
        provider:
          keycloak:
            issuer-uri: https://keycloak.example.com/realms/myrealm

eraf:
  security:
    oauth2:
      success-url: /oauth2/success
      failure-url: /oauth2/failure
```

### 사용법

```java
import com.eraf.security.oauth2.OAuth2SecurityConfig;

@Configuration
public class OAuth2Config extends OAuth2SecurityConfig {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);  // 기본 OAuth2 설정 적용

        http.oauth2Login(oauth2 -> oauth2
            .successHandler((request, response, authentication) -> {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                String name = oAuth2User.getAttribute("name");

                // 사용자 생성 또는 업데이트
                User user = userService.createOrUpdate(email, name);

                // JWT 토큰 생성 및 리다이렉트
                String token = jwtTokenProvider.generateToken(email);
                response.sendRedirect("/oauth2/success?token=" + token);
            })
        );
    }
}
```

---

## 5. IP 접근 제어

### 설정

```yaml
eraf:
  security:
    ip:
      enabled: true
      mode: whitelist  # whitelist 또는 blacklist
      whitelist:
        - 192.168.1.0/24
        - 10.0.0.0/8
        - 172.16.0.1
      blacklist:
        - 1.2.3.4
        - 5.6.7.0/24
```

### 사용법

```java
import com.eraf.security.ip.IpAccessControlFilter;

@Configuration
public class IpSecurityConfig {

    @Bean
    public IpAccessControlFilter ipAccessControlFilter(IpAccessControlProperties properties) {
        return new IpAccessControlFilter(properties);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, IpAccessControlFilter ipFilter) throws Exception {
        return http
            .addFilterBefore(ipFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasIpAddress("192.168.1.0/24")
                .anyRequest().authenticated())
            .build();
    }
}
```

**프로그래밍 방식 검증**:

```java
import com.eraf.security.ip.IpValidator;

@Service
public class AdminService {

    private final IpValidator ipValidator;

    public void performAdminAction(HttpServletRequest request) {
        String clientIp = getClientIp(request);

        if (!ipValidator.isAllowed(clientIp)) {
            throw new AccessDeniedException("IP 주소가 허용되지 않습니다: " + clientIp);
        }

        // 관리 작업 수행
    }
}
```

---

## 6. 봇 탐지

### 설정

```yaml
eraf:
  security:
    bot:
      enabled: true
      action: block  # block, log, captcha
      user-agent-patterns:
        - ".*bot.*"
        - ".*crawler.*"
        - ".*spider.*"
```

### 사용법

```java
import com.eraf.security.bot.*;

@Service
public class BotDetectionService {

    private final BotDetector botDetector;

    public void validateRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        BotDetectionResult result = botDetector.detect(userAgent, request);

        if (result.isBot()) {
            log.warn("Bot detected: type={}, method={}, userAgent={}",
                result.getBotType(),
                result.getDetectionMethod(),
                userAgent);

            if (result.getBotType() == BotType.MALICIOUS) {
                throw new BotBlockedException("악성 봇이 감지되었습니다");
            }
        }
    }
}
```

**커스텀 봇 탐지기**:

```java
@Component
public class RateLimitBotDetector implements BotDetector {

    private final RateLimiter rateLimiter;

    @Override
    public BotDetectionResult detect(String userAgent, HttpServletRequest request) {
        String clientIp = getClientIp(request);

        // 초당 10회 이상 요청 시 봇으로 간주
        if (rateLimiter.isExceeded(clientIp, 10, Duration.ofSeconds(1))) {
            return new BotDetectionResult(
                true,
                BotType.SUSPICIOUS,
                DetectionMethod.RATE_LIMIT,
                "Rate limit exceeded"
            );
        }

        return BotDetectionResult.notBot();
    }
}
```

---

## 7. CORS 설정

### 설정

```yaml
eraf:
  security:
    cors:
      enabled: true
      allowed-origins:
        - https://example.com
        - https://app.example.com
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      allowed-headers:
        - "*"
      exposed-headers:
        - Authorization
      allow-credentials: true
      max-age: 3600
```

### 사용법

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.getAllowedOrigins());
        config.setAllowedMethods(properties.getAllowedMethods());
        config.setAllowedHeaders(properties.getAllowedHeaders());
        config.setExposedHeaders(properties.getExposedHeaders());
        config.setAllowCredentials(properties.isAllowCredentials());
        config.setMaxAge(properties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## 8. 보안 감사 로깅

### 설정

```yaml
eraf:
  security:
    audit:
      enabled: true
      log-successful-auth: true
      log-failed-auth: true
      log-access-denied: true
```

### 사용법

```java
import com.eraf.security.audit.*;

@Service
public class AuthService {

    private final SecurityAuditLogger auditLogger;

    public String login(String username, String password) {
        try {
            User user = authenticate(username, password);

            // 성공 로그
            auditLogger.logAuthenticationSuccess(
                username,
                user.getId(),
                getClientIp()
            );

            return jwtTokenProvider.generateToken(username);

        } catch (AuthenticationException e) {
            // 실패 로그
            auditLogger.logAuthenticationFailure(
                username,
                e.getMessage(),
                getClientIp()
            );

            throw e;
        }
    }
}
```

**이벤트 리스너 사용**:

```java
import com.eraf.security.audit.SecurityEventListener;

@Component
public class CustomSecurityEventListener implements SecurityEventListener {

    @Override
    public void onAuthenticationSuccess(SecurityAuditEvent event) {
        log.info("로그인 성공: user={}, ip={}",
            event.getUsername(), event.getIpAddress());

        // 알림 발송, 메트릭 수집 등
        metricsService.incrementLoginCount(event.getUsername());
    }

    @Override
    public void onAuthenticationFailure(SecurityAuditEvent event) {
        log.warn("로그인 실패: user={}, reason={}, ip={}",
            event.getUsername(), event.getReason(), event.getIpAddress());

        // 실패 횟수 체크, 계정 잠금 등
        failureCountService.increment(event.getUsername());
    }

    @Override
    public void onAccessDenied(SecurityAuditEvent event) {
        log.warn("접근 거부: user={}, resource={}, ip={}",
            event.getUsername(), event.getResource(), event.getIpAddress());
    }
}
```

---

## 통합 예제

### 전체 보안 설정

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtFilter;
    private final ApiKeyAuthenticationFilter apiKeyFilter;
    private final IpAccessControlFilter ipFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 필터 체인
            .addFilterBefore(ipFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)

            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").authenticated()
                .anyRequest().denyAll())

            // OAuth2
            .oauth2Login(Customizer.withDefaults())

            // 예외 처리
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler()))

            .build();
    }
}
```

---

## 모범 사례

1. **Secret 관리**: 환경 변수로 관리, 코드에 하드코딩 금지
2. **토큰 만료**: Access Token 짧게 (1시간), Refresh Token 길게 (24시간)
3. **HTTPS**: 프로덕션에서 반드시 HTTPS 사용
4. **CORS**: 필요한 Origin만 허용, "*" 사용 금지
5. **감사 로그**: 인증/인가 이벤트 모두 기록
6. **IP 제한**: 관리자 페이지는 IP 화이트리스트 적용
7. **봇 탐지**: Rate Limiting과 함께 사용
8. **권한 분리**: 최소 권한 원칙 적용

---

## 참고

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [OAuth 2.0](https://oauth.net/2/)
- [OWASP Security](https://owasp.org/)
