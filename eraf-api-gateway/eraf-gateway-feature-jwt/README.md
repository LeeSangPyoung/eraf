# ERAF Gateway Feature - JWT

API Gateway의 JWT 인증 기능을 제공하는 모듈입니다.

## 기능

- **JWT 토큰 검증**: Authorization 헤더 또는 Cookie를 통한 JWT 토큰 검증
- **eraf-core 통합**: eraf-core의 Jwt 유틸리티를 활용한 검증
- **경로별 제외**: PathMatcher를 사용한 경로 패턴 기반 검증 제외
- **클레임 전달**: 검증된 JWT 클레임을 downstream 서비스로 헤더 전달
- **유연한 토큰 추출**: Authorization 헤더 (Bearer) 및 Cookie에서 토큰 추출
- **상세한 에러 처리**: 만료, 형식 오류, 서명 오류 등 구분된 에러 코드

## 포함 내용

### Core Classes
- `JwtValidator`: JWT 검증 인터페이스
- `DefaultJwtValidator`: 기본 JWT 검증 구현체 (eraf-core 사용)
- `JwtValidationResult`: JWT 검증 결과 클래스

### Filter
- `JwtValidationFilter`: HTTP 요청 필터 (Order: HIGHEST + 35)

### Configuration
- `JwtProperties`: 설정 클래스 (prefix: eraf.gateway.jwt)
- `JwtAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-jwt</artifactId>
</dependency>
```

**참고**: 이 모듈은 다음 JWT 라이브러리를 포함합니다.
- `io.jsonwebtoken:jjwt-api` (compile)
- `io.jsonwebtoken:jjwt-impl` (runtime)
- `io.jsonwebtoken:jjwt-jackson` (runtime)

## 설정 예시

```yaml
eraf:
  gateway:
    jwt:
      enabled: true
      secret-key: your-secret-key-here-minimum-256-bits
      header-name: Authorization
      allow-cookie: true
      cookie-name: access_token
      propagate-claims: true
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /public/**
        - /auth/login
        - /auth/register
```

## JWT 토큰 인증 방법

### 1. Authorization 헤더 (Bearer 토큰, 권장)
```http
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Authorization 헤더 (Bearer 없이)
```http
GET /api/users
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Cookie
```http
GET /api/users
Cookie: access_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 인증 실패 응답

### JWT 토큰 누락 (401)
```json
{
  "code": "JWT_MISSING",
  "message": "인증 토큰이 필요합니다",
  "status": 401
}
```

응답 헤더:
```http
WWW-Authenticate: Bearer
```

### JWT 토큰 무효 (401)
```json
{
  "code": "JWT_INVALID",
  "message": "유효하지 않은 토큰입니다",
  "status": 401
}
```

### JWT 토큰 만료 (401)
```json
{
  "code": "JWT_EXPIRED",
  "message": "만료된 토큰입니다",
  "status": 401
}
```

### JWT 토큰 형식 오류 (401)
```json
{
  "code": "JWT_MALFORMED",
  "message": "잘못된 형식의 토큰입니다",
  "status": 401
}
```

### JWT 서명 오류 (401)
```json
{
  "code": "JWT_SIGNATURE_INVALID",
  "message": "토큰 서명이 유효하지 않습니다",
  "status": 401
}
```

## JWT 클레임 전달

검증에 성공하면, JWT 클레임 정보가 다음과 같이 전달됩니다:

### Request Attribute
```java
@GetMapping("/api/protected")
public ResponseEntity<?> protectedEndpoint(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    Map<String, Object> claims = (Map<String, Object>)
        request.getAttribute(JwtValidationFilter.JWT_CLAIMS_ATTRIBUTE);

    if (claims != null) {
        String userId = (String) claims.get("sub");
        List<String> roles = (List<String>) claims.get("roles");
        // ... 클레임 사용
    }

    return ResponseEntity.ok("Success");
}
```

### Response Headers (Downstream 서비스용)
검증 성공 시 다음 헤더가 자동으로 추가됩니다:
```http
X-User-Id: user-id-from-sub-claim
X-User-Roles: roles-from-claims
```

이를 통해 downstream 마이크로서비스에서 별도의 JWT 검증 없이 사용자 정보를 활용할 수 있습니다.

## JWT 토큰 생성 예시

eraf-core의 Jwt 유틸리티를 사용한 토큰 생성:

```java
import com.eraf.core.crypto.Jwt;
import java.util.Map;
import java.util.List;

// 기본 토큰 생성
String token = Jwt.generate(
    "user123",                          // subject (사용자 ID)
    Map.of("roles", List.of("USER")),   // 추가 클레임
    "your-secret-key",                  // secret key
    3600                                // 만료 시간 (초)
);

// 관리자 토큰 생성
String adminToken = Jwt.generate(
    "admin",
    Map.of(
        "roles", List.of("ADMIN", "USER"),
        "permissions", List.of("read", "write", "delete")
    ),
    "your-secret-key",
    7200  // 2시간
);
```

## 커스텀 JwtValidator 구현

기본 `DefaultJwtValidator` 대신 커스텀 구현을 사용할 수 있습니다:

```java
@Configuration
public class CustomJwtConfig {

    @Bean
    public JwtValidator customJwtValidator() {
        return new JwtValidator() {
            @Override
            public JwtValidationResult validate(String token) {
                // 커스텀 검증 로직
                // - 추가 클레임 검증
                // - 외부 서비스 연동
                // - 토큰 블랙리스트 체크 등

                // ...

                return JwtValidationResult.success(claims);
            }
        };
    }
}
```

## 보안 권장사항

### Secret Key
- **최소 256비트** (32자) 이상의 강력한 Secret Key 사용
- 환경 변수나 외부 설정 서버에서 관리
- 주기적인 Secret Key 로테이션

```yaml
eraf:
  gateway:
    jwt:
      secret-key: ${JWT_SECRET_KEY:}  # 환경 변수에서 주입
```

### HTTPS 사용
- JWT 토큰은 반드시 HTTPS를 통해 전송
- HTTP에서는 토큰이 네트워크에 노출됨

### 토큰 만료 시간
- Access Token: 짧은 만료 시간 (15분 ~ 1시간)
- Refresh Token: 긴 만료 시간 (7일 ~ 30일)
- 필요 시 Refresh Token을 별도로 구현

### 민감한 정보
- JWT 클레임에 민감한 정보 (비밀번호, 개인정보) 저장 금지
- JWT는 Base64로 인코딩되어 있어 누구나 디코딩 가능

## 경로 제외 패턴

PathMatcher를 사용하여 다양한 패턴 지원:

```yaml
eraf:
  gateway:
    jwt:
      exclude-patterns:
        - /public/**           # public으로 시작하는 모든 경로
        - /auth/**             # 인증 관련 경로
        - /actuator/**         # Spring Actuator
        - /health              # Health Check
        - /api/v1/products     # 특정 경로
        - /**/open/**          # 중간에 open이 포함된 모든 경로
```

## 통합 테스트 예시

```java
@SpringBootTest
@AutoConfigureMockMvc
class JwtValidationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${eraf.gateway.jwt.secret-key}")
    private String secretKey;

    @Test
    void testValidJwtToken() throws Exception {
        // Given: 유효한 JWT 토큰 생성
        String token = Jwt.generate(
            "testuser",
            Map.of("roles", List.of("USER")),
            secretKey,
            3600
        );

        // When: 보호된 엔드포인트 호출
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + token))
            // Then: 성공
            .andExpect(status().isOk());
    }

    @Test
    void testMissingToken() throws Exception {
        // When: 토큰 없이 호출
        mockMvc.perform(get("/api/protected"))
            // Then: 401 Unauthorized
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT_MISSING"));
    }

    @Test
    void testExpiredToken() throws Exception {
        // Given: 만료된 토큰 생성
        String expiredToken = Jwt.generate(
            "testuser",
            Map.of(),
            secretKey,
            -1  // 이미 만료
        );

        // When: 만료된 토큰으로 호출
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + expiredToken))
            // Then: 401 Unauthorized (JWT_EXPIRED)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT_EXPIRED"));
    }
}
```

## 사용 방법

1. 모듈 의존성 추가
2. Secret Key 설정 (최소 256비트)
3. 설정 파일에 JWT 설정 추가
4. 제외할 경로 패턴 설정
5. eraf-core의 Jwt 유틸리티로 토큰 생성
6. 클라이언트는 JWT 토큰을 Authorization 헤더에 포함하여 요청

## 빌드

```bash
mvn clean install
```

## 관련 모듈

- `eraf-core`: JWT 생성 및 검증 유틸리티 제공
- `eraf-gateway-common`: 공통 필터, 에러 코드, 유틸리티
- `eraf-gateway-feature-api-key`: API Key 인증 (JWT와 함께 사용 가능)

## 필터 순서

JWT 검증 필터는 다음 순서로 실행됩니다:

1. Bot Detection (HIGHEST + 5)
2. Rate Limit (HIGHEST + 10)
3. IP Restriction (HIGHEST + 20)
4. API Key Auth (HIGHEST + 30)
5. **JWT Validation (HIGHEST + 35)** ← 현재 모듈
6. Circuit Breaker (HIGHEST + 40)
7. Response Cache (HIGHEST + 50)
8. Request Transform (HIGHEST + 60)

## 주의사항

- JWT Secret Key는 반드시 외부 설정으로 관리
- HTTPS를 통해서만 JWT 토큰 전송
- 토큰 만료 시간을 적절히 설정 (너무 길지 않게)
- JWT에 민감한 정보 저장 금지
- Cookie를 사용할 경우 HttpOnly, Secure 플래그 설정 권장
