# ERAF Core 모듈 분할 계획

## 🎯 목표
- eraf-core (150개 파일, 23개 패키지)를 **6개 세부 모듈**로 분할
- 의존성 그래프 단순화
- 필요한 기능만 선택적으로 사용 가능
- 유지보수성 향상

---

## 📊 현재 구조 분석

### 패키지 목록 (23개)
```
async, code, config, context, converter, crypto, event, exception,
file, http, i18n, idempotent, lock, logging, masking, messaging,
resilience, response, sequence, session, template, utils, validation
```

### 의존성 현황
- **총 150개 Java 파일**
- **외부 의존성**: jackson, jasypt, okhttp, zip4j, bcrypt, jjwt 등

---

## 🎨 새로운 모듈 구조

### 1️⃣ **eraf-core-crypto** (암호화 & 보안)
```
📦 eraf-core-crypto
├── crypto/          (Crypto, Hash, Hmac, Signature, Password)
├── config/          (ConfigEncryptionUtil, ErafEncryptionProperties)
└── masking/         (Masking, MaskingAspect, @Masking)

의존성:
- jasypt-spring-boot-starter
- bcrypt
- 없음 (최상위 모듈)

파일 수: ~20개
```

### 2️⃣ **eraf-core-util** (유틸리티)
```
📦 eraf-core-util
├── utils/           (JsonUtils, StringUtils, DateUtils, CollectionUtils)
├── converter/       (BaseMapper, JsonConverter, XmlConverter)
├── file/            (FileUtils, ZipUtils, ExcelUtils)
└── template/        (TemplateEngine, FreemarkerTemplate)

의존성:
- jackson
- apache-commons-lang3
- zip4j

파일 수: ~30개
```

### 3️⃣ **eraf-core-validation** (검증)
```
📦 eraf-core-validation
└── validation/      (@Email, @NoXss, @BusinessNo, @Phone, ValidationUtils)

의존성:
- jakarta.validation-api
- eraf-core-util (StringUtils)

파일 수: ~15개
```

### 4️⃣ **eraf-core-exception** (예외 처리)
```
📦 eraf-core-exception
├── exception/       (BusinessException, ValidationException, SystemException)
├── response/        (ApiResponse, ErrorCode, GlobalExceptionHandler)
└── logging/         (RequestLoggingFilter, ExceptionLogger)

의존성:
- spring-web
- eraf-core-util (JsonUtils)

파일 수: ~20개
```

### 5️⃣ **eraf-core-resilience** (복원력 패턴)
```
📦 eraf-core-resilience
├── resilience/      (CircuitBreaker, RateLimit, Retry, Timeout, Bulkhead)
├── lock/            (DistributedLock, RedisLock, LockAspect)
└── idempotent/      (IdempotentAspect, @Idempotent)

의존성:
- spring-aop
- eraf-core-exception

파일 수: ~25개
```

### 6️⃣ **eraf-core** (핵심 기능)
```
📦 eraf-core
├── async/           (AsyncExecutor, @Async)
├── code/            (CommonCode, CodeRegistry)
├── context/         (RequestContext, UserContext)
├── event/           (ApplicationEvent, EventPublisher)
├── http/            (HttpClient, RestTemplate)
├── i18n/            (MessageSource, LocaleResolver)
├── messaging/       (MessagePublisher, MessageConsumer)
├── sequence/        (SequenceGenerator, SnowflakeId)
└── session/         (SessionManager, SessionStore)

의존성:
- eraf-core-util
- eraf-core-exception
- eraf-core-crypto
- spring-context

파일 수: ~40개
```

---

## 🔄 모듈 간 의존성 그래프

```
                    eraf-core-crypto (독립)
                           ↑
                           |
    eraf-core-util  ←──────┤
         ↑                 |
         |                 |
         ├─────────────────┤
         |                 |
    eraf-core-validation   |
         ↑                 |
         |                 |
    eraf-core-exception ←──┤
         ↑                 |
         |                 |
    eraf-core-resilience   |
         ↑                 |
         |                 |
    eraf-core ←────────────┘
```

**의존성 레벨:**
- Level 0: `eraf-core-crypto` (독립)
- Level 1: `eraf-core-util`
- Level 2: `eraf-core-validation`, `eraf-core-exception`
- Level 3: `eraf-core-resilience`
- Level 4: `eraf-core` (통합)

---

## 📝 마이그레이션 단계

### Phase 1: 모듈 생성 (1주)
- [ ] 6개 신규 모듈 POM 생성
- [ ] parent POM에 모듈 등록
- [ ] 의존성 정의

### Phase 2: 코드 이동 (2주)
- [ ] **Week 1**: crypto, util, validation 모듈 분리
- [ ] **Week 2**: exception, resilience 모듈 분리

### Phase 3: 테스트 & 검증 (1주)
- [ ] 각 모듈별 테스트 실행
- [ ] 의존성 순환 참조 확인
- [ ] 통합 테스트

### Phase 4: 문서화 & 배포 (1주)
- [ ] 각 모듈 README 작성
- [ ] 마이그레이션 가이드
- [ ] 버전 1.1.0 릴리스

---

## 🚀 Quick Start: 첫 번째 모듈 분리

### Step 1: eraf-core-crypto 생성

```bash
# 1. 디렉토리 생성
cd eraf-commons
mkdir -p eraf-core-crypto/src/{main,test}/java/com/eraf/crypto

# 2. POM 파일 생성
cat > eraf-core-crypto/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-commons</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>eraf-core-crypto</artifactId>
    <name>ERAF Core - Cryptography</name>
    <description>암호화, 해시, 서명 등 보안 관련 기능</description>

    <dependencies>
        <!-- Jasypt -->
        <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
        </dependency>

        <!-- BCrypt -->
        <dependency>
            <groupId>at.favre.lib</groupId>
            <artifactId>bcrypt</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
EOF

# 3. 코드 이동
mv eraf-core/src/main/java/com/eraf/core/crypto/* \
   eraf-core-crypto/src/main/java/com/eraf/crypto/

mv eraf-core/src/main/java/com/eraf/core/config/ErafEncryption* \
   eraf-core-crypto/src/main/java/com/eraf/crypto/config/

mv eraf-core/src/main/java/com/eraf/core/masking/* \
   eraf-core-crypto/src/main/java/com/eraf/crypto/masking/

# 4. 테스트 이동
mv eraf-core/src/test/java/com/eraf/core/crypto/* \
   eraf-core-crypto/src/test/java/com/eraf/crypto/

# 5. 컴파일 & 테스트
cd eraf-core-crypto
mvn clean test
```

### Step 2: parent POM 업데이트

```xml
<!-- eraf-commons/pom.xml에 추가 -->
<modules>
    <!-- 기존 모듈들 -->
    <module>eraf-core</module>

    <!-- 새 모듈 추가 -->
    <module>eraf-core-crypto</module>
    <module>eraf-core-util</module>
    <module>eraf-core-validation</module>
    <module>eraf-core-exception</module>
    <module>eraf-core-resilience</module>
</modules>
```

### Step 3: 다른 모듈에서 참조 업데이트

```xml
<!-- 기존: eraf-core 의존성 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
</dependency>

<!-- 변경: 필요한 모듈만 선택 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core-crypto</artifactId>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core-util</artifactId>
</dependency>
```

---

## ✅ 체크리스트

### Before Migration
- [x] 현재 구조 분석 완료
- [x] 모듈 분할 계획 수립
- [x] 의존성 그래프 작성
- [ ] 팀 리뷰 및 승인

### During Migration
- [ ] eraf-core-crypto 생성 및 테스트
- [ ] eraf-core-util 생성 및 테스트
- [ ] eraf-core-validation 생성 및 테스트
- [ ] eraf-core-exception 생성 및 테스트
- [ ] eraf-core-resilience 생성 및 테스트
- [ ] eraf-core 정리 (불필요한 코드 제거)

### After Migration
- [ ] 전체 빌드 성공 확인
- [ ] 모든 테스트 통과 확인
- [ ] 의존성 순환 참조 없음 확인
- [ ] 문서 업데이트
- [ ] 버전 태깅 및 릴리스

---

## 📈 기대 효과

| 항목 | 현재 | 분할 후 | 개선 |
|------|------|---------|------|
| **모듈 크기** | 150 파일 | 평균 20-30 파일 | **-80%** |
| **빌드 시간** | ~30초 | ~5초/모듈 | **-80%** |
| **의존성 복잡도** | 높음 | 낮음 | **-60%** |
| **재사용성** | 중간 | 높음 | **+100%** |
| **유지보수성** | 어려움 | 쉬움 | **+100%** |

---

## 🎯 우선순위

### 즉시 (이번 스프린트)
1. **eraf-core-crypto** - 가장 독립적, 의존성 없음
2. **eraf-core-util** - 많은 모듈에서 사용, 우선 분리 필요

### 다음 스프린트
3. **eraf-core-exception** - util 이후 분리
4. **eraf-core-validation** - exception과 함께 분리

### 이후
5. **eraf-core-resilience** - 복잡도 높음, 충분한 테스트 필요
6. **eraf-core** 정리 - 마지막 단계

---

## 📞 지원 및 문의

- 담당자: ERAF Team
- 일정: 5주 (Phase 1-4)
- 리스크: 낮음 (단계별 진행, 철저한 테스트)

---

*Last Updated: 2026-02-11*
*Version: 1.0*
