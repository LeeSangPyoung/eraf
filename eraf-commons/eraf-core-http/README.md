# ERAF Core - HTTP

HTTP 클라이언트 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. HTTP 클라이언트
- **ErafHttpClient**: OkHttp 기반 HTTP 클라이언트
- GET, POST, PUT, DELETE, PATCH 메서드 지원
- JSON 자동 직렬화/역직렬화

### 2. Cookie 유틸리티
- **CookieUtils**: Cookie 생성/읽기/삭제
- Secure, HttpOnly, SameSite 지원

### 3. 응답 유틸리티
- **HttpResponseUtils**: HTTP 응답 헬퍼

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-util (JSON)
- eraf-core-exception (ErrorCode)

**외부 라이브러리**:
- OkHttp (Square)
- Jakarta Servlet (optional)

## 📝 사용 예시

### GET 요청
```java
@Service
public class ExternalApiService {

    @Autowired
    private ErafHttpClient httpClient;

    public User getUser(Long userId) {
        String url = "https://api.example.com/users/" + userId;
        return httpClient.get(url, User.class);
    }

    public List<User> getUsers() {
        String url = "https://api.example.com/users";
        return httpClient.getList(url, User.class);
    }
}
```

### POST 요청
```java
public User createUser(UserCreateRequest request) {
    String url = "https://api.example.com/users";
    return httpClient.post(url, request, User.class);
}
```

### PUT 요청
```java
public User updateUser(Long userId, UserUpdateRequest request) {
    String url = "https://api.example.com/users/" + userId;
    return httpClient.put(url, request, User.class);
}
```

### DELETE 요청
```java
public void deleteUser(Long userId) {
    String url = "https://api.example.com/users/" + userId;
    httpClient.delete(url);
}
```

### 커스텀 헤더
```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer " + token);
headers.put("X-API-Key", apiKey);

User user = httpClient.get(url, User.class, headers);
```

### Cookie 관리
```java
@Controller
public class AuthController {

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request, HttpServletResponse response) {
        String token = authService.login(request);

        // Cookie 생성 (7일, HttpOnly, Secure)
        CookieUtils.addCookie(response, "authToken", token, 7 * 24 * 60 * 60);

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        // Cookie 읽기
        String token = CookieUtils.getCookie(request, "authToken");

        if (token == null) {
            return "redirect:/login";
        }

        return "dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Cookie 삭제
        CookieUtils.deleteCookie(response, "authToken");
        return "redirect:/login";
    }
}
```

### 타임아웃 설정
```java
@Configuration
public class HttpClientConfig {

    @Bean
    public ErafHttpClient httpClient() {
        return ErafHttpClient.builder()
            .connectTimeout(10) // 10초
            .readTimeout(30)    // 30초
            .writeTimeout(30)   // 30초
            .build();
    }
}
```

### 에러 처리
```java
try {
    User user = httpClient.get(url, User.class);
} catch (HttpException e) {
    if (e.getStatusCode() == 404) {
        throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    } else if (e.getStatusCode() >= 500) {
        throw new SystemException("External API error");
    }
}
```

## 🏗️ 주요 클래스

**HTTP 클라이언트**:
- `ErafHttpClient` - HTTP 요청 수행
  - `get(url, responseType)`
  - `post(url, body, responseType)`
  - `put(url, body, responseType)`
  - `delete(url)`
  - `patch(url, body, responseType)`

**Cookie**:
- `CookieUtils` - Cookie 관리
  - `addCookie(response, name, value, maxAge)`
  - `getCookie(request, name)`
  - `deleteCookie(response, name)`

**응답**:
- `HttpResponseUtils` - HTTP 응답 헬퍼

## 📚 Cookie 속성

### Secure
HTTPS에서만 전송되도록 설정
```java
CookieUtils.addSecureCookie(response, "token", value, maxAge);
```

### HttpOnly
JavaScript에서 접근 불가 (XSS 방지)
```java
CookieUtils.addHttpOnlyCookie(response, "token", value, maxAge);
```

### SameSite
CSRF 방지
```java
CookieUtils.addSameSiteCookie(response, "token", value, maxAge, "Strict");
```

## ⚠️ 주의사항

- HTTPS 환경에서는 반드시 Secure Cookie 사용
- 민감한 정보는 HttpOnly 설정 필수
- 외부 API 호출 시 타임아웃 설정 권장
- 대용량 응답은 스트리밍 방식 고려
