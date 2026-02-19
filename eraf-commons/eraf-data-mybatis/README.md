# ERAF Data MyBatis

MyBatis 자동 구성 및 페이지네이션 지원

## 개요

ERAF Data MyBatis는 Spring Boot와 MyBatis를 쉽게 통합할 수 있도록 자동 구성 및 유틸리티를 제공합니다.

### 주요 기능

- **자동 구성**: MyBatis 및 PageHelper 자동 설정
- **페이지네이션**: PageHelper 기반 자동 페이징 처리
- **JSON 타입 핸들러**: JSON 컬럼 자동 매핑
- **감사 기능**: 생성/수정 시간 및 사용자 자동 기록
- **통합 예외 처리**: MyBatis 예외를 ERAF 표준 예외로 변환

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-mybatis</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Database Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass

mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.example.domain
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

pagehelper:
  helper-dialect: postgresql
  reasonable: true
  support-methods-arguments: true
  params: count=countSql
```

### 2. Mapper 인터페이스 작성

```java
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Insert("INSERT INTO users (username, email, password, created_at) " +
            "VALUES (#{username}, #{email}, #{password}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();
}
```

### 3. XML Mapper 작성

```xml
<!-- src/main/resources/mapper/UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.example.mapper.UserMapper">

    <resultMap id="UserResultMap" type="com.example.domain.User">
        <id property="id" column="id"/>
        <result property="username" column="username"/>
        <result property="email" column="email"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByUsername" resultMap="UserResultMap">
        SELECT * FROM users
        WHERE username = #{username}
    </select>

    <select id="searchUsers" resultMap="UserResultMap">
        SELECT * FROM users
        WHERE 1=1
        <if test="username != null">
            AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="email != null">
            AND email LIKE CONCAT('%', #{email}, '%')
        </if>
        ORDER BY created_at DESC
    </select>

</mapper>
```

### 4. 페이지네이션

```java
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class UserService {

    private final UserMapper userMapper;

    public PageInfo<User> findUsers(int pageNum, int pageSize, String username) {
        // PageHelper로 페이지 설정
        PageHelper.startPage(pageNum, pageSize);

        // 검색 실행
        List<User> users = userMapper.searchUsers(username);

        // PageInfo로 결과 래핑
        return new PageInfo<>(users);
    }

    public Page<User> findAllWithPage(int pageNum, int pageSize) {
        // Page 객체 직접 반환
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPage(() -> userMapper.findAll());
    }
}
```

### 5. JSON 타입 핸들러

```java
// Entity
public class Product {
    private Long id;
    private String name;

    @Column(typeHandler = JsonTypeHandler.class)
    private Map<String, Object> metadata;  // JSON 컬럼

    @Column(typeHandler = JsonTypeHandler.class)
    private List<String> tags;  // JSON 배열
}

// Mapper
@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM products WHERE id = #{id}")
    @Results({
        @Result(property = "metadata", column = "metadata",
                typeHandler = JsonTypeHandler.class),
        @Result(property = "tags", column = "tags",
                typeHandler = JsonTypeHandler.class)
    })
    Product findById(Long id);

    @Insert("INSERT INTO products (name, metadata, tags) " +
            "VALUES (#{name}, #{metadata, typeHandler=JsonTypeHandler}, " +
            "#{tags, typeHandler=JsonTypeHandler})")
    void insert(Product product);
}
```

## 실전 예제

### 복잡한 조회

```java
@Mapper
public interface OrderMapper {

    // 다중 조건 검색
    List<Order> searchOrders(
        @Param("status") String status,
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 조인 쿼리
    @Select("SELECT o.*, c.name as customer_name " +
            "FROM orders o " +
            "INNER JOIN customers c ON o.customer_id = c.id " +
            "WHERE o.id = #{id}")
    OrderWithCustomer findByIdWithCustomer(Long id);

    // 집계 쿼리
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count, " +
            "SUM(total_amount) as total " +
            "FROM orders " +
            "WHERE created_at >= #{startDate} AND created_at < #{endDate} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date DESC")
    List<OrderStats> getStatistics(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
```

### 배치 작업

```java
@Mapper
public interface ProductMapper {

    // 배치 INSERT
    @Insert({
        "<script>",
        "INSERT INTO products (name, price, category) VALUES ",
        "<foreach collection='products' item='item' separator=','>",
        "(#{item.name}, #{item.price}, #{item.category})",
        "</foreach>",
        "</script>"
    })
    void batchInsert(@Param("products") List<Product> products);

    // 배치 UPDATE
    @Update({
        "<script>",
        "<foreach collection='products' item='item' separator=';'>",
        "UPDATE products SET price = #{item.price} WHERE id = #{item.id}",
        "</foreach>",
        "</script>"
    })
    void batchUpdate(@Param("products") List<Product> products);
}
```

### 동적 SQL

```xml
<select id="searchProducts" resultType="Product">
    SELECT * FROM products
    WHERE 1=1
    <if test="name != null and name != ''">
        AND name LIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="category != null">
        AND category = #{category}
    </if>
    <if test="minPrice != null">
        AND price >= #{minPrice}
    </if>
    <if test="maxPrice != null">
        AND price <= #{maxPrice}
    </if>
    <choose>
        <when test="sortBy == 'price'">
            ORDER BY price ${sortOrder}
        </when>
        <when test="sortBy == 'name'">
            ORDER BY name ${sortOrder}
        </when>
        <otherwise>
            ORDER BY created_at DESC
        </otherwise>
    </choose>
</select>
```

## 설정 옵션

```yaml
mybatis:
  # Mapper XML 위치
  mapper-locations: classpath*:mapper/**/*.xml

  # Type Alias 패키지
  type-aliases-package: com.example.domain

  # Type Handler 패키지
  type-handlers-package: com.example.handler

  # 설정 파일
  config-location: classpath:mybatis-config.xml

  configuration:
    # Camel Case 자동 변환
    map-underscore-to-camel-case: true

    # 기본 Fetch Size
    default-fetch-size: 100

    # Statement Timeout (초)
    default-statement-timeout: 30

    # Lazy Loading
    lazy-loading-enabled: true

    # Aggressive Lazy Loading
    aggressive-lazy-loading: false

    # Multiple ResultSets
    multiple-result-sets-enabled: true

    # Generated Keys 사용
    use-generated-keys: true

    # 컬럼 자동 매핑
    auto-mapping-behavior: PARTIAL

    # Null 허용
    call-setters-on-nulls: false

    # 로그 구현체
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

pagehelper:
  # 데이터베이스 방언
  helper-dialect: postgresql

  # 페이지 번호 자동 보정
  reasonable: true

  # 메서드 파라미터로 페이지 설정
  support-methods-arguments: true

  # 파라미터 매핑
  params: count=countSql
```

## 트랜잭션 관리

```java
@Service
@Transactional
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public void createOrder(Order order, List<OrderItem> items) {
        // 주문 생성
        orderMapper.insert(order);

        // 주문 아이템 생성
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 재고 감소
        for (OrderItem item : items) {
            productMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderMapper.findById(id);
    }
}
```

## 모범 사례

### 1. Mapper 인터페이스와 XML 분리

간단한 쿼리는 어노테이션, 복잡한 쿼리는 XML 사용:

```java
@Mapper
public interface UserMapper {
    // 간단한 쿼리: 어노테이션
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    // 복잡한 쿼리: XML
    List<User> searchUsers(UserSearchCriteria criteria);
}
```

### 2. ResultMap 재사용

```xml
<resultMap id="BaseResultMap" type="User">
    <id property="id" column="id"/>
    <result property="username" column="username"/>
    <result property="email" column="email"/>
</resultMap>

<resultMap id="DetailResultMap" type="User" extends="BaseResultMap">
    <result property="createdAt" column="created_at"/>
    <result property="updatedAt" column="updated_at"/>
    <collection property="orders" ofType="Order">
        <id property="id" column="order_id"/>
        <result property="orderDate" column="order_date"/>
    </collection>
</resultMap>
```

### 3. SQL 재사용

```xml
<sql id="userColumns">
    id, username, email, created_at, updated_at
</sql>

<select id="findById" resultMap="BaseResultMap">
    SELECT <include refid="userColumns"/>
    FROM users
    WHERE id = #{id}
</select>
```

## 제약사항

- MyBatis 3.x 이상 필요
- Spring Boot 3.x 호환
- Java 17 이상

## 참고 자료

- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [MyBatis Spring Boot](https://github.com/mybatis/spring-boot-starter)
- [PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)

## 라이선스

Copyright 2024 ERAF Platform
