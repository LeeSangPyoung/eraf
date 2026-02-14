# ERAF Test - Testing Utilities

## 📋 개요

통합 테스트를 위한 유틸리티 모듈입니다.

**주요 기능**:
- ✅ TestContainers 래퍼 (PostgreSQL, Redis, Kafka, Elasticsearch 등)
- ✅ Test Data Generator (DataFaker 기반)
- ✅ Generic Test Builder 패턴
- ✅ Mock Helper 유틸리티
- ✅ Spring Boot Test 통합

## 🚀 사용 방법

### 1. 의존성 추가

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-test</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## 📦 주요 컴포넌트

### 1. TestContainers

#### PostgreSQL Container

```java
import com.eraf.test.containers.ErafPostgreSQLContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static ErafPostgreSQLContainer postgres = new ErafPostgreSQLContainer();

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testRepository() {
        // 테스트 코드
    }
}
```

**Reusable Container (여러 테스트 클래스에서 재사용)**:

```java
@Container
static ErafPostgreSQLContainer postgres = ErafPostgreSQLContainer.createReusable();
```

#### Redis Container

```java
import com.eraf.test.containers.ErafRedisContainer;

@Container
static ErafRedisContainer redis = new ErafRedisContainer();

@DynamicPropertySource
static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getRedisPort);
}
```

### 2. Test Data Generator

#### 기본 사용

```java
import com.eraf.test.data.TestDataGenerator;

class UserTest {

    @Test
    void testCreateUser() {
        // 랜덤 테스트 데이터 생성
        String name = TestDataGenerator.fullName();        // "John Doe"
        String email = TestDataGenerator.email();          // "john.doe@example.com"
        String phone = TestDataGenerator.phoneNumber();    // "555-1234"
        int age = TestDataGenerator.randomInt(18, 65);     // 18~65 사이 랜덤

        User user = new User(name, email, phone, age);
        // 테스트 실행
    }
}
```

#### 한글 데이터 생성

```java
TestDataGenerator korean = TestDataGenerator.korean();
String koreanName = korean.instanceFullName();     // "김철수"
String koreanAddress = korean.instanceAddress();   // "서울특별시..."
```

#### List 생성

```java
// 10개의 User 생성
List<User> users = TestDataGenerator.list(10, () -> new User(
    TestDataGenerator.fullName(),
    TestDataGenerator.email(),
    TestDataGenerator.randomInt(18, 65)
));
```

#### 다양한 데이터 타입

```java
// 문자열
String uuid = TestDataGenerator.uuid();
String url = TestDataGenerator.url();
String ipAddress = TestDataGenerator.ipAddress();
String sentence = TestDataGenerator.sentence();
String paragraph = TestDataGenerator.paragraph();

// 숫자
int randomInt = TestDataGenerator.randomInt(1, 100);
long randomLong = TestDataGenerator.randomLong(1000L, 9999L);
double randomDouble = TestDataGenerator.randomDouble(0.0, 100.0);
boolean randomBool = TestDataGenerator.randomBoolean();

// 날짜
LocalDate pastDate = TestDataGenerator.pastDate();
LocalDate futureDate = TestDataGenerator.futureDate();
LocalDateTime pastDateTime = TestDataGenerator.pastDateTime();
Instant pastInstant = TestDataGenerator.pastInstant();

// 주소/회사
String address = TestDataGenerator.address();
String city = TestDataGenerator.city();
String country = TestDataGenerator.country();
String zipCode = TestDataGenerator.zipCode();
String companyName = TestDataGenerator.companyName();
String jobTitle = TestDataGenerator.jobTitle();

// Random 선택
String role = TestDataGenerator.oneOf("USER", "ADMIN", "GUEST");
```

### 3. Test Builder

Fluent API를 사용한 테스트 객체 생성:

```java
import com.eraf.test.builder.TestBuilder;
import com.eraf.test.data.TestDataGenerator;

class UserTest {

    @Test
    void testUserCreation() {
        // Builder 패턴으로 객체 생성
        User user = TestBuilder.of(User::new)
            .with(User::setName, "John Doe")
            .with(User::setEmail, "john@example.com")
            .with(User::setAge, 30)
            .with(User::setActive, true)
            .build();

        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    void testUserList() {
        // 랜덤 데이터로 리스트 생성
        List<User> users = TestBuilder.of(User::new)
            .with(User::setName, TestDataGenerator.fullName())
            .with(User::setEmail, TestDataGenerator.email())
            .with(User::setAge, TestDataGenerator.randomInt(18, 65))
            .buildList(10);

        assertThat(users).hasSize(10);
    }

    @Test
    void testCustomBuilder() {
        User user = TestBuilder.of(User::new)
            .apply(u -> {
                u.setName(TestDataGenerator.fullName());
                u.setEmail(TestDataGenerator.email());
                u.setCreatedAt(Instant.now());
            })
            .build();
    }
}
```

### 4. Mock Helper

Mockito 래퍼 유틸리티:

```java
import com.eraf.test.mock.MockHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class UserServiceTest {

    private UserRepository userRepository = MockHelper.mockRepository(UserRepository.class);
    private UserService userService = new UserService(userRepository);

    @Test
    void testFindUserById() {
        // Mock 설정
        User user = new User(1L, "John Doe", "john@example.com");
        MockHelper.mockFindById(userRepository, 1L, user);

        // 테스트 실행
        Optional<User> found = userService.findById(1L);

        // 검증
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        MockHelper.verifyOnce(userRepository).findById(1L);
    }

    @Test
    void testFindAllUsers() {
        // Page Mock
        List<User> users = List.of(
            new User(1L, "John", "john@example.com"),
            new User(2L, "Jane", "jane@example.com")
        );
        Page<User> page = MockHelper.mockPage(users, PageRequest.of(0, 10));

        // Mock 설정
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        // 테스트 실행 및 검증
        Page<User> result = userService.findAll(PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void testSaveUser() {
        User user = new User("John", "john@example.com");
        MockHelper.mockSave(userRepository, user);

        User saved = userService.save(user);

        assertThat(saved).isNotNull();
        MockHelper.verifyOnce(userRepository).save(any(User.class));
    }

    @Test
    void testUserNotFound() {
        MockHelper.mockFindByIdEmpty(userRepository, 999L);

        Optional<User> found = userService.findById(999L);

        assertThat(found).isEmpty();
    }
}
```

#### Mock Helper 주요 메서드

```java
// Repository Mocks
MockHelper.mockRepository(UserRepository.class)
MockHelper.mockFindById(repo, id, entity)
MockHelper.mockFindByIdEmpty(repo, id)
MockHelper.mockSave(repo, entity)
MockHelper.mockExistsById(repo, id, true/false)
MockHelper.mockFindAll(repo, listOfEntities)

// Page Mocks
MockHelper.mockPage(content)
MockHelper.mockPage(content, pageable)
MockHelper.mockPage(content, pageable, totalElements)
MockHelper.emptyPage()
MockHelper.emptyPage(pageable)

// Verification
MockHelper.verifyOnce(mock)
MockHelper.verifyTimes(mock, n)
MockHelper.verifyNever(mock)
MockHelper.verifyNoMoreInteractions(mocks...)
MockHelper.reset(mocks...)
```

## 🎯 실전 예제

### Integration Test 전체 구성

```java
import com.eraf.test.containers.ErafPostgreSQLContainer;
import com.eraf.test.data.TestDataGenerator;
import com.eraf.test.builder.TestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class UserIntegrationTest {

    @Container
    static ErafPostgreSQLContainer postgres = new ErafPostgreSQLContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndFindUser() {
        // 테스트 데이터 생성
        User user = TestBuilder.of(User::new)
            .with(User::setName, TestDataGenerator.fullName())
            .with(User::setEmail, TestDataGenerator.email())
            .with(User::setAge, TestDataGenerator.randomInt(18, 65))
            .build();

        // 저장
        User saved = userRepository.save(user);

        // 조회 및 검증
        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo(user.getName());
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testBulkInsert() {
        // 대량 데이터 생성
        List<User> users = TestDataGenerator.list(100, () ->
            TestBuilder.of(User::new)
                .with(User::setName, TestDataGenerator.fullName())
                .with(User::setEmail, TestDataGenerator.email())
                .with(User::setAge, TestDataGenerator.randomInt(18, 65))
                .build()
        );

        // Bulk 저장
        userRepository.saveAll(users);

        // 검증
        long count = userRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(100);
    }
}
```

## 📚 지원 TestContainers

| Container | Class | Default Image |
|-----------|-------|---------------|
| PostgreSQL | `ErafPostgreSQLContainer` | `postgres:15-alpine` |
| Redis | `ErafRedisContainer` | `redis:7-alpine` |
| MySQL | `org.testcontainers.containers.MySQLContainer` | 직접 사용 |
| Kafka | `org.testcontainers.containers.KafkaContainer` | 직접 사용 |
| Elasticsearch | `org.testcontainers.containers.ElasticsearchContainer` | 직접 사용 |

## 🔧 Best Practices

### 1. Reusable Containers 사용

```java
// ❌ 나쁜 예 - 매 테스트마다 Container 생성 (느림)
@Container
static ErafPostgreSQLContainer postgres = new ErafPostgreSQLContainer();

// ✅ 좋은 예 - Reusable Container (빠름)
@Container
static ErafPostgreSQLContainer postgres = ErafPostgreSQLContainer.createReusable();
```

### 2. Base Test Class 패턴

```java
@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static ErafPostgreSQLContainer postgres = ErafPostgreSQLContainer.createReusable();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}

// 실제 테스트
class UserRepositoryTest extends BaseIntegrationTest {
    @Test
    void test() {
        // 테스트 코드
    }
}
```

### 3. Test Data 재사용

```java
public class UserTestData {
    public static User createDefaultUser() {
        return TestBuilder.of(User::new)
            .with(User::setName, "Test User")
            .with(User::setEmail, "test@example.com")
            .with(User::setAge, 30)
            .build();
    }

    public static User createRandomUser() {
        return TestBuilder.of(User::new)
            .with(User::setName, TestDataGenerator.fullName())
            .with(User::setEmail, TestDataGenerator.email())
            .with(User::setAge, TestDataGenerator.randomInt(18, 65))
            .build();
    }
}
```

## 🔗 관련 문서

- [TestContainers Documentation](https://www.testcontainers.org/)
- [DataFaker Documentation](https://www.datafaker.net/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

## ⚠️ 주의사항

1. **Docker 필요**: TestContainers는 Docker가 필수입니다
2. **성능**: Container 시작은 시간이 걸리므로 Reusable Container 사용 권장
3. **CI/CD**: GitHub Actions 등에서는 Docker-in-Docker 또는 서비스 컨테이너 설정 필요
4. **메모리**: 여러 Container 동시 실행 시 메모리 사용량 주의

## 📝 라이센스

이 모듈은 ERAF Commons의 일부입니다.
