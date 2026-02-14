# ERAF Data JPA

Spring Data JPA 통합 및 엔터프라이즈 기능을 제공하는 모듈입니다. 감사(Auditing), 소프트 삭제(Soft Delete), 멀티테넌시(Multi-tenancy), 투명한 암호화, 슬로우 쿼리 감지, 커서 페이징 등 엔터프라이즈급 데이터 관리 기능을 포함합니다.

## 주요 기능

- **자동 감사(Auditing)**: 생성/수정 시간, 사용자 자동 기록
- **소프트 삭제(Soft Delete)**: 물리적 삭제 대신 논리적 삭제로 데이터 보존
- **멀티테넌시(Multi-tenancy)**: 테넌트 ID 자동 관리 및 필터링
- **투명한 암호화**: @Encrypt 애노테이션으로 민감 데이터 자동 암복호화
- **데이터 마스킹**: 로그/응답에서 민감 정보 마스킹
- **슬로우 쿼리 감지**: 느린 쿼리 자동 감지 및 로깅
- **동적 Specification**: 복잡한 쿼리 조건을 빌더 패턴으로 구성
- **공통 코드 관리**: 마스터 데이터 중앙 관리
- **커서 기반 페이징**: 대용량 데이터 효율적 페이징
- **낙관적 락 재시도**: 동시성 제어 최적화

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-jpa</artifactId>
</dependency>
```

---

## 1. 기본 엔티티 설정

### BaseEntity 상속

생성/수정 감사 기능이 포함된 기본 엔티티:

```java
import com.eraf.jpa.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    // BaseEntity에서 자동 제공:
    // - Instant createdAt (생성일)
    // - String createdBy (생성자)
    // - Instant updatedAt (수정일)
    // - String updatedBy (수정자)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**BaseEntity가 제공하는 필드**:

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdAt` | `Instant` | 엔티티 생성 시간 (자동) |
| `createdBy` | `String` | 엔티티 생성자 (자동) |
| `updatedAt` | `Instant` | 마지막 수정 시간 (자동) |
| `updatedBy` | `String` | 마지막 수정자 (자동) |

```yaml
# 설정
eraf:
  jpa:
    auditing-enabled: true  # 감사 활성화 (기본값: true)
```

---

## 2. 소프트 삭제 (Soft Delete)

### SoftDeleteEntity 상속

데이터를 물리적으로 삭제하지 않고 논리적으로 삭제:

```java
import com.eraf.jpa.softdelete.SoftDelete;
import com.eraf.jpa.softdelete.SoftDeleteEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
@SoftDelete  // 소프트 삭제 마크
public class Product extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    // SoftDeleteEntity에서 자동 제공:
    // - boolean deleted (삭제 여부)
    // - Instant deletedAt (삭제 시간)
    // - String deletedBy (삭제자)

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Repository에서 soft delete 쿼리 작성**:

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 삭제되지 않은 상품만 조회
    @Query("SELECT p FROM Product p WHERE p.deleted = false")
    List<Product> findActiveProducts();

    // 특정 이름의 활성 상품 조회
    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.deleted = false")
    Optional<Product> findByNameActive(String name);

    // 모든 상품 조회 (삭제된 것 포함)
    @Query("SELECT p FROM Product p")
    List<Product> findAllIncludingDeleted();
}
```

**사용 예제**:

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 생성
    public Product createProduct(String name, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        return productRepository.save(product);
    }

    // 상품 소프트 삭제
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        product.softDelete();  // 삭제 여부, 삭제 시간 자동 설정
        productRepository.save(product);
    }

    // 삭제자 정보와 함께 삭제
    public void deleteProductWithUser(Long id, String userId) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        product.softDelete(userId);  // 삭제 시간과 삭제자 설정
        productRepository.save(product);
    }

    // 상품 복원
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));

        product.restore();  // 삭제 상태 해제
        productRepository.save(product);
    }

    // 활성 상품 조회
    public List<Product> getActiveProducts() {
        return productRepository.findActiveProducts();
    }
}
```

---

## 3. 멀티테넌시 (Multi-tenancy)

### TenantEntity 상속 - 테넌트 ID 자동 관리

```java
import com.eraf.jpa.multitenancy.TenantEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    // TenantEntity에서 자동 제공:
    // - String tenantId (테넌트 ID, 자동 설정)

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
}
```

### SoftDeleteTenantEntity - 소프트 삭제 + 멀티테넌시

```java
import com.eraf.jpa.entity.SoftDeleteTenantEntity;

@Entity
@Table(name = "invoices")
public class Invoice extends SoftDeleteTenantEntity {
    // 소프트 삭제 + 멀티테넌시 + 감사 모두 지원

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String invoiceNumber;
}
```

### 설정

```yaml
eraf:
  jpa:
    multi-tenancy:
      enabled: true                    # 멀티테넌시 활성화
      header-name: X-Tenant-ID         # 테넌트 ID 헤더명
      default-tenant-id: default       # 기본 테넌트 ID
      required: true                   # 테넌트 ID 필수 여부
```

### TenantContext 사용

```java
import com.eraf.jpa.multitenancy.TenantContext;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(String orderNumber, BigDecimal amount) {
        // 현재 요청의 테넌트 ID 자동 적용
        String currentTenantId = TenantContext.getTenantId();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setAmount(amount);
        // tenantId는 엔티티 저장 시 자동 설정

        return orderRepository.save(order);
    }

    public List<Order> getOrders() {
        // 현재 테넌트의 주문만 조회
        String tenantId = TenantContext.getTenantId();
        return orderRepository.findByTenantId(tenantId);
    }
}
```

### HTTP 요청에서 테넌트 ID 전달

```bash
# 요청 헤더에 테넌트 ID 포함
curl -H "X-Tenant-ID: tenant-123" https://api.example.com/api/orders

# 자동으로 TenantContext에 설정되고 모든 엔티티에 적용
```

### TenantFilter 동작

- HTTP 요청의 `X-Tenant-ID` 헤더에서 테넌트 ID 추출
- TenantContext에 설정하여 애플리케이션 전체에서 사용 가능
- 요청 처리 후 TenantContext 정리

---

## 4. JPA 암호화

### @Encrypt 애노테이션으로 자동 암복호화

민감한 데이터(주민번호, 카드번호 등)를 DB에 자동으로 암호화하여 저장:

```java
import com.eraf.jpa.encryption.Encrypt;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Encrypt  // 자동 암복호화
    @Column(name = "ssn", length = 1000)  // 암호화로 길이 증가
    private String socialSecurityNumber;

    @Encrypt
    @Column(name = "card_number", length = 1000)
    private String creditCardNumber;

    @Encrypt
    @Column(name = "phone", length = 1000)
    private String phoneNumber;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSocialSecurityNumber() { return socialSecurityNumber; }
    public void setSocialSecurityNumber(String ssn) { this.socialSecurityNumber = ssn; }

    public String getCreditCardNumber() { return creditCardNumber; }
    public void setCreditCardNumber(String number) { this.creditCardNumber = number; }
}
```

### 암호화 키 설정

```yaml
# 환경 변수로 설정 (권장)
ERAF_ENCRYPTION_KEY=your-256-bit-base64-encoded-key

# 또는 시스템 속성
java -Deraf.encryption.key=your-key ...

# 또는 application.yml
eraf:
  encryption:
    key: ${ERAF_ENCRYPTION_KEY}
```

### 사용 예제

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());

        // 암호화될 필드들 (자동 암호화)
        user.setSocialSecurityNumber(request.getSsn());
        user.setCreditCardNumber(request.getCardNumber());
        user.setPhoneNumber(request.getPhone());

        // DB 저장 시 자동 암호화
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        // DB에서 조회 시 자동 복호화
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
```

**DB에 저장된 데이터** (암호화됨):
```
id | email | ssn (encrypted) | card_number (encrypted) | phone (encrypted)
1  | john@example.com | ASD+fg8k2Ldf...jk2L | 9sK+sd2fL+... | 2Ksd+fL9ksd...
```

**애플리케이션에서 접근** (자동 복호화됨):
```java
user.getSocialSecurityNumber();  // "123-45-6789" (자동 복호화)
user.getCreditCardNumber();      // "1234-5678-9012-3456" (자동 복호화)
```

### 주의사항

- 암호화된 컬럼은 **DB에서 직접 검색/정렬 불가능** (애플리케이션 레벨에서 처리)
- 암호화 키 변경 시 **기존 데이터 복호화 불가** (키 관리 중요)
- 암호화로 인해 **컬럼 길이 증가** (원본의 1.5~2배 추천)

---

## 5. 동적 Specification 빌더

### SpecificationBuilder로 복잡한 쿼리 조건 구성

```java
import com.eraf.jpa.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

public interface UserRepository extends JpaRepository<User, Long> {
}

@Service
public class UserService {

    private final UserRepository userRepository;

    // 동적 검색 예제
    public List<User> searchUsers(UserSearchRequest request) {
        Specification<User> spec = SpecificationBuilder.<User>builder()
            .equal("status", UserStatus.ACTIVE)
            .likeIgnoreCase("name", request.getKeyword())
            .greaterThanOrEqual("age", request.getMinAge())
            .lessThanOrEqual("age", request.getMaxAge())
            .in("role", request.getRoles())
            .build();

        return userRepository.findAll(spec);
    }

    // 조건부 검색
    public List<User> advancedSearch(UserSearchRequest request) {
        Specification<User> spec = SpecificationBuilder.<User>builder()
            .when(request.hasName(), (sb) -> sb.like("name", request.getName()))
            .when(request.hasEmail(), (sb) -> sb.like("email", request.getEmail()))
            .when(request.hasRole(), (sb) -> sb.equal("role", request.getRole()))
            .when(request.hasDateRange(), (sb) -> sb
                .dateTimeBetween("createdAt", request.getStartDate(), request.getEndDate()))
            .build();

        return userRepository.findAll(spec);
    }

    // 날짜 범위 검색
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        Specification<Order> spec = SpecificationBuilder.<Order>builder()
            .dateTimeBetween("createdAt", start, end)
            .isTrue("paid")
            .build();

        return orderRepository.findAll(spec);
    }

    // NULL 값 처리
    public List<Product> getProductsWithoutDescription() {
        Specification<Product> spec = SpecificationBuilder.<Product>builder()
            .isNull("description")
            .build();

        return productRepository.findAll(spec);
    }

    // 페이징과 함께 사용
    public Page<User> searchWithPaging(UserSearchRequest request, Pageable pageable) {
        Specification<User> spec = SpecificationBuilder.<User>builder()
            .like("name", request.getKeyword())
            .equal("status", request.getStatus())
            .build();

        return userRepository.findAll(spec, pageable);
    }
}
```

### SpecificationBuilder API

| 메서드 | 설명 | 예제 |
|--------|------|------|
| `equal(field, value)` | 동등 비교 | `.equal("status", ACTIVE)` |
| `notEqual(field, value)` | 불동등 비교 | `.notEqual("id", 1)` |
| `like(field, value)` | LIKE 검색 (대소문자 구분) | `.like("name", "%john%")` |
| `likeIgnoreCase(field, value)` | LIKE 검색 (대소문자 무시) | `.likeIgnoreCase("email", "test")` |
| `startsWith(field, value)` | 시작 문자 | `.startsWith("code", "USER")` |
| `endsWith(field, value)` | 끝 문자 | `.endsWith("email", "@example.com")` |
| `greaterThan(field, value)` | > | `.greaterThan("age", 18)` |
| `greaterThanOrEqual(field, value)` | >= | `.greaterThanOrEqual("score", 80)` |
| `lessThan(field, value)` | < | `.lessThan("price", 10000)` |
| `lessThanOrEqual(field, value)` | <= | `.lessThanOrEqual("quantity", 100)` |
| `between(field, start, end)` | BETWEEN | `.between("id", 1, 100)` |
| `in(field, values)` | IN | `.in("status", [ACTIVE, PENDING])` |
| `notIn(field, values)` | NOT IN | `.notIn("id", [1, 2, 3])` |
| `isNull(field)` | IS NULL | `.isNull("deletedAt")` |
| `isNotNull(field)` | IS NOT NULL | `.isNotNull("email")` |
| `isTrue(field)` | = true | `.isTrue("enabled")` |
| `isFalse(field)` | = false | `.isFalse("deleted")` |
| `dateBetween(field, start, end)` | 날짜 범위 (LocalDate) | `.dateBetween("birthDate", start, end)` |
| `dateTimeBetween(field, start, end)` | 날짜/시간 범위 | `.dateTimeBetween("createdAt", start, end)` |
| `when(condition, builder)` | 조건부 추가 | `.when(hasKeyword, sb -> sb.like("name", keyword))` |
| `ifNotNull(value, specFunction)` | NULL 아닐 때만 추가 | `.ifNotNull(roleId, r -> spec.equal("role.id", r))` |
| `join(field, joinField, value)` | JOIN 조건 | `.join("user", "id", 1)` |
| `fetchJoin(field)` | Fetch Join | `.fetchJoin("user")` |
| `distinct()` | DISTINCT | `.distinct()` |

---

## 6. 공통 코드 관리

### CommonCodeEntity로 마스터 데이터 관리

```java
import com.eraf.jpa.code.CommonCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// CommonCodeEntity 자동 제공 필드:
// - Long id
// - String codeGroup (코드 그룹명)
// - String code (코드값)
// - String name (코드명)
// - String description (설명)
// - Integer sortOrder (정렬순서)
// - Boolean enabled (사용여부)
// - String extraValue1~3 (추가 속성 3개)

public interface CommonCodeRepository extends JpaRepository<CommonCodeEntity, Long> {

    @Query("SELECT c FROM CommonCodeEntity c WHERE c.codeGroup = :group AND c.enabled = true AND c.deleted = false ORDER BY c.sortOrder")
    List<CommonCodeEntity> findByGroup(String group);

    @Query("SELECT c FROM CommonCodeEntity c WHERE c.codeGroup = :group AND c.code = :code AND c.deleted = false")
    Optional<CommonCodeEntity> findByGroupAndCode(String group, String code);
}
```

### 사용 예제

```java
@Service
public class CodeService {

    private final CommonCodeRepository commonCodeRepository;

    public List<CommonCodeEntity> getStatusCodes() {
        // "USER_STATUS" 그룹의 모든 활성 코드 조회
        return commonCodeRepository.findByGroup("USER_STATUS");
    }

    public Optional<CommonCodeEntity> getStatusCode(String code) {
        return commonCodeRepository.findByGroupAndCode("USER_STATUS", code);
    }

    public void createStatusCode(String code, String name) {
        CommonCodeEntity entity = new CommonCodeEntity("USER_STATUS", code, name);
        entity.setDescription("사용자 상태 코드");
        entity.setSortOrder(1);
        entity.setEnabled(true);
        commonCodeRepository.save(entity);
    }
}
```

### DB 예제 데이터

```sql
INSERT INTO common_code (code_group, code, name, description, sort_order, enabled, tenant_id)
VALUES
  ('USER_STATUS', 'ACTIVE', '활성', '활성 사용자', 1, true, 'default'),
  ('USER_STATUS', 'INACTIVE', '비활성', '비활성 사용자', 2, true, 'default'),
  ('USER_STATUS', 'SUSPENDED', '정지', '정지된 사용자', 3, true, 'default'),
  ('ORDER_STATUS', 'PENDING', '대기', '주문 대기 중', 1, true, 'default'),
  ('ORDER_STATUS', 'COMPLETED', '완료', '주문 완료', 2, true, 'default');
```

---

## 7. 커서 기반 페이징

### 대용량 데이터를 효율적으로 페이징

오프셋 기반 페이징의 문제점 해결:
- 대용량 데이터에서 OFFSET이 커질수록 성능 저하
- 페이징 중 데이터 추가/삭제 시 중복/누락 발생

```java
import com.eraf.jpa.cursor.*;

public interface ProductRepository extends JpaRepository<Product, Long>, CursorPageable<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.deleted = false ORDER BY p.id DESC")
    CursorPage<Product, Long> findAll(CursorPageRequest<Long> request);
}
```

### 사용 예제

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public CursorPage<Product, Long> getProductsCursor(Long cursor, int pageSize) {
        // 첫 페이지: cursor = null
        CursorPageRequest<Long> request = new CursorPageRequest<>(
            cursor,           // 이전 페이지의 nextCursor
            pageSize,         // 페이지 크기
            CursorPageRequest.SortDirection.DESC
        );

        return productRepository.findAll(request);
    }

    public CursorPage<Product, Long> getProductsUsingBuilder(Long cursor) {
        CursorPageRequest<Long> request = CursorPageBuilder.<Long>builder()
            .cursor(cursor)
            .size(20)
            .direction(CursorPageRequest.SortDirection.DESC)
            .build();

        return productRepository.findAll(request);
    }
}
```

### API 응답 예제

```json
{
  "content": [
    { "id": 100, "name": "Product 100", "price": 50000 },
    { "id": 99, "name": "Product 99", "price": 45000 },
    ...
  ],
  "nextCursor": 91,
  "hasMore": true,
  "size": 10
}
```

### 클라이언트 구현

```typescript
// TypeScript 클라이언트 예제
async function loadProducts(cursor?: number) {
  const response = await fetch(
    `/api/products/cursor?cursor=${cursor || ''}&size=10`
  );
  const data = await response.json();

  renderProducts(data.content);

  if (data.hasMore) {
    // 다음 페이지 로드 버튼 표시
    showLoadMoreButton(() => loadProducts(data.nextCursor));
  }
}
```

---

## 8. 슬로우 쿼리 감지

### 느린 쿼리 자동 감지 및 로깅

```yaml
eraf:
  jpa:
    slow-query:
      enabled: true
      threshold-ms: 1000          # 1초 이상 쿼리 감시
      critical-threshold-ms: 5000 # 5초 이상은 에러 레벨
      log-as-error: false         # warning 레벨로 로그
      log-stack-trace: true       # 스택 트레이스 포함
      stack-trace-depth: 10       # 스택 트레이스 깊이
```

### 자동 감지 동작

쿼리 실행 시간이 임계값을 초과하면 자동으로 경고 로그 출력:

```
[WARN ] [SLOW QUERY] Execution time: 1500ms
SQL: SELECT u FROM User u LEFT JOIN u.orders WHERE u.status = ?
Stack Trace:
  at com.example.service.UserService.getActiveUsers(UserService.java:45)
  at com.example.api.UserController.listUsers(UserController.java:30)
```

### SlowQueryDetector 프로그래밍 사용

```java
@Service
public class CustomQueryService {

    private final EntityManager entityManager;
    private final SlowQueryDetector slowQueryDetector;

    public List<User> complexQuery() {
        long startTime = System.currentTimeMillis();

        // 복잡한 쿼리 실행
        List<User> users = entityManager.createQuery(
            "SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.status = 'ACTIVE'",
            User.class
        ).getResultList();

        long executionTime = System.currentTimeMillis() - startTime;

        // 수동으로 느린 쿼리 기록
        slowQueryDetector.recordQueryExecution(
            "SELECT u FROM User u LEFT JOIN FETCH u.orders",
            executionTime
        );

        return users;
    }

    public Map<String, Long> getSlowQueryStats() {
        return slowQueryDetector.getSlowQueryStats();
    }

    public void clearStats() {
        slowQueryDetector.clearStats();
    }
}
```

---

## 9. 설정 방법

### application.yml 전체 설정 예제

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eraf_db
    username: postgres
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  jpa:
    database: postgresql
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
          fetch_size: 50

eraf:
  jpa:
    # 감사 설정
    auditing-enabled: true
    code-repository-enabled: true
    audit-log-enabled: true

    # 성능 설정
    open-in-view: false           # LazyInitializationException 방지 (권장: false)
    show-sql: false               # SQL 출력 (개발 환경에서만)
    format-sql: true              # SQL 포맷팅
    optimistic-retry-enabled: true

    # 멀티테넌시
    multi-tenancy:
      enabled: true
      header-name: X-Tenant-ID
      default-tenant-id: default
      required: false

    # 슬로우 쿼리
    slow-query:
      enabled: true
      threshold-ms: 1000
      critical-threshold-ms: 5000
      log-as-error: false
      log-stack-trace: true
      stack-trace-depth: 10

    # 마이그레이션
    flyway:
      enabled: true
      location: classpath:db/migration
      baseline-version: 1.0.0
      baseline-on-migrate: true
      validate-on-migrate: true
```

---

## 10. 실전 예제

### 완전한 엔티티 설계

```java
import com.eraf.jpa.entity.SoftDeleteTenantEntity;
import com.eraf.jpa.encryption.Encrypt;
import com.eraf.jpa.softdelete.SoftDelete;
import jakarta.persistence.*;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_tenant", columnList = "tenant_id"),
    @Index(name = "idx_customer_deleted", columnList = "deleted")
})
@SoftDelete
public class Customer extends SoftDeleteTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Encrypt  // 자동 암호화
    @Column(name = "phone_number", length = 1000)
    private String phoneNumber;

    @Encrypt
    @Column(name = "ssn", length = 1000)
    private String socialSecurityNumber;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_country", length = 100)
    private String createdCountry;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getSocialSecurityNumber() { return socialSecurityNumber; }
    public void setSocialSecurityNumber(String ssn) { this.socialSecurityNumber = ssn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Order> getOrders() { return orders; }
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }
}
```

### Repository 작성

```java
import com.eraf.jpa.specification.SpecificationBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 기본 쿼리
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.deleted = false")
    Optional<Customer> findByEmailActive(@Param("email") String email);

    // 활성 고객 목록
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Customer> findActiveCustomers();

    // 특정 기간 가입 고객
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :start AND :end AND c.deleted = false")
    List<Customer> findCreatedBetween(@Param("start") Instant start, @Param("end") Instant end);
}
```

### Service 구현

```java
import com.eraf.jpa.multitenancy.TenantContext;
import com.eraf.jpa.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // 고객 생성
    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setSocialSecurityNumber(request.getSsn());
        customer.setStatus("ACTIVE");

        return customerRepository.save(customer);
    }

    // 고객 조회
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id)
            .filter(c -> !c.isDeleted())
            .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    // 복합 검색
    public Page<Customer> searchCustomers(
            String keyword,
            String status,
            Pageable pageable) {

        Specification<Customer> spec = SpecificationBuilder.<Customer>builder()
            .when(keyword != null && !keyword.isEmpty(), (sb) ->
                sb.or(
                    (root, query, cb) -> cb.like(cb.lower(root.get("name").as(String.class)),
                        "%" + keyword.toLowerCase() + "%"),
                    (root, query, cb) -> cb.like(cb.lower(root.get("email").as(String.class)),
                        "%" + keyword.toLowerCase() + "%")
                ))
            .when(status != null, (sb) -> sb.equal("status", status))
            .isFalse("deleted")
            .build();

        return customerRepository.findAll(spec, pageable);
    }

    // 고객 수정
    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer customer = getCustomer(id);

        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setStatus(request.getStatus());

        return customerRepository.save(customer);
    }

    // 고객 삭제 (소프트 삭제)
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomer(id);
        String currentUser = getCurrentUser();
        customer.softDelete(currentUser);
        customerRepository.save(customer);
    }

    // 고객 복원
    @Transactional
    public void restoreCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        customer.restore();
        customerRepository.save(customer);
    }

    private String getCurrentUser() {
        // SecurityContext에서 현재 사용자 조회
        return "system";
    }
}
```

---

## 11. 모범 사례

### 1. 엔티티 설계

- **BaseEntity 상속**: 감사 기능 자동 지원
- **소프트 삭제 사용**: 데이터 추적성 보장
- **인덱스 설계**: 자주 사용되는 필드에 인덱스 설정
- **관계 설정**: 양방향 관계 시 한쪽은 mappedBy 사용

```java
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_status", columnList = "status"),
    @Index(name = "idx_product_category_id", columnList = "category_id")
})
public class Product extends BaseEntity {
    // ...
}
```

### 2. Repository 설계

- **명시적 쿼리 작성**: @Query 사용으로 명확한 의도 표현
- **소프트 삭제 필터**: 모든 SELECT에 `deleted = false` 조건 포함
- **페이징 지원**: 대용량 데이터는 CursorPageable 사용

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p " +
           "WHERE p.deleted = false " +
           "AND p.status = 'ACTIVE' " +
           "ORDER BY p.createdAt DESC")
    List<Product> findActiveProducts();

    @Query("SELECT p FROM Product p " +
           "WHERE p.category.id = :categoryId " +
           "AND p.deleted = false")
    Page<Product> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
}
```

### 3. 암호화 사용

- **최소 권한**: 필수 필드만 암호화
- **컬럼 길이**: 암호화된 데이터는 원본의 1.5~2배 길이 필요
- **키 관리**: 환경 변수 또는 보안 저장소에서 키 로드

```java
@Encrypt
@Column(name = "ssn", length = 1000)  // 원본 11자 → 암호화 후 ~200자
private String socialSecurityNumber;
```

### 4. 멀티테넌시

- **자동 필터링**: TenantEntity 상속으로 자동 처리
- **테넌트 검증**: 요청할 때 마다 테넌트 ID 확인
- **데이터 격리**: 물리적으로 나누기 (권장)

### 5. 쿼리 최적화

- **슬로우 쿼리 모니터링**: 임계값 설정으로 성능 문제 조기 발견
- **N+1 쿼리 방지**: @Query + JOIN FETCH 사용
- **배치 처리**: 대량 삽입/수정 시 배치 설정

```java
@Query("SELECT DISTINCT p FROM Product p " +
       "LEFT JOIN FETCH p.images " +
       "LEFT JOIN FETCH p.reviews " +
       "WHERE p.id = :id")
Optional<Product> findByIdWithRelations(@Param("id") Long id);
```

### 6. Specification 사용

- **조건부 빌드**: 사용자 입력에 따라 동적 쿼리 구성
- **복잡한 검색**: OR/AND 조합으로 복잡한 조건 구현

```java
Specification<Product> spec = SpecificationBuilder.<Product>builder()
    .when(hasKeyword, sb -> sb.like("name", keyword))
    .when(hasCategory, sb -> sb.equal("category.id", categoryId))
    .when(hasPriceRange, sb -> sb.between("price", minPrice, maxPrice))
    .isTrue("enabled")
    .build();
```

### 7. 트랜잭션 관리

- **@Transactional 사용**: 데이터 일관성 보장
- **Lazy Loading 주의**: open-in-view는 프로덕션에서 비활성화
- **격리 레벨**: 필요에 따라 격리 레벨 설정

```java
@Service
public class ProductService {

    @Transactional  // 트랜잭션 시작
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        // ...
        return productRepository.save(product);  // 트랜잭션 커밋
    }

    @Transactional(readOnly = true)  // 읽기 전용 (성능 최적화)
    public List<Product> getProducts() {
        return productRepository.findAll();
    }
}
```

---

## 14. Phase 2: Advanced Audit & Entity History (신규 기능)

Phase 2에서는 비동기 감사 로깅, 감사 로그 자동 삭제, Hibernate Envers 기반 엔티티 변경 이력 추적 기능이 추가되었습니다.

### 14.1. AsyncAuditLogger - 비동기 감사 로깅

감사 로그를 비동기로 저장하여 메인 비즈니스 로직의 성능에 영향을 주지 않습니다.

#### 주요 특징

- **@Async**: 별도 스레드에서 비동기 실행
- **REQUIRES_NEW**: 독립적인 트랜잭션으로 메인 트랜잭션 롤백에 영향받지 않음
- **Fire-and-Forget**: 비동기 로깅 실패 시에도 메인 로직은 성공

#### 사용 예제

```java
import com.eraf.jpa.audit.AsyncAuditLogger;
import com.eraf.jpa.audit.AuditEventStandard;
import com.eraf.jpa.audit.AuditLogEntity;

@Service
public class OrderService {

    private final AsyncAuditLogger asyncAuditLogger;
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 주문 생성
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(request.getTotalAmount());
        order = orderRepository.save(order);

        // 감사 로그 비동기 저장 (메인 트랜잭션과 독립)
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.CREATE)
                .resource("Order")
                .resourceId(String.valueOf(order.getId()))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(request.getUserId())
                .username(request.getUsername())
                .clientIp(request.getClientIp())
                .requestUri("/api/orders")
                .requestMethod("POST")
                .description("Order created with amount: " + order.getTotalAmount())
                .build();

        // Fire-and-Forget: 실패해도 주문 생성은 성공
        asyncAuditLogger.logAndForget(auditLog);

        return order;
    }

    @Transactional
    public void deleteOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        orderRepository.delete(order);

        // 삭제 감사 로그
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.DELETE)
                .resource("Order")
                .resourceId(String.valueOf(orderId))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(userId)
                .description("Order deleted")
                .build();

        asyncAuditLogger.logAndForget(auditLog);
    }
}
```

#### CompletableFuture로 결과 확인

```java
@Service
public class PaymentService {

    private final AsyncAuditLogger asyncAuditLogger;

    public PaymentResult processPayment(PaymentRequest request) {
        try {
            // 결제 처리
            PaymentResult result = paymentGateway.charge(request);

            // 비동기 감사 로그 (결과 확인)
            AuditLogEntity auditLog = AuditEventStandard.builder()
                    .action(AuditEventStandard.Action.PAYMENT)
                    .resource("Payment")
                    .result(AuditEventStandard.Result.SUCCESS)
                    .userId(request.getUserId())
                    .description("Payment processed: " + result.getTransactionId())
                    .build();

            // 비동기 실행 후 결과 처리
            asyncAuditLogger.logAsync(auditLog)
                    .thenAccept(auditLogId -> {
                        log.info("Audit log saved with ID: {}", auditLogId);
                    })
                    .exceptionally(throwable -> {
                        log.error("Failed to save audit log", throwable);
                        return null;
                    });

            return result;

        } catch (Exception e) {
            // 결제 실패 감사 로그
            AuditLogEntity errorLog = AuditEventStandard.builder()
                    .action(AuditEventStandard.Action.PAYMENT)
                    .resource("Payment")
                    .result(AuditEventStandard.Result.FAILURE)
                    .userId(request.getUserId())
                    .errorMessage(e.getMessage())
                    .build();

            asyncAuditLogger.logAndForget(errorLog);

            throw e;
        }
    }
}
```

#### AuditEventStandard - 표준 감사 이벤트

```java
import com.eraf.jpa.audit.AuditEventStandard;

// 표준 액션 상수
AuditEventStandard.Action.CREATE     // 생성
AuditEventStandard.Action.READ       // 조회
AuditEventStandard.Action.UPDATE     // 수정
AuditEventStandard.Action.DELETE     // 삭제
AuditEventStandard.Action.LOGIN      // 로그인
AuditEventStandard.Action.LOGOUT     // 로그아웃
AuditEventStandard.Action.EXPORT     // 데이터 내보내기
AuditEventStandard.Action.IMPORT     // 데이터 가져오기

// 표준 결과 상수
AuditEventStandard.Result.SUCCESS    // 성공
AuditEventStandard.Result.FAILURE    // 실패
AuditEventStandard.Result.ERROR      // 에러

// 빌더로 감사 로그 생성
AuditLogEntity auditLog = AuditEventStandard.builder()
        .action(AuditEventStandard.Action.CREATE)
        .resource("User")
        .resourceId("12345")
        .result(AuditEventStandard.Result.SUCCESS)
        .userId("admin")
        .username("Administrator")
        .clientIp("192.168.1.100")
        .requestUri("/api/users")
        .requestMethod("POST")
        .description("User created successfully")
        .build();
```

### 14.2. AuditLogQueryService - 감사 로그 검색

JPA Specification을 사용하여 복잡한 조건으로 감사 로그를 검색합니다.

```java
import com.eraf.jpa.audit.AuditLogQueryService;
import com.eraf.jpa.audit.AuditLogSearchCriteria;

@Service
public class AuditService {

    private final AuditLogQueryService auditLogQueryService;

    // 사용자별 감사 로그 조회
    public Page<AuditLogEntity> getUserAuditLogs(String userId, Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .userId(userId)
                .build();

        return auditLogQueryService.search(criteria, pageable);
    }

    // 리소스별 감사 로그 조회
    public Page<AuditLogEntity> getResourceAuditLogs(String resource, String resourceId, Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .resource(resource)
                .resourceId(resourceId)
                .build();

        return auditLogQueryService.search(criteria, pageable);
    }

    // 기간별 실패한 작업 조회
    public Page<AuditLogEntity> getFailedActions(Instant from, Instant to, Pageable pageable) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .result("FAILURE")
                .timestampFrom(from)
                .timestampTo(to)
                .build();

        return auditLogQueryService.search(criteria, pageable);
    }

    // 복합 조건 검색
    public Page<AuditLogEntity> searchAuditLogs(
            String userId,
            String action,
            String resource,
            Instant from,
            Instant to,
            Pageable pageable) {

        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .timestampFrom(from)
                .timestampTo(to)
                .deleted(false)  // 삭제되지 않은 로그만
                .build();

        return auditLogQueryService.search(criteria, pageable);
    }

    // 특정 사용자의 최근 활동
    public List<AuditLogEntity> getRecentUserActivities(String userId, int limit) {
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .userId(userId)
                .deleted(false)
                .build();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogQueryService.search(criteria, pageable).getContent();
    }
}
```

#### AuditLogSearchCriteria 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `userId` | String | 사용자 ID |
| `username` | String | 사용자 이름 |
| `action` | String | 액션 (CREATE, UPDATE, DELETE 등) |
| `resource` | String | 리소스 타입 (User, Order 등) |
| `resourceId` | String | 리소스 ID |
| `result` | String | 결과 (SUCCESS, FAILURE, ERROR) |
| `timestampFrom` | Instant | 시작 시간 |
| `timestampTo` | Instant | 종료 시간 |
| `clientIp` | String | 클라이언트 IP |
| `deleted` | Boolean | 삭제 여부 |

### 14.3. AuditLogRetentionPolicy - 자동 삭제

감사 로그를 자동으로 정리하여 데이터베이스 용량을 관리합니다.

#### 2단계 삭제 정책

1. **Soft Delete**: 보존 기간 경과 후 논리적 삭제 (deleted = true)
2. **Hard Delete**: Hard Delete 기간 경과 후 물리적 삭제 (DB에서 완전 제거)

#### 설정

```yaml
eraf:
  jpa:
    audit-retention:
      enabled: true                      # 자동 삭제 활성화
      retention-days: 365                # 365일 후 Soft Delete
      hard-delete-enabled: true          # Hard Delete 활성화
      hard-delete-after-days: 730        # Soft Delete 후 365일 후 Hard Delete (총 730일)
      cron: "0 0 2 * * ?"                # 매일 오전 2시 Soft Delete 실행
      hard-delete-cron: "0 0 3 * * SUN" # 매주 일요일 오전 3시 Hard Delete 실행
```

#### 동작 흐름

```
[Day 0]      생성                     → audit_logs 테이블에 저장 (deleted = false)
[Day 365]    Soft Delete (오전 2시)  → deleted = true, deleted_at = now
[Day 730]    Hard Delete (일요일 3시) → DB에서 완전 삭제
```

#### 수동 실행

```java
import com.eraf.jpa.audit.AuditLogRetentionPolicy;

@Service
public class AdminService {

    private final AuditLogRetentionPolicy retentionPolicy;

    // Soft Delete 수동 실행
    public int softDeleteOldLogs() {
        return retentionPolicy.softDeleteOldLogs();
    }

    // Hard Delete 수동 실행
    public int hardDeleteOldLogs() {
        return retentionPolicy.hardDeleteOldLogs();
    }

    // 특정 기간의 로그 삭제
    public int deleteLogsBefore(Instant cutoffDate) {
        // 커스텀 삭제 로직 구현
        // ...
        return 0;
    }
}
```

### 14.4. Hibernate Envers - 엔티티 변경 이력 추적

Hibernate Envers를 통해 엔티티의 모든 변경 사항을 자동으로 추적합니다.

#### 설정

```yaml
eraf:
  jpa:
    envers:
      enabled: true  # Envers 활성화

spring:
  jpa:
    properties:
      org.hibernate.envers:
        audit_table_suffix: _aud           # 감사 테이블 접미사 (기본값)
        revision_field_name: rev           # 리비전 필드 이름
        revision_type_field_name: revtype  # 리비전 타입 필드 이름
```

#### 엔티티에 @Audited 추가

```java
import org.hibernate.envers.Audited;
import com.eraf.jpa.entity.BaseEntity;

@Entity
@Table(name = "users")
@Audited  // Envers 감사 활성화
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    // Getters and Setters
}
```

**데이터베이스 스키마**:
- `users` - 현재 데이터
- `users_aud` - 변경 이력 (Envers가 자동 생성)
- `revinfo` - 리비전 정보 (누가, 언제 변경했는지)

#### 변경 이력 예제

```sql
-- 현재 users 테이블
SELECT * FROM users WHERE id = 1;
| id | name  | email           | phone        |
|----|-------|-----------------|--------------|
| 1  | Alice | alice@email.com | 123-456-7890 |

-- users_aud 테이블 (변경 이력)
SELECT * FROM users_aud WHERE id = 1;
| id | name  | email           | phone        | rev | revtype |
|----|-------|-----------------|--------------|-----|---------|
| 1  | Alice | alice@email.com | NULL         | 1   | 0       | -- 생성 (INSERT)
| 1  | Alice | alice@email.com | 123-456-7890 | 2   | 1       | -- 수정 (UPDATE)

-- revinfo 테이블 (리비전 정보)
SELECT * FROM revinfo WHERE rev IN (1, 2);
| rev | revtstmp      | user_id | username | client_ip     |
|-----|---------------|---------|----------|---------------|
| 1   | 1704067200000 | admin   | Admin    | 192.168.1.100 |
| 2   | 1704153600000 | admin   | Admin    | 192.168.1.100 |
```

**revtype 값**:
- `0` = INSERT (생성)
- `1` = UPDATE (수정)
- `2` = DELETE (삭제)

### 14.5. EntityRevisionService - 엔티티 변경 이력 조회

Envers를 통해 저장된 엔티티 변경 이력을 조회합니다.

```java
import com.eraf.jpa.envers.EntityRevisionService;
import com.eraf.jpa.envers.EntityRevisionService.EntityRevision;

@Service
public class UserHistoryService {

    private final EntityRevisionService entityRevisionService;

    // 특정 리비전의 엔티티 조회
    public User getUserAtRevision(Long userId, Long revisionNumber) {
        return entityRevisionService.findEntityAtRevision(User.class, userId, revisionNumber);
    }

    // 엔티티의 모든 변경 이력 조회
    public List<EntityRevision<User>> getUserHistory(Long userId) {
        return entityRevisionService.getEntityHistory(User.class, userId);
    }

    // 특정 시점의 엔티티 조회
    public User getUserAtDate(Long userId, Date date) {
        return entityRevisionService.findEntityAtDate(User.class, userId, date);
    }

    // 모든 리비전 번호 조회
    public List<Number> getUserRevisions(Long userId) {
        return entityRevisionService.getRevisions(User.class, userId);
    }

    // 특정 사용자가 변경한 엔티티 조회
    public List<EntityRevision<User>> getUserChangesByAdmin(String adminId) {
        return entityRevisionService.findChangedByUser(User.class, adminId);
    }

    // 두 리비전 간의 변경 사항 조회
    public List<User> getChangedUsersBetweenRevisions(Long fromRevision, Long toRevision) {
        return entityRevisionService.findChangedEntities(User.class, fromRevision, toRevision);
    }
}
```

#### 변경 이력 상세 조회 예제

```java
@Service
public class AuditHistoryService {

    private final EntityRevisionService entityRevisionService;

    public void printUserHistory(Long userId) {
        List<EntityRevision<User>> history = entityRevisionService.getEntityHistory(User.class, userId);

        for (EntityRevision<User> revision : history) {
            User user = revision.getEntity();
            RevisionEntity revInfo = revision.getRevision();
            String revType = revision.getRevisionTypeString();  // "INSERT", "UPDATE", "DELETE"

            System.out.printf("Revision #%d (%s) at %s by %s (%s)%n",
                    revInfo.getId(),
                    revType,
                    revInfo.getRevisionDate(),
                    revInfo.getUsername(),
                    revInfo.getUserId());

            System.out.printf("  Name: %s, Email: %s, Phone: %s%n",
                    user.getName(),
                    user.getEmail(),
                    user.getPhone());
        }
    }

    // 변경 사항 비교
    public Map<String, Object> compareRevisions(Long userId, Long revision1, Long revision2) {
        User user1 = entityRevisionService.findEntityAtRevision(User.class, userId, revision1);
        User user2 = entityRevisionService.findEntityAtRevision(User.class, userId, revision2);

        Map<String, Object> changes = new HashMap<>();

        if (!Objects.equals(user1.getName(), user2.getName())) {
            changes.put("name", Map.of("from", user1.getName(), "to", user2.getName()));
        }

        if (!Objects.equals(user1.getEmail(), user2.getEmail())) {
            changes.put("email", Map.of("from", user1.getEmail(), "to", user2.getEmail()));
        }

        if (!Objects.equals(user1.getPhone(), user2.getPhone())) {
            changes.put("phone", Map.of("from", user1.getPhone(), "to", user2.getPhone()));
        }

        return changes;
    }
}
```

#### REST API 예제

```java
@RestController
@RequestMapping("/api/users/{userId}/history")
public class UserHistoryController {

    private final EntityRevisionService entityRevisionService;

    @GetMapping
    public ResponseEntity<List<UserRevisionDto>> getUserHistory(@PathVariable Long userId) {
        List<EntityRevision<User>> history = entityRevisionService.getEntityHistory(User.class, userId);

        List<UserRevisionDto> dtos = history.stream()
                .map(revision -> {
                    User user = revision.getEntity();
                    RevisionEntity revInfo = revision.getRevision();

                    return UserRevisionDto.builder()
                            .revisionId(revInfo.getId())
                            .revisionType(revision.getRevisionTypeString())
                            .timestamp(revInfo.getRevisionDate())
                            .userId(revInfo.getUserId())
                            .username(revInfo.getUsername())
                            .clientIp(revInfo.getClientIp())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/revisions/{revisionId}")
    public ResponseEntity<User> getUserAtRevision(
            @PathVariable Long userId,
            @PathVariable Long revisionId) {

        User user = entityRevisionService.findEntityAtRevision(User.class, userId, revisionId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/at-date")
    public ResponseEntity<User> getUserAtDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date) {

        User user = entityRevisionService.findEntityAtDate(User.class, userId, date);
        return ResponseEntity.ok(user);
    }
}
```

### 14.6. SoftDeleteTenantEntity - Soft Delete + Multi-tenancy

Soft Delete와 Multi-tenancy를 동시에 지원하는 엔티티입니다.

```java
import com.eraf.jpa.entity.SoftDeleteTenantEntity;

@Entity
@Table(name = "tenant_products")
@SoftDelete
public class TenantProduct extends SoftDeleteTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    // SoftDeleteTenantEntity에서 제공:
    // - tenant_id (자동 필터링)
    // - deleted, deleted_at, deleted_by (Soft Delete)
    // - created_at, created_by, updated_at, updated_by (Auditing)

    // Getters and Setters
}
```

**장점**:
- 테넌트별 데이터 격리
- Soft Delete로 데이터 보존
- 자동 감사 기능

### 14.7. 통합 예제

```java
import com.eraf.jpa.audit.AsyncAuditLogger;
import com.eraf.jpa.audit.AuditEventStandard;
import com.eraf.jpa.envers.EntityRevisionService;
import com.eraf.web.context.RequestContext;
import com.eraf.web.context.RequestContextHolder;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final AsyncAuditLogger asyncAuditLogger;
    private final EntityRevisionService entityRevisionService;

    @Transactional
    public User createUser(UserRequest request) {
        RequestContext context = RequestContextHolder.getContext();

        // 1. 사용자 생성
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);

        // 2. 비동기 감사 로그
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.CREATE)
                .resource("User")
                .resourceId(String.valueOf(user.getId()))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context != null ? context.getUserId() : "system")
                .username(context != null ? context.getUsername() : "System")
                .clientIp(context != null ? context.getClientIp() : "0.0.0.0")
                .requestUri(context != null ? context.getUri() : "/api/users")
                .requestMethod(context != null ? context.getMethod() : "POST")
                .description("User created: " + user.getEmail())
                .build();

        asyncAuditLogger.logAndForget(auditLog);

        // 3. Envers가 자동으로 users_aud, revinfo 테이블에 변경 이력 저장

        return user;
    }

    @Transactional
    public User updateUser(Long userId, UserRequest request) {
        RequestContext context = RequestContextHolder.getContext();

        // 1. 사용자 수정
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String oldEmail = user.getEmail();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);

        // 2. 비동기 감사 로그
        AuditLogEntity auditLog = AuditEventStandard.builder()
                .action(AuditEventStandard.Action.UPDATE)
                .resource("User")
                .resourceId(String.valueOf(userId))
                .result(AuditEventStandard.Result.SUCCESS)
                .userId(context != null ? context.getUserId() : "system")
                .description("User updated: " + oldEmail + " -> " + user.getEmail())
                .build();

        asyncAuditLogger.logAndForget(auditLog);

        // 3. Envers가 자동으로 변경 이력 저장 (UPDATE)

        return user;
    }

    // 사용자 변경 이력 조회
    @Transactional(readOnly = true)
    public List<UserHistoryDto> getUserHistory(Long userId) {
        List<EntityRevision<User>> history = entityRevisionService.getEntityHistory(User.class, userId);

        return history.stream()
                .map(revision -> {
                    User user = revision.getEntity();
                    RevisionEntity revInfo = revision.getRevision();

                    return UserHistoryDto.builder()
                            .revisionId(revInfo.getId())
                            .revisionType(revision.getRevisionTypeString())
                            .timestamp(revInfo.getRevisionDate())
                            .modifiedBy(revInfo.getUsername())
                            .name(user.getName())
                            .email(user.getEmail())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
```

### 14.8. 설정 요약

```yaml
eraf:
  jpa:
    # Auditing
    auditing-enabled: true

    # Envers (Entity History)
    envers:
      enabled: true

    # Audit Log Retention
    audit-retention:
      enabled: true
      retention-days: 365
      hard-delete-enabled: true
      hard-delete-after-days: 730
      cron: "0 0 2 * * ?"
      hard-delete-cron: "0 0 3 * * SUN"

spring:
  jpa:
    properties:
      org.hibernate.envers:
        audit_table_suffix: _aud
```

---

## 참고 자료

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [Jakarta Persistence API](https://jakarta.ee/learn/jakarta-persistence/)
- [eraf-core Documentation](../eraf-core/)
- [eraf-security Documentation](../eraf-security/)

---

## 문제 해결

### LazyInitializationException
**문제**: 트랜잭션 밖에서 연관 엔티티 접근
**해결책**:
1. open-in-view 비활성화 (권장)
2. Lazy Loading 대신 Eager Loading 사용
3. JOIN FETCH 사용

### 암호화 키 오류
**문제**: `Failed to decrypt sensitive data`
**해결책**:
1. 암호화 키가 올바르게 설정되었는지 확인
2. 키를 변경하지 않았는지 확인
3. 환경 변수 또는 시스템 속성 확인

### 멀티테넌시 필터 누락
**문제**: 다른 테넌트의 데이터 조회됨
**해결책**:
1. 모든 Repository 쿼리에 `tenant_id` 필터 추가
2. TenantEntity 상속 확인
3. TenantContext가 제대로 설정되었는지 로그 확인

