# ERAF Swagger

Springdoc OpenAPI 기반 API 문서화

## 개요

ERAF Swagger는 Springdoc OpenAPI를 사용한 자동 API 문서화를 제공합니다.

### 주요 기능

- **Swagger UI 자동 생성**
- **OpenAPI 3.0 스펙 지원**
- **JWT 인증 통합**
- **API 그룹화**
- **응답 예시 자동 생성**

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-swagger</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs

  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha

eraf:
  swagger:
    title: "My API"
    description: "API 문서"
    version: "1.0.0"
    contact:
      name: "Support Team"
      email: "support@example.com"
    license:
      name: "Apache 2.0"
      url: "https://www.apache.org/licenses/LICENSE-2.0"
```

### 2. API 문서화

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "사용자 관리 API")
public class UserController {

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public List<UserDto> getUsers(
        @Parameter(description = "페이지 번호", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        return userService.findAll(page, size);
    }

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다")
    @PostMapping
    public UserDto createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "사용자 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateUserRequest.class)
            )
        )
        @RequestBody CreateUserRequest request
    ) {
        return userService.create(request);
    }

    @Operation(summary = "사용자 조회", description = "ID로 사용자를 조회합니다")
    @GetMapping("/{id}")
    public UserDto getUser(
        @Parameter(description = "사용자 ID", required = true, example = "123")
        @PathVariable Long id
    ) {
        return userService.findById(id);
    }
}
```

### 3. DTO 문서화

```java
@Schema(description = "사용자 정보")
public class UserDto {

    @Schema(description = "사용자 ID", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자명", example = "john.doe", required = true)
    private String username;

    @Schema(description = "이메일", example = "john@example.com", required = true)
    private String email;

    @Schema(description = "생성일시", example = "2024-01-01T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}

@Schema(description = "사용자 생성 요청")
public class CreateUserRequest {

    @Schema(description = "사용자명", example = "john.doe", required = true, minLength = 3, maxLength = 50)
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Schema(description = "이메일", example = "john@example.com", required = true)
    @NotBlank
    @Email
    private String email;

    @Schema(description = "비밀번호", example = "password123", required = true, format = "password")
    @NotBlank
    @Size(min = 8)
    private String password;
}
```

### 4. JWT 인증 설정

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("My API")
                .version("1.0.0")
                .description("API Documentation"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearer-jwt"));
    }
}
```

## 접근 경로

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## 모범 사례

### 1. API 그룹화

```java
@RestController
@Tag(name = "User", description = "사용자 API")
public class UserController { }

@RestController
@Tag(name = "Order", description = "주문 API")
public class OrderController { }
```

### 2. 상세한 설명 추가

```java
@Operation(
    summary = "사용자 생성",
    description = "새로운 사용자를 생성합니다. 사용자명과 이메일은 중복될 수 없습니다."
)
```

### 3. 예시 값 제공

```java
@Schema(description = "사용자명", example = "john.doe")
private String username;
```

## 참고 자료

- [Springdoc OpenAPI](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)

## 라이선스

Copyright 2024 ERAF Platform
