# ERAF Data - MongoDB

Spring Data MongoDB 기반의 NoSQL 데이터 저장소 통합 모듈입니다.

## 기능

- **BaseDocument**: 공통 감사 필드를 포함한 기본 Document 클래스
- **자동 감사**: createdAt, updatedAt, createdBy, updatedBy 자동 관리
- **Spring Data MongoDB**: Repository 패턴 지원
- **인덱스 관리**: @Indexed 어노테이션 자동 인덱스 생성

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-mongo</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/mydb
      # 또는
      host: localhost
      port: 27017
      database: mydb
      username: user
      password: pass
      authentication-database: admin

eraf:
  mongo:
    auditing-enabled: true      # 감사 필드 자동 관리
    auto-index-creation: true   # 인덱스 자동 생성
```

## 사용법

### 1. Document 클래스 정의

```java
import com.eraf.mongo.domain.BaseIdDocument;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "users")
public class UserDocument extends BaseIdDocument {

    @Indexed(unique = true)
    @Field("email")
    private String email;

    @Field("name")
    private String name;

    @Field("age")
    private Integer age;

    // Constructors, Getters, Setters
}
```

**자동 포함 필드** (BaseIdDocument 상속):
- `_id`: String (MongoDB ObjectId)
- `created_at`: LocalDateTime
- `updated_at`: LocalDateTime
- `created_by`: String
- `updated_by`: String

### 2. Repository 인터페이스

```java
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {

    // Method Query
    Optional<UserDocument> findByEmail(String email);
    List<UserDocument> findByNameContaining(String name);
    List<UserDocument> findByAgeBetween(Integer minAge, Integer maxAge);

    // Custom Query
    @Query("{ 'email': ?0, 'active': true }")
    Optional<UserDocument> findActiveByEmail(String email);

    // Exists Query
    boolean existsByEmail(String email);
}
```

### 3. Service 예제

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDocumentRepository userRepository;

    public UserDocument createUser(String email, String name) {
        // 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists");
        }

        // Document 생성
        UserDocument user = new UserDocument();
        user.setEmail(email);
        user.setName(name);
        user.setAge(30);

        // 저장 시 자동으로 createdAt, createdBy 설정됨
        return userRepository.save(user);
    }

    public UserDocument updateUser(String id, String name) {
        UserDocument user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("User not found"));

        user.setName(name);

        // 저장 시 자동으로 updatedAt, updatedBy 설정됨
        return userRepository.save(user);
    }

    public List<UserDocument> searchByName(String keyword) {
        return userRepository.findByNameContaining(keyword);
    }
}
```

### 4. MongoTemplate 사용

```java
@Service
@RequiredArgsConstructor
public class AdvancedUserService {

    private final MongoTemplate mongoTemplate;

    public List<UserDocument> complexQuery(String name, Integer minAge) {
        Query query = new Query();

        // 동적 조건 추가
        if (name != null) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }

        if (minAge != null) {
            query.addCriteria(Criteria.where("age").gte(minAge));
        }

        // 정렬
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));

        // 페이징
        query.limit(10).skip(0);

        return mongoTemplate.find(query, UserDocument.class);
    }

    public long countActiveUsers() {
        Query query = Query.query(Criteria.where("active").is(true));
        return mongoTemplate.count(query, UserDocument.class);
    }

    public void bulkUpdate() {
        Query query = Query.query(Criteria.where("age").lt(18));
        Update update = new Update().set("active", false);
        mongoTemplate.updateMulti(query, update, UserDocument.class);
    }
}
```

## 인덱스 관리

### 1. 어노테이션 기반

```java
@Document(collection = "products")
public class ProductDocument extends BaseIdDocument {

    @Indexed(unique = true)
    private String sku;

    @Indexed
    private String category;

    @Indexed(background = true)
    private String brand;

    // Compound Index
    @CompoundIndex(name = "category_brand_idx", def = "{'category': 1, 'brand': 1}")
    static class CompoundIndexes {}
}
```

### 2. 프로그래밍 방식

```java
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        // Text Index (전문 검색)
        mongoTemplate.indexOps(UserDocument.class)
            .ensureIndex(new Index().on("name", Sort.Direction.ASC).text());

        // TTL Index (자동 삭제)
        mongoTemplate.indexOps(SessionDocument.class)
            .ensureIndex(new Index()
                .on("expired_at", Sort.Direction.ASC)
                .expire(Duration.ofHours(24)));

        // Geospatial Index
        mongoTemplate.indexOps(LocationDocument.class)
            .ensureIndex(new GeospatialIndex("location"));
    }
}
```

## Aggregation Pipeline

```java
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MongoTemplate mongoTemplate;

    public List<CategoryReport> getCategoryStatistics() {
        Aggregation aggregation = Aggregation.newAggregation(
            // Match: 활성 상품만
            Aggregation.match(Criteria.where("active").is(true)),

            // Group: 카테고리별 집계
            Aggregation.group("category")
                .count().as("count")
                .avg("price").as("avgPrice")
                .sum("quantity").as("totalQuantity"),

            // Sort: 개수 내림차순
            Aggregation.sort(Sort.Direction.DESC, "count"),

            // Limit: 상위 10개
            Aggregation.limit(10)
        );

        AggregationResults<CategoryReport> results =
            mongoTemplate.aggregate(aggregation, "products", CategoryReport.class);

        return results.getMappedResults();
    }
}
```

## 트랜잭션

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MongoTemplate mongoTemplate;

    @Transactional
    public void createOrder(OrderDocument order, List<OrderItemDocument> items) {
        // Order 저장
        mongoTemplate.save(order);

        // OrderItems 저장
        items.forEach(item -> {
            item.setOrderId(order.getId());
            mongoTemplate.save(item);
        });

        // Product 재고 감소
        items.forEach(item -> {
            Query query = Query.query(Criteria.where("_id").is(item.getProductId()));
            Update update = new Update().inc("stock", -item.getQuantity());
            mongoTemplate.updateFirst(query, update, ProductDocument.class);
        });
    }
}
```

**주의**: MongoDB 트랜잭션은 Replica Set 또는 Sharded Cluster에서만 지원됩니다.

## BaseDocument vs BaseIdDocument

| 클래스 | 용도 | 포함 필드 |
|--------|------|----------|
| `BaseDocument` | ID가 없는 임베디드 Document | createdAt, updatedAt, createdBy, updatedBy |
| `BaseIdDocument` | 최상위 Collection Document | id + BaseDocument 필드 |

```java
// 최상위 Document
@Document(collection = "orders")
public class OrderDocument extends BaseIdDocument {
    private List<OrderItem> items; // 임베디드
}

// 임베디드 Document
public class OrderItem extends BaseDocument {
    private String productId;
    private Integer quantity;
}
```

## 모범 사례

### 1. 필드명 컨벤션
```java
// Snake case 사용
@Field("created_at")
private LocalDateTime createdAt;

@Field("user_name")
private String userName;
```

### 2. Null 처리
```java
// Optional 활용
Optional<UserDocument> user = userRepository.findByEmail(email);

// 기본값 제공
@Field("active")
private Boolean active = true;
```

### 3. 인덱스 최적화
```java
// 자주 조회하는 필드에 인덱스
@Indexed
private String email;

// Compound 인덱스 활용
@CompoundIndex(def = "{'status': 1, 'created_at': -1}")
```

## 참고

- [Spring Data MongoDB Documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB Manual](https://www.mongodb.com/docs/manual/)
- [Query Methods](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongodb.repositories.queries)
