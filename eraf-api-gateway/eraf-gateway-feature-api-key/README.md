# ERAF Gateway Feature - API Key

API Gateway의 API Key 인증 기능을 제공하는 모듈입니다.

## 기능

- **API Key 인증**: 헤더, Authorization 헤더, 쿼리 파라미터를 통한 API Key 인증
- **경로별 권한 관리**: PathMatcher를 사용한 경로 패턴 매칭
- **IP 제한**: 특정 IP에서만 API Key 사용 허용
- **만료 관리**: API Key 만료 시간 설정 및 검증
- **활성화/비활성화**: API Key 활성화 상태 관리
- **안전한 Key 생성**: SecureRandom과 Base64를 사용한 안전한 API Key 생성

## 포함 내용

### Domain
- `ApiKey`: API Key 도메인 모델

### Repository
- `ApiKeyRepository`: API Key 저장소 인터페이스

### Service
- `ApiKeyService`: API Key 인증 및 관리

### Filter
- `ApiKeyAuthFilter`: HTTP 요청 필터 (Order: HIGHEST + 30)

### Exception
- `InvalidApiKeyException`: API Key 인증 실패 예외

### Configuration
- `ApiKeyProperties`: 설정 클래스
- `ApiKeyAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-api-key</artifactId>
</dependency>
```

**주의**: Repository 구현체는 별도로 제공해야 합니다.
- `eraf-gateway-store-memory` 또는
- `eraf-gateway-store-jpa`

## 설정 예시

```yaml
eraf:
  gateway:
    api-key:
      enabled: true
      header-name: X-API-Key
      allow-authorization-header: true
      allow-query-parameter: false
      check-expiration: true
      check-ip-restriction: true
      check-path-restriction: true
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /public/**
```

## API Key 인증 방법

### 1. 커스텀 헤더 (권장)
```http
GET /api/users
X-API-Key: your-api-key-here
```

### 2. Authorization 헤더
```http
GET /api/users
Authorization: ApiKey your-api-key-here
```

### 3. 쿼리 파라미터 (보안상 비권장)
```http
GET /api/users?api_key=your-api-key-here
```

## 인증 실패 응답

### API Key 누락 (401)
```json
{
  "code": "API_KEY_MISSING",
  "message": "API Key가 필요합니다",
  "status": 401
}
```

### API Key 무효 (401)
```json
{
  "code": "API_KEY_INVALID",
  "message": "유효하지 않은 API Key입니다",
  "status": 401
}
```

### 경로 접근 권한 없음 (403)
```json
{
  "code": "API_KEY_PATH_NOT_ALLOWED",
  "message": "해당 경로에 대한 접근 권한이 없습니다",
  "status": 403
}
```

### IP 접근 권한 없음 (403)
```json
{
  "code": "API_KEY_IP_NOT_ALLOWED",
  "message": "해당 IP에서의 접근이 허용되지 않습니다",
  "status": 403
}
```

## API Key 생성 예시

```java
@Autowired
private ApiKeyService apiKeyService;

// 기본 API Key 생성
ApiKey apiKey = apiKeyService.createApiKey(
    "My API Key",
    "Description",
    null,  // 모든 경로 허용
    null,  // 모든 IP 허용
    null,  // Rate Limit 없음
    null   // 만료 없음
);

// 제한된 API Key 생성
ApiKey restrictedKey = apiKeyService.createApiKey(
    "Restricted Key",
    "Admin API only",
    Set.of("/api/admin/**"),              // 관리자 경로만 허용
    Set.of("192.168.1.100", "10.0.0.1"),  // 특정 IP만 허용
    100,                                   // 초당 100 요청 제한
    LocalDateTime.now().plusMonths(1)     // 1개월 후 만료
);
```

## API Key 관리

```java
// API Key 비활성화
apiKeyService.disableApiKey(apiKeyId);

// API Key 활성화
apiKeyService.enableApiKey(apiKeyId);

// API Key 재발급
ApiKey newKey = apiKeyService.regenerateApiKey(apiKeyId);

// API Key 삭제
apiKeyService.deleteApiKey(apiKeyId);

// 모든 API Key 조회
List<ApiKey> allKeys = apiKeyService.getAllApiKeys();
```

## 인증된 API Key 사용

```java
@RestController
@RequestMapping("/api")
public class MyController {

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(HttpServletRequest request) {
        // 필터에서 인증된 API Key 가져오기
        ApiKey apiKey = (ApiKey) request.getAttribute(ApiKeyAuthFilter.API_KEY_ATTRIBUTE);

        if (apiKey != null) {
            // API Key 정보 사용
            String keyName = apiKey.getName();
            Integer rateLimit = apiKey.getRateLimitPerSecond();
        }

        return ResponseEntity.ok("Success");
    }
}
```

## 사용 방법

1. 모듈 의존성 추가
2. Repository 구현체 선택 (Memory 또는 JPA)
3. 설정 파일에 api-key 설정 추가
4. ApiKeyService를 통해 API Key 생성/관리
5. 클라이언트는 API Key를 헤더에 포함하여 요청

## 빌드

```bash
mvn clean install
```

## 주의사항

- 쿼리 파라미터를 통한 API Key 전송은 보안상 비권장 (URL 로그에 노출됨)
- HTTPS를 사용하여 API Key 전송 보호
- API Key는 안전하게 저장하고 정기적으로 갱신
- Rate Limit과 함께 사용하여 API 남용 방지
