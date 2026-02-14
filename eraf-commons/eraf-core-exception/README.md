# ERAF Core - Exception Handling

예외 처리, 응답 포맷, 에러 코드 관리를 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 예외 클래스
- **BusinessException**: 비즈니스 로직 예외
- **ValidationException**: 검증 실패 예외
- **SystemException**: 시스템 레벨 예외

### 2. 응답 포맷
- **ApiResponse<T>**: 표준 API 응답 형식
- **ErrorResponse**: 에러 응답 형식
- **PageResponse<T>**: 페이징 응답 형식

### 3. 에러 코드
- **ErrorCode**: 에러 코드 인터페이스
- **CommonErrorCode**: 공통 에러 코드 정의

### 4. 글로벌 예외 처리
- **GlobalExceptionHandler**: Spring MVC 전역 예외 핸들러

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-util (JsonUtils)

**외부 라이브러리**:
- Spring Web
- Spring Boot Validation

## 📝 사용 예시

### 비즈니스 예외 발생
```java
if (user == null) {
    throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "User not found");
}
```

### API 응답 생성
```java
@GetMapping("/users/{id}")
public ApiResponse<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ApiResponse.success(user);
}
```

### 에러 코드 정의
```java
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("U002", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD("U003", "비밀번호가 올바르지 않습니다");

    private final String code;
    private final String message;

    // constructor, getters...
}
```

### 글로벌 예외 처리
```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(e));
    }
}
```

## 🏗️ 주요 클래스

**예외**:
- `BusinessException` - 비즈니스 로직 예외
- `ValidationException` - 검증 실패 예외
- `SystemException` - 시스템 예외

**응답**:
- `ApiResponse<T>` - 성공 응답 (data, message, timestamp)
- `ErrorResponse` - 에러 응답 (code, message, errors)
- `PageResponse<T>` - 페이징 응답

**에러 코드**:
- `ErrorCode` - 인터페이스
- `CommonErrorCode` - 공통 에러 (400, 404, 500 등)

**핸들러**:
- `GlobalExceptionHandler` - @ExceptionHandler 기반 전역 처리

## 📚 표준 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "message": "Success",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 에러 응답
```json
{
  "success": false,
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다",
  "errors": [
    {
      "field": "userId",
      "message": "Invalid user ID"
    }
  ],
  "timestamp": "2024-01-01T12:00:00"
}
```
