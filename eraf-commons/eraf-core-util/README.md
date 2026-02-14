# ERAF Core - Utilities

유틸리티, 변환기, 파일 처리 등 범용 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. JSON/XML 처리
- **JsonUtils**: JSON 직렬화/역직렬화
- **JsonConverter**: 객체 ↔ JSON 변환
- **XmlConverter**: 객체 ↔ XML 변환

### 2. 문자열 유틸리티
- **StringUtils**: 문자열 조작 (camelCase, snake_case, 등)
- **RegexUtils**: 정규식 패턴 검증

### 3. 날짜/시간
- **DateUtils**: 날짜 포맷, 변환, 계산
- **TimeUtils**: 시간 관련 유틸리티

### 4. 컬렉션
- **CollectionUtils**: 리스트, 맵, 셋 유틸리티
- **StreamUtils**: Java Stream API 헬퍼

### 5. 파일 처리
- **FileUtils**: 파일 읽기/쓰기/복사
- **ZipUtils**: ZIP 압축/해제
- **ExcelUtils**: Excel 파일 처리
- **CsvUtils**: CSV 파일 처리

### 6. 기타
- **IpUtils**: IP 주소 유틸리티
- **RandomUtils**: 랜덤 문자열/숫자 생성
- **BaseMapper**: 엔티티 ↔ DTO 매퍼 인터페이스

## 🔗 의존성

이 모듈은 **독립적**이며 다른 ERAF 모듈에 의존하지 않습니다.

**외부 라이브러리**:
- Jackson (JSON/XML)
- Apache Commons Lang3
- Zip4j
- Spring Web (optional)

## 📝 사용 예시

### JSON 처리
```java
// 객체 → JSON
String json = JsonUtils.toJson(user);

// JSON → 객체
User user = JsonUtils.fromJson(json, User.class);

// Pretty print
String prettyJson = JsonUtils.toPrettyJson(user);
```

### 파일 처리
```java
// 파일 읽기
String content = FileUtils.readFileToString("/path/to/file.txt");

// 파일 쓰기
FileUtils.writeStringToFile("/path/to/file.txt", content);

// ZIP 압축
ZipUtils.zip(sourceDir, outputZipFile);

// ZIP 해제
ZipUtils.unzip(zipFile, destDir);
```

### 문자열 변환
```java
// camelCase → snake_case
String snake = StringUtils.toSnakeCase("userName"); // user_name

// snake_case → camelCase
String camel = StringUtils.toCamelCase("user_name"); // userName
```

### 날짜 처리
```java
// 날짜 포맷
String formatted = DateUtils.format(date, "yyyy-MM-dd");

// 날짜 파싱
Date date = DateUtils.parse("2024-01-01", "yyyy-MM-dd");

// 날짜 계산
Date tomorrow = DateUtils.addDays(date, 1);
```

## 🏗️ 주요 클래스

**JSON/XML**:
- `JsonUtils`, `JsonConverter`
- `XmlConverter`

**문자열**:
- `StringUtils`, `RegexUtils`

**날짜/시간**:
- `DateUtils`, `TimeUtils`

**컬렉션**:
- `CollectionUtils`, `StreamUtils`

**파일**:
- `FileUtils`, `ZipUtils`, `ExcelUtils`, `CsvUtils`

**기타**:
- `IpUtils`, `RandomUtils`, `BaseMapper`
