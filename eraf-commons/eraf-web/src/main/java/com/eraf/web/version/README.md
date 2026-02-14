# API 버전 관리

REST API 버전 관리를 위한 기능을 제공합니다.

## 주요 기능

- **@ApiVersion 어노테이션**: 메서드/클래스 레벨에서 API 버전 지정
- **4가지 버전 전략**: URI, Header, Param, Media Type
- **Deprecated API 지원**: 자동 경고 헤더 추가
- **버전별 라우팅**: 요청 버전에 맞는 핸들러 자동 선택

## 사용 예시

### URI 기반 버전 관리

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @ApiVersion("1")
    @GetMapping
    public List<UserV1> getUsersV1() {
        // GET /v1/users
        return userService.findAllV1();
    }

    @ApiVersion("2")
    @GetMapping
    public List<UserV2> getUsersV2() {
        // GET /v2/users
        return userService.findAllV2();
    }

    @ApiVersion(value = "1", deprecated = true,
                deprecatedMessage = "Use v2 instead",
                replacedBy = "2")
    @PostMapping
    public UserV1 createUserV1(@RequestBody UserV1 user) {
        // Deprecated API - adds warning headers
        return userService.createV1(user);
    }
}
```

### Header 기반 버전 관리

```java
@ApiVersion(value = "2", strategy = VersionStrategy.HEADER)
@GetMapping("/products")
public List<Product> getProducts() {
    // Requires header: X-API-Version: 2
    return productService.findAll();
}
```

### 설정

```yaml
eraf:
  web:
    api-version:
      enabled: true
      default-strategy: URI
      default-version: "1"
      add-deprecation-warning: true
```

### Response Headers (Deprecated API)

```
X-API-Deprecated: true
X-API-Deprecated-Version: 1
X-API-Deprecated-Message: Use v2 instead
X-API-Replaced-By: 2
```

## 버전 전략

1. **URI**: `/v1/users`, `/api/v2/products`
2. **HEADER**: `X-API-Version: 1` or `API-Version: 2`
3. **PARAM**: `?version=1` or `?api-version=2`
4. **MEDIA_TYPE**: `Accept: application/vnd.myapi.v1+json`
