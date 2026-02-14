# JPA Column Encryption (@Encrypt)

## 📋 개요

민감한 데이터를 데이터베이스에 자동으로 암호화하여 저장하고, 조회 시 자동으로 복호화하는 기능입니다.

**암호화 알고리즘**: AES-256-GCM (Galois/Counter Mode)

## 🔐 사용 방법

### 1. 암호화 키 설정

프로덕션 환경에서는 반드시 암호화 키를 설정해야 합니다.

**환경변수 설정 (권장)**:
```bash
export ERAF_ENCRYPTION_KEY="your-32-byte-base64-encoded-key"
```

**또는 시스템 속성 설정**:
```bash
java -Deraf.encryption.key="your-32-byte-base64-encoded-key" -jar app.jar
```

**키 생성 방법**:
```java
// eraf-core의 Crypto 클래스 사용
String key = Crypto.generateKey();
System.out.println("Generated Key: " + key);
```

### 2. Entity에 @Encrypt 어노테이션 적용

```java
import com.eraf.jpa.encryption.Encrypt;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Encrypt
    @Column(name = "social_security_number", length = 1000)
    private String socialSecurityNumber;  // 주민번호 - 자동 암호화

    @Encrypt
    @Column(name = "credit_card_number", length = 1000)
    private String creditCardNumber;  // 카드번호 - 자동 암호화

    @Encrypt
    @Column(name = "phone_number", length = 500)
    private String phoneNumber;  // 전화번호 - 자동 암호화

    // Getters and Setters
}
```

### 3. 일반적인 JPA 사용

```java
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void createUser() {
        User user = new User();
        user.setUsername("john_doe");
        user.setSocialSecurityNumber("123456-1234567");  // 평문 입력
        user.setCreditCardNumber("1234-5678-9012-3456");  // 평문 입력

        // 저장 시 자동 암호화됨
        userRepository.save(user);
    }

    public void readUser(Long id) {
        // 조회 시 자동 복호화됨
        User user = userRepository.findById(id).orElseThrow();
        String ssn = user.getSocialSecurityNumber();  // 평문으로 반환
        System.out.println("SSN: " + ssn);
    }
}
```

## ⚠️ 주의사항

### 1. 컬럼 길이 설정

암호화된 데이터는 원본보다 길이가 증가합니다 (약 3배).

```java
@Encrypt
@Column(length = 1000)  // ✅ 충분한 길이 설정
private String sensitiveData;

@Encrypt
@Column(length = 50)  // ❌ 너무 짧음 - 저장 실패 가능
private String creditCard;
```

**권장 길이**:
- 주민번호 (14자): `length = 500`
- 카드번호 (16-19자): `length = 500`
- 일반 텍스트 (100자 이하): `length = 500`
- 긴 텍스트 (100자 이상): `length = 1000` 이상
- CLOB/TEXT 타입 사용 가능

### 2. 검색 및 정렬 불가

암호화된 컬럼은 DB에서 직접 검색하거나 정렬할 수 없습니다.

```java
// ❌ 불가능 - 암호화된 값으로 검색 안됨
List<User> users = userRepository.findBySocialSecurityNumber("123456-1234567");

// ✅ 대안 - 애플리케이션 레벨에서 처리
List<User> allUsers = userRepository.findAll();
User target = allUsers.stream()
    .filter(u -> "123456-1234567".equals(u.getSocialSecurityNumber()))
    .findFirst()
    .orElse(null);
```

**해결책**:
- 해시 값을 별도 컬럼에 저장하여 검색
- 암호화가 필요 없는 검색 키는 별도 관리

```java
@Entity
public class User {
    @Encrypt
    @Column(length = 1000)
    private String socialSecurityNumber;

    @Column(name = "ssn_hash", length = 64)
    private String ssnHash;  // SHA-256 해시값 (검색용)

    public void setSocialSecurityNumber(String ssn) {
        this.socialSecurityNumber = ssn;
        this.ssnHash = DigestUtils.sha256Hex(ssn);  // 해시 저장
    }
}

// 해시로 검색
List<User> users = userRepository.findBySsnHash(DigestUtils.sha256Hex("123456-1234567"));
```

### 3. 암호화 키 관리

**CRITICAL**: 암호화 키가 변경되면 기존 데이터를 복호화할 수 없습니다!

- 키를 안전하게 보관하세요 (AWS Secrets Manager, Vault 등)
- 키 변경 시 데이터 재암호화 필요
- 백업 시 키도 함께 백업 (별도 안전한 장소)

### 4. 성능 고려사항

- 암복호화는 CPU 비용이 발생합니다
- 대량 데이터 조회 시 성능 영향 가능
- 필요한 컬럼만 선택적으로 암호화 적용

## 🔧 고급 설정

### 커스텀 암호화 키 설정

```java
// 환경변수 또는 시스템 속성으로 설정
export ERAF_ENCRYPTION_KEY="base64-encoded-key"

// 또는
java -Deraf.encryption.key="base64-encoded-key" -jar app.jar
```

### 로깅

DEBUG 레벨에서 암복호화 작업 로깅:

```yaml
# application.yml
logging:
  level:
    com.eraf.jpa.encryption: DEBUG
```

로그 예시:
```
[EncryptedStringConverter] Encrypted value (length: 14 → 192)
[EncryptedStringConverter] Decrypted value (length: 192 → 14)
```

## 🧪 테스트

### 단위 테스트

```java
@Test
void encryptionTest() {
    System.setProperty("eraf.encryption.key", "TEST_KEY");
    EncryptedStringConverter converter = new EncryptedStringConverter();

    String plain = "sensitive-data";
    String encrypted = converter.convertToDatabaseColumn(plain);
    String decrypted = converter.convertToEntityAttribute(encrypted);

    assertThat(encrypted).isNotEqualTo(plain);
    assertThat(decrypted).isEqualTo(plain);
}
```

### 통합 테스트

```java
@SpringBootTest
@Transactional
class UserEncryptionTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldEncryptAndDecryptSensitiveData() {
        // given
        User user = new User();
        user.setUsername("test");
        user.setSocialSecurityNumber("123456-1234567");

        // when
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // then
        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSocialSecurityNumber()).isEqualTo("123456-1234567");

        // DB에는 암호화된 값이 저장됨
        String encryptedValue = jdbcTemplate.queryForObject(
            "SELECT social_security_number FROM users WHERE id = ?",
            String.class,
            saved.getId()
        );
        assertThat(encryptedValue).isNotEqualTo("123456-1234567");
        assertThat(encryptedValue.length()).isGreaterThan(100);
    }
}
```

## 📚 관련 클래스

- `@Encrypt` - 민감 컬럼 표시 어노테이션
- `EncryptedStringConverter` - JPA AttributeConverter 구현체
- `com.eraf.core.crypto.Crypto` - AES-256-GCM 암호화 유틸리티

## 🔗 참고 자료

- [JPA AttributeConverter](https://docs.oracle.com/javaee/7/api/javax/persistence/AttributeConverter.html)
- [AES-GCM Mode](https://en.wikipedia.org/wiki/Galois/Counter_Mode)
- [개인정보보호법 - 암호화 조치](https://www.pipc.go.kr/)
