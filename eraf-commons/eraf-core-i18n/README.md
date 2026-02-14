# ERAF Core - I18N

국제화(Internationalization) 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 메시지 관리
- **MessageService**: 다국어 메시지 조회
- **MessageAspect**: 자동 메시지 번역 (AOP)

### 2. Locale 관리
- **ErafLocaleResolver**: 사용자별 Locale 해석
- Header/Cookie/Session 기반 Locale 지원

### 3. 메시지 소스
- **DatabaseMessageSource**: DB 기반 메시지 관리
- **CachedMessageSource**: 캐시 기반 성능 최적화

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-exception (ApiResponse)

**외부 라이브러리**:
- Spring Boot AOP
- Spring Web (optional)
- Spring WebMVC (optional)
- Jakarta Servlet (optional)

## 📝 사용 예시

### 메시지 정의
```properties
# messages_ko.properties
user.not.found=사용자를 찾을 수 없습니다
user.created.success=사용자가 생성되었습니다

# messages_en.properties
user.not.found=User not found
user.created.success=User created successfully
```

### 메시지 조회
```java
@Service
public class UserService {

    @Autowired
    private MessageService messageService;

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                messageService.getMessage("user.not.found")
            ));

        userRepository.delete(user);
    }
}
```

### 파라미터 메시지
```java
// messages.properties: user.welcome=환영합니다, {0}님!

String message = messageService.getMessage("user.welcome", "홍길동");
// 결과: "환영합니다, 홍길동님!"
```

### Locale 기반 메시지
```java
@GetMapping("/messages/{key}")
public String getMessage(@PathVariable String key, Locale locale) {
    return messageService.getMessage(key, locale);
}
```

### API 응답 자동 번역
```java
@MessageTranslate // 자동으로 응답 메시지 번역
@GetMapping("/users")
public ApiResponse<List<User>> getUsers() {
    return ApiResponse.success(userService.findAll(), "user.list.success");
}
```

### Locale Resolver 설정
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en
        registry.addInterceptor(interceptor);
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new ErafLocaleResolver(); // Header/Cookie/Session 지원
    }
}
```

### Header 기반 Locale
```http
GET /api/users
Accept-Language: ko-KR
```

### Cookie 기반 Locale
```http
GET /api/users
Cookie: locale=en
```

## 🏗️ 주요 클래스

**메시지**:
- `MessageService` - 메시지 조회 서비스
- `MessageAspect` - 자동 번역 AOP
- `@MessageTranslate` - 자동 번역 어노테이션

**Locale**:
- `ErafLocaleResolver` - Locale 해석
- `LocaleContext` - 현재 Locale 관리

**메시지 소스**:
- `DatabaseMessageSource` - DB 기반
- `CachedMessageSource` - 캐시 기반

## 📚 Locale 우선순위

1. **URL Parameter**: `?lang=en`
2. **HTTP Header**: `Accept-Language: ko-KR`
3. **Cookie**: `locale=en`
4. **Session**: `session.getAttribute("locale")`
5. **Default**: `Locale.getDefault()`

## 💡 Best Practices

### 메시지 키 네이밍
```properties
# 권장: {도메인}.{액션}.{상태}
user.create.success=사용자가 생성되었습니다
user.delete.failed=사용자 삭제에 실패했습니다
order.payment.completed=결제가 완료되었습니다
```

### 에러 메시지와 통합
```java
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("U001", "user.not.found"),
    DUPLICATE_EMAIL("U002", "user.duplicate.email");

    // 메시지 키를 에러 코드에 포함
}
```

## ⚠️ 주의사항

- 메시지 파일은 UTF-8로 저장
- 파라미터는 MessageFormat 형식 사용 (`{0}`, `{1}`)
- DB 기반 메시지 사용 시 캐시 전략 필수
