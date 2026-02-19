# ERAF Commons Refactoring Log

## 2026-02-15: Feature Flag 분리 작업

### 배경 및 목표

**프로젝트 목표:**
- Commons를 개발하여 어느 프로젝트에서든 필요한 모듈만 선택해서 사용 가능
- 소규모/대규모 사이트 모두 지원
- ERAF Generator를 통해 사용자가 선택한 모듈 조합으로 프로젝트 생성

**문제점:**
- `eraf-core`가 무거움 (32개 파일)
- Feature Flag 시스템(24개 파일)이 core에 포함되어 있음
- 모든 프로젝트에 Feature Flag가 필수는 아님

**목표:**
- `eraf-core`는 모든 프로젝트의 필수 기반만 포함
- Feature Flag는 선택 가능한 별도 모듈로 분리

---

## 변경 전 상태 (Before)

### eraf-core 구조 (32개 파일)

```
eraf-core/
├─ context/ (2개)
│  ├─ ErafContext.java
│  └─ ErafContextHolder.java
│
├─ logging/ (5개)
│  ├─ AuditLogger.java
│  ├─ AuditLogStore.java
│  ├─ NoOpAuditLogStore.java
│  ├─ StructuredLogger.java
│  └─ TraceContextHolder.java
│
├─ config/ (6개)
│  ├─ Config.java
│  ├─ Feature.java                           ⚠️ Feature Flag 관련
│  ├─ FeatureDisabledException.java          ⚠️ Feature Flag 관련
│  ├─ FeatureToggle.java                     ⚠️ Feature Flag 관련
│  ├─ FeatureToggleAspect.java               ⚠️ Feature Flag 관련
│  └─ VirtualThreadConfig.java
│
├─ config/feature/ (12개) - 전부 Feature Flag  ⚠️
│  ├─ FeatureFlagAdminController.java
│  ├─ FeatureFlagAdminService.java
│  ├─ FeatureFlagAutoConfiguration.java
│  ├─ FeatureFlagCacheManager.java
│  ├─ FeatureFlagDefaultLoader.java
│  ├─ FeatureFlagEntity.java
│  ├─ FeatureFlagMapper.java
│  ├─ FeatureFlagProperties.java
│  ├─ FeatureFlagRepository.java
│  ├─ FeatureFlagService.java
│  ├─ dto/ (3개)
│  │  ├─ FeatureFlagRequest.java
│  │  ├─ FeatureFlagResponse.java
│  │  └─ FeatureFlagStats.java
│  └─ evaluator/ (5개)
│     ├─ FeatureFlagEvaluator.java
│     ├─ PercentageEvaluator.java
│     ├─ SimpleEvaluator.java
│     ├─ TimeWindowEvaluator.java
│     └─ UserBasedEvaluator.java
│
└─ session/ (1개)
   └─ SessionUtils.java                      ⚠️ eraf-session으로 이동 고려
```

**Feature Flag 관련 파일: 24개**
- config/Feature.java
- config/FeatureDisabledException.java
- config/FeatureToggle.java
- config/FeatureToggleAspect.java
- config/feature/** (전체 디렉토리)

### eraf-core/pom.xml 의존성

```xml
<dependencies>
    <!-- ERAF Core 모듈들 -->
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-crypto</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-util</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-exception</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-resilience</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eraf</groupId>
        <artifactId>eraf-core-async</artifactId>
    </dependency>

    <!-- Feature Flag 전용 (분리 예정) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 변경 후 목표 (After)

### eraf-core 구조 (10개 파일)

```
eraf-core/
├─ context/ (2개)
│  ├─ ErafContext.java                       ✅ 유지
│  └─ ErafContextHolder.java                 ✅ 유지
│
├─ logging/ (5개)
│  ├─ AuditLogger.java                       ✅ 유지
│  ├─ AuditLogStore.java                     ✅ 유지
│  ├─ NoOpAuditLogStore.java                 ✅ 유지
│  ├─ StructuredLogger.java                  ✅ 유지
│  └─ TraceContextHolder.java                ✅ 유지
│
└─ config/ (2개)
   ├─ Config.java                            ✅ 유지
   └─ VirtualThreadConfig.java               ✅ 유지

+ session/SessionUtils.java는 eraf-session 모듈로 이동 검토
```

### eraf-feature-flag 구조 (신규 모듈, 24개 파일)

```
eraf-feature-flag/
├─ pom.xml                                   🆕 신규
├─ src/main/java/com/eraf/featureflag/
│  ├─ Feature.java                           📦 eraf-core에서 이동
│  ├─ FeatureDisabledException.java          📦 eraf-core에서 이동
│  ├─ FeatureToggle.java                     📦 eraf-core에서 이동
│  ├─ FeatureToggleAspect.java               📦 eraf-core에서 이동
│  ├─ FeatureFlagAutoConfiguration.java      📦 eraf-core에서 이동
│  ├─ FeatureFlagService.java                📦 eraf-core에서 이동
│  ├─ FeatureFlagAdminService.java           📦 eraf-core에서 이동
│  ├─ FeatureFlagAdminController.java        📦 eraf-core에서 이동
│  ├─ FeatureFlagCacheManager.java           📦 eraf-core에서 이동
│  ├─ FeatureFlagDefaultLoader.java          📦 eraf-core에서 이동
│  ├─ FeatureFlagEntity.java                 📦 eraf-core에서 이동
│  ├─ FeatureFlagMapper.java                 📦 eraf-core에서 이동
│  ├─ FeatureFlagProperties.java             📦 eraf-core에서 이동
│  ├─ FeatureFlagRepository.java             📦 eraf-core에서 이동
│  ├─ dto/
│  │  ├─ FeatureFlagRequest.java             📦 eraf-core에서 이동
│  │  ├─ FeatureFlagResponse.java            📦 eraf-core에서 이동
│  │  └─ FeatureFlagStats.java               📦 eraf-core에서 이동
│  └─ evaluator/
│     ├─ FeatureFlagEvaluator.java           📦 eraf-core에서 이동
│     ├─ PercentageEvaluator.java            📦 eraf-core에서 이동
│     ├─ SimpleEvaluator.java                📦 eraf-core에서 이동
│     ├─ TimeWindowEvaluator.java            📦 eraf-core에서 이동
│     └─ UserBasedEvaluator.java             📦 eraf-core에서 이동
└─ src/main/resources/
   └─ META-INF/spring/
      └─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## 마이그레이션 영향도

### Breaking Change

**기존 사용자:**
```xml
<!-- Before: Feature Flag 자동 포함 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**변경 후:**
```xml
<!-- After: Feature Flag 제거됨 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
    <version>1.1.0</version>  <!-- 버전 업그레이드 -->
</dependency>

<!-- Feature Flag 필요시 별도 추가 -->
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-feature-flag</artifactId>
    <version>1.1.0</version>
</dependency>
```

**영향받는 코드:**
- `@FeatureToggle` 어노테이션 사용하는 클래스
- `FeatureFlagService` 주입받는 클래스
- Feature Flag 관련 설정(`eraf.feature-flag.*`)

---

## 작업 체크리스트

- [ ] 1. eraf-feature-flag 모듈 생성
- [ ] 2. Feature Flag 관련 파일 24개 이동
- [ ] 3. 패키지명 변경 (com.eraf.core.config.feature → com.eraf.featureflag)
- [ ] 4. eraf-feature-flag/pom.xml 작성
- [ ] 5. AutoConfiguration 등록
- [ ] 6. eraf-core/pom.xml 정리 (JPA, Redis optional 의존성 제거)
- [ ] 7. 테스트 작성
- [ ] 8. README.md 작성
- [ ] 9. 마이그레이션 가이드 작성
- [ ] 10. 빌드 & 검증

---

## 다음 단계 (Phase 2 - 향후)

1. **ERAF Generator 개발**
   - 웹 UI (React/Vue)
   - 백엔드 API (Spring Boot)
   - 프로젝트 템플릿

2. **예제 프로젝트 작성**
   - example-rest-api
   - example-batch
   - example-microservice

3. **문서화**
   - 모듈 선택 가이드
   - Decision Tree
   - Compatibility Matrix

---

## 참고

- 작업 시작일: 2026-02-15
- 담당자: Claude + User
- 목표 완료일: TBD
