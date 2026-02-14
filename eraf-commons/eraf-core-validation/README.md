# ERAF Core - Validation

Jakarta Bean Validation 기반 커스텀 검증기를 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 한국 비즈니스 검증
- **@BusinessNo**: 사업자등록번호 검증
- **@Phone**: 한국 전화번호 검증

### 2. 일반 검증
- **@Email**: 이메일 형식 검증 (강화된 패턴)
- **@Password**: 비밀번호 강도 검증

### 3. 보안 검증
- **@NoXss**: XSS 공격 패턴 차단
- **@NoSqlInjection**: SQL Injection 패턴 차단
- **@NoPathTraversal**: Path Traversal 공격 차단

### 4. 파일 검증
- **@FileExtension**: 허용된 파일 확장자 검증

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-util (StringUtils)

**외부 라이브러리**:
- Spring Boot Validation (Jakarta Validation)

## 📝 사용 예시

### DTO 검증
```java
public class UserCreateRequest {

    @Email(message = "유효한 이메일 주소를 입력하세요")
    private String email;

    @Password(
        minLength = 8,
        requireUppercase = true,
        requireLowercase = true,
        requireDigit = true,
        requireSpecialChar = true
    )
    private String password;

    @Phone
    private String phone;

    @NoXss
    private String name;
}
```

### 사업자등록번호 검증
```java
public class CompanyRequest {

    @BusinessNo(message = "유효한 사업자등록번호를 입력하세요")
    private String businessNumber; // 123-45-67890
}
```

### 파일 업로드 검증
```java
public class FileUploadRequest {

    @FileExtension(
        allowed = {"jpg", "png", "pdf"},
        message = "jpg, png, pdf 파일만 업로드 가능합니다"
    )
    private String fileName;
}
```

### 보안 검증
```java
public class ArticleRequest {

    @NoXss(message = "XSS 공격 패턴이 감지되었습니다")
    private String title;

    @NoSqlInjection
    private String content;
}
```

### Controller에서 사용
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ApiResponse<User> createUser(
        @Valid @RequestBody UserCreateRequest request
    ) {
        // 검증 통과 시에만 실행됨
        User user = userService.create(request);
        return ApiResponse.success(user);
    }
}
```

## 🏗️ 주요 검증기

**비즈니스**:
- `@BusinessNo` + `BusinessNoValidator` - 사업자등록번호
- `@Phone` + `PhoneValidator` - 전화번호 (010-1234-5678)

**일반**:
- `@Email` + `EmailValidator` - 이메일
- `@Password` + `PasswordValidator` - 비밀번호 강도

**보안**:
- `@NoXss` + `NoXssValidator` - XSS 방지
- `@NoSqlInjection` + `NoSqlInjectionValidator` - SQL Injection 방지
- `@NoPathTraversal` + `NoPathTraversalValidator` - Path Traversal 방지

**파일**:
- `@FileExtension` + `FileExtensionValidator` - 파일 확장자

## 📚 검증 규칙

### 사업자등록번호
- 형식: `123-45-67890` 또는 `1234567890`
- 체크섬 알고리즘 검증

### 전화번호
- 휴대폰: `010-1234-5678`
- 일반: `02-1234-5678`, `031-123-4567`

### 비밀번호
- 최소 길이 (기본 8자)
- 대문자, 소문자, 숫자, 특수문자 조합 (선택)

### XSS 패턴
- `<script>`, `javascript:`, `onerror=` 등 차단
