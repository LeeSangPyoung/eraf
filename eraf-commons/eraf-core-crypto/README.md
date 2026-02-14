# ERAF Core - Cryptography

암호화, 해시, 서명 등 보안 관련 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 암호화 (Encryption)
- **Crypto**: AES256 양방향 암호화
- **ConfigEncryptionUtil**: 설정 파일 암호화 (Jasypt 기반)
- **ErafEncryptionAutoConfiguration**: 자동 설정

### 2. 해시 (Hashing)
- **Hash**: SHA-256, MD5 해시 생성
- **PasswordEncoder**: BCrypt 기반 비밀번호 해싱

### 3. JWT (JSON Web Token)
- **JwtTokenProvider**: JWT 토큰 생성/검증
- **JwtProperties**: JWT 설정 관리

### 4. 서명 (Signature)
- **Signature**: HMAC, RSA 디지털 서명

## 🔗 의존성

이 모듈은 **독립적**이며 다른 ERAF 모듈에 의존하지 않습니다.

**외부 라이브러리**:
- Jasypt Spring Boot Starter 3.0.5
- BCrypt (at.favre.lib)
- JJWT (io.jsonwebtoken)

## 📝 사용 예시

### AES256 암호화/복호화
```java
String encrypted = Crypto.encrypt("hello", "mySecretKey");
String decrypted = Crypto.decrypt(encrypted, "mySecretKey");
```

### BCrypt 비밀번호 해싱
```java
String hashed = PasswordEncoder.encode("password123");
boolean matches = PasswordEncoder.matches("password123", hashed);
```

### JWT 토큰 생성
```java
@Autowired
private JwtTokenProvider jwtTokenProvider;

String token = jwtTokenProvider.createToken(userId, roles);
Claims claims = jwtTokenProvider.parseToken(token);
```

### 설정 파일 암호화
```yaml
# application.yml
eraf:
  encryption:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}

database:
  password: ENC(EncryptedPasswordHere)
```

## 🏗️ 주요 클래스

- `Crypto` - AES256 암호화/복호화
- `Hash` - SHA-256, MD5 해시
- `PasswordEncoder` - BCrypt 비밀번호 인코더
- `JwtTokenProvider` - JWT 토큰 생성/검증
- `Signature` - HMAC/RSA 디지털 서명
- `ConfigEncryptionUtil` - Jasypt 기반 설정 암호화

## 📚 참고

- Jasypt: Java Simplified Encryption
- BCrypt: Adaptive hash function
- JJWT: Java JWT library
