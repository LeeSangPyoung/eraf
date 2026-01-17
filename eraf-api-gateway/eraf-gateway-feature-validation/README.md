# ERAF Gateway Feature - Validation

AWS API Gateway 스타일의 요청 검증 기능을 제공하는 모듈입니다.

## 기능

- **JSON Schema 검증**: JSON Schema 기반 요청 바디 검증
- **OpenAPI 3.0 검증**: OpenAPI 스펙 기반 요청 검증
- **Request Size 검증**: 최대 요청 바디 크기 제한
- **Content-Type 검증**: 허용된 Content-Type 검증
- **헤더 검증**: 필수 헤더 존재 여부 확인
- **쿼리 파라미터 검증**: 필수 쿼리 파라미터 존재 여부 확인
- **필드 검증**: JSON 바디 내 필수 필드 검증

## 설정

### application.yml

```yaml
eraf:
  gateway:
    validation:
      # 검증 기능 활성화
      enabled: true

      # 최대 요청 바디 크기 (10MB)
      max-body-size: 10485760

      # 헤더 검증 활성화
      validate-headers: true

      # 쿼리 파라미터 검증 활성화
      validate-query-params: true

      # 바디 검증 활성화
      validate-body: true

      # Strict 모드 (알 수 없는 필드 거부)
      strict-mode: false

      # 제외할 경로 패턴
      exclude-patterns:
        - /health
        - /actuator/**
        - /swagger-ui/**

      # OpenAPI 스펙 경로 (선택사항)
      open-api-spec: classpath:openapi.yaml

      # JSON Schema 버전
      json-schema-version: draft-07

      # 캐시 설정
      cache:
        enabled: true
        max-size: 100
        ttl: 3600
```

## 사용법

### 1. JSON Schema 검증

#### JSON Schema 정의

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "minLength": 1,
      "maxLength": 100
    },
    "email": {
      "type": "string",
      "format": "email"
    },
    "age": {
      "type": "integer",
      "minimum": 0,
      "maximum": 150
    }
  },
  "required": ["name", "email"]
}
```

#### 검증 규칙 등록

```java
@RestController
@RequiredArgsConstructor
public class ValidationRuleController {

    private final ValidationRuleRepository ruleRepository;

    @PostMapping("/admin/validation-rules")
    public ValidationRule createRule(@RequestBody ValidationRuleRequest request) {
        ValidationRule rule = ValidationRule.builder()
                .pathPattern("/api/users/**")
                .method("POST")
                .jsonSchema(request.getJsonSchema())
                .maxBodySize(1024 * 1024) // 1MB
                .allowedContentTypes(List.of("application/json"))
                .requiredHeaders(Set.of("X-API-Key"))
                .validateBody(true)
                .validateHeaders(true)
                .enabled(true)
                .build();

        return ruleRepository.save(rule);
    }
}
```

### 2. OpenAPI 검증

#### OpenAPI 스펙 (openapi.yaml)

```yaml
openapi: 3.0.0
info:
  title: User API
  version: 1.0.0

paths:
  /api/users:
    post:
      operationId: createUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                name:
                  type: string
                email:
                  type: string
                  format: email
              required:
                - name
                - email
      parameters:
        - name: X-API-Key
          in: header
          required: true
          schema:
            type: string
```

#### 검증 규칙 등록

```java
ValidationRule rule = ValidationRule.builder()
        .pathPattern("/api/users")
        .method("POST")
        .openApiOperationId("createUser")
        .allowedContentTypes(List.of("application/json"))
        .validateBody(true)
        .enabled(true)
        .build();

ruleRepository.save(rule);
```

### 3. Request Size 검증

```java
ValidationRule rule = ValidationRule.builder()
        .pathPattern("/api/upload/**")
        .method("POST")
        .maxBodySize(5 * 1024 * 1024) // 5MB
        .allowedContentTypes(List.of(
            "multipart/form-data",
            "application/octet-stream"
        ))
        .validateBody(true)
        .enabled(true)
        .build();

ruleRepository.save(rule);
```

### 4. Content-Type 검증

```java
ValidationRule rule = ValidationRule.builder()
        .pathPattern("/api/**")
        .allowedContentTypes(List.of(
            "application/json",
            "application/xml",
            "text/plain"
        ))
        .validateBody(true)
        .enabled(true)
        .build();

ruleRepository.save(rule);
```

### 5. 헤더 및 쿼리 파라미터 검증

```java
ValidationRule rule = ValidationRule.builder()
        .pathPattern("/api/secure/**")
        .requiredHeaders(Set.of(
            "X-API-Key",
            "X-Request-ID",
            "Authorization"
        ))
        .requiredQueryParams(Set.of(
            "version",
            "format"
        ))
        .validateHeaders(true)
        .validateQueryParams(true)
        .enabled(true)
        .build();

ruleRepository.save(rule);
```

### 6. 필수 필드 검증

```java
ValidationRule rule = ValidationRule.builder()
        .pathPattern("/api/orders")
        .method("POST")
        .requiredFields(Set.of(
            "customerId",
            "items",
            "totalAmount"
        ))
        .allowedContentTypes(List.of("application/json"))
        .validateBody(true)
        .enabled(true)
        .build();

ruleRepository.save(rule);
```

## 에러 응답 형식

검증 실패 시 다음과 같은 형식의 응답이 반환됩니다.

### 기본 에러 응답

```json
{
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "errors": [
    "Required header 'X-API-Key' is missing",
    "Request body size exceeds maximum allowed size"
  ]
}
```

### 필드별 에러 응답

```json
{
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "errors": [
    "$.name: must be at least 1 characters long",
    "$.email: must be a valid email address"
  ],
  "fieldErrors": {
    "name": [
      "must be at least 1 characters long"
    ],
    "email": [
      "must be a valid email address"
    ]
  }
}
```

## JSON Schema 예제

### 기본 타입 검증

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "age": { "type": "integer" },
    "active": { "type": "boolean" }
  }
}
```

### 문자열 패턴 검증

```json
{
  "type": "object",
  "properties": {
    "email": {
      "type": "string",
      "pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    },
    "phone": {
      "type": "string",
      "pattern": "^\\d{3}-\\d{4}-\\d{4}$"
    }
  }
}
```

### 숫자 범위 검증

```json
{
  "type": "object",
  "properties": {
    "age": {
      "type": "integer",
      "minimum": 0,
      "maximum": 150
    },
    "price": {
      "type": "number",
      "minimum": 0,
      "exclusiveMinimum": true
    }
  }
}
```

### 배열 검증

```json
{
  "type": "object",
  "properties": {
    "items": {
      "type": "array",
      "minItems": 1,
      "maxItems": 100,
      "items": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "quantity": {
            "type": "integer",
            "minimum": 1
          }
        },
        "required": ["id", "quantity"]
      }
    }
  }
}
```

### Enum 검증

```json
{
  "type": "object",
  "properties": {
    "status": {
      "type": "string",
      "enum": ["pending", "active", "completed", "cancelled"]
    },
    "priority": {
      "type": "integer",
      "enum": [1, 2, 3, 4, 5]
    }
  }
}
```

### 중첩 객체 검증

```json
{
  "type": "object",
  "properties": {
    "user": {
      "type": "object",
      "properties": {
        "name": { "type": "string" },
        "address": {
          "type": "object",
          "properties": {
            "street": { "type": "string" },
            "city": { "type": "string" },
            "zipCode": {
              "type": "string",
              "pattern": "^\\d{5}$"
            }
          },
          "required": ["street", "city"]
        }
      },
      "required": ["name", "address"]
    }
  }
}
```

## 커스텀 Validator 추가

### 1. Validator 인터페이스

```java
public interface CustomValidator {
    ValidationResult validate(HttpServletRequest request, ValidationRule rule);
}
```

### 2. 커스텀 Validator 구현

```java
@Component
public class BusinessRuleValidator implements CustomValidator {

    @Override
    public ValidationResult validate(HttpServletRequest request, ValidationRule rule) {
        ValidationResult result = ValidationResult.success();

        // 커스텀 검증 로직
        String businessId = request.getParameter("businessId");
        if (businessId == null || !isValidBusinessId(businessId)) {
            result.addFieldError("businessId", "Invalid business ID format");
        }

        return result;
    }

    private boolean isValidBusinessId(String businessId) {
        // 비즈니스 규칙 검증 로직
        return businessId.matches("^BIZ-\\d{8}$");
    }
}
```

### 3. ValidationService 확장

```java
@Service
public class ExtendedValidationService extends ValidationService {

    private final List<CustomValidator> customValidators;

    public ExtendedValidationService(
            JsonSchemaValidator jsonSchemaValidator,
            OpenApiValidator openApiValidator,
            RequestSizeValidator requestSizeValidator,
            ContentTypeValidator contentTypeValidator,
            List<CustomValidator> customValidators) {
        super(jsonSchemaValidator, openApiValidator,
              requestSizeValidator, contentTypeValidator);
        this.customValidators = customValidators;
    }

    @Override
    public ValidationResult validateRequest(HttpServletRequest request, ValidationRule rule) {
        // 기본 검증
        ValidationResult result = super.validateRequest(request, rule);

        // 커스텀 검증
        for (CustomValidator validator : customValidators) {
            result.merge(validator.validate(request, rule));
        }

        return result;
    }
}
```

## 성능 최적화

### 1. Schema 캐싱

- JSON Schema와 OpenAPI 스펙은 자동으로 캐싱됩니다.
- 캐시 크기와 TTL은 설정으로 조정 가능합니다.

### 2. Content-Length 기반 빠른 검증

- 바디 크기 검증 시 Content-Length 헤더를 먼저 확인하여 성능을 최적화합니다.

### 3. 조건부 Request Wrapping

- 바디 검증이 필요한 경우에만 ContentCachingRequestWrapper로 래핑합니다.
- GET, DELETE 등 바디가 없는 요청은 래핑하지 않습니다.

## 필터 순서

ValidationFilter는 다음 순서로 실행됩니다:

```
BOT_DETECTION (HIGHEST + 5)
  ↓
RATE_LIMIT (HIGHEST + 10)
  ↓
VALIDATION (HIGHEST + 15) ← 여기
  ↓
IP_RESTRICTION (HIGHEST + 20)
  ↓
API_KEY_AUTH (HIGHEST + 30)
  ↓
...
```

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-validation</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 참고

- JSON Schema: https://json-schema.org/
- OpenAPI 3.0: https://swagger.io/specification/
- AWS API Gateway Request Validation: https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-method-request-validation.html
