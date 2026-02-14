# ERAF Core - System

시스템 공통 기능 (공통코드, 시퀀스 등)을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 공통코드 관리
- **CodeRepository**: 공통코드 저장소 인터페이스
- **CodeService**: 공통코드 서비스
- **@Code**: 공통코드 검증 어노테이션

### 2. 시퀀스 생성
- **SequenceGenerator**: 시퀀스 번호 생성
- **@Sequence**: 자동 시퀀스 주입 (AOP)
- **@AutoSequence**: 메서드 파라미터 자동 시퀀스

## 🔗 의존성

**외부 라이브러리**:
- Spring Boot Validation
- Spring Boot AOP
- Spring Data Commons (optional)

## 📝 사용 예시

### 공통코드 정의
```java
public class CommonCodes {
    public static final String USER_STATUS = "USER_STATUS";  // 사용자 상태
    public static final String ORDER_STATUS = "ORDER_STATUS"; // 주문 상태
    public static final String PAYMENT_METHOD = "PAYMENT_METHOD"; // 결제 수단
}
```

### 공통코드 조회
```java
@Service
public class UserService {

    @Autowired
    private CodeService codeService;

    public List<CodeItem> getUserStatusCodes() {
        return codeService.getCodeItems("USER_STATUS");
        // [
        //   {code: "ACTIVE", name: "활성"},
        //   {code: "INACTIVE", name: "비활성"},
        //   {code: "SUSPENDED", name: "정지"}
        // ]
    }

    public String getCodeName(String codeGroup, String code) {
        return codeService.getCodeName(codeGroup, code);
        // "ACTIVE" -> "활성"
    }
}
```

### 공통코드 검증
```java
public class UserUpdateRequest {

    @Code(group = "USER_STATUS", message = "유효하지 않은 사용자 상태입니다")
    private String status; // ACTIVE, INACTIVE, SUSPENDED만 허용
}
```

### 시퀀스 생성
```java
@Service
public class OrderService {

    @Autowired
    private SequenceGenerator sequenceGenerator;

    public Order createOrder(OrderRequest request) {
        // 주문번호 생성: ORD20240101001, ORD20240101002, ...
        String orderNo = sequenceGenerator.generate(
            "ORDER",
            Reset.DAILY,
            "ORD{yyyyMMdd}{###}"
        );

        Order order = new Order(orderNo, request);
        return orderRepository.save(order);
    }
}
```

### 자동 시퀀스 주입 (AOP)
```java
public class Invoice {

    @Sequence(
        name = "INVOICE",
        reset = Reset.MONTHLY,
        pattern = "INV-{yyyyMM}-{#####}"
    )
    private String invoiceNo; // INV-202401-00001
}

@Service
public class InvoiceService {

    public Invoice createInvoice(InvoiceRequest request) {
        Invoice invoice = new Invoice(request);
        // invoiceNo가 자동으로 생성됨
        return invoiceRepository.save(invoice);
    }
}
```

### 메서드 파라미터 시퀀스
```java
@Service
public class DocumentService {

    @AutoSequence(
        name = "DOCUMENT",
        reset = Reset.YEARLY,
        pattern = "DOC-{yyyy}-{######}"
    )
    public Document createDocument(String docNo, DocumentRequest request) {
        // docNo에 자동으로 시퀀스가 주입됨: DOC-2024-000001
        Document doc = new Document(docNo, request);
        return documentRepository.save(doc);
    }
}
```

### 시퀀스 리셋 정책
```java
public enum Reset {
    NONE,    // 리셋 안 함 (계속 증가)
    DAILY,   // 매일 00:00 리셋
    MONTHLY, // 매월 1일 00:00 리셋
    YEARLY   // 매년 1월 1일 00:00 리셋
}
```

### 커스텀 시퀀스 패턴
```java
// 년도 + 월 + 일련번호
sequenceGenerator.generate("ORDER", Reset.DAILY, "ORD-{yyyy}{MM}{dd}-{####}");
// -> ORD-20240115-0001

// 지점코드 + 일련번호
sequenceGenerator.generate("RECEIPT", Reset.NONE, "RCP-{branchCode}-{#####}");
// -> RCP-SEL-00001

// 타임스탬프 + 랜덤
sequenceGenerator.generate("SESSION", Reset.NONE, "SES-{yyyyMMddHHmmss}-{random:8}");
// -> SES-20240115143025-A3K9M2X7
```

## 🏗️ 주요 클래스

**공통코드**:
- `CodeRepository` - 공통코드 저장소 인터페이스
- `CodeService` - 공통코드 조회 서비스
- `@Code` - 공통코드 검증 어노테이션
- `CodeItem` - 공통코드 항목 (code, name, order)

**시퀀스**:
- `SequenceGenerator` - 시퀀스 생성기
- `@Sequence` - 필드 자동 시퀀스
- `@AutoSequence` - 파라미터 자동 시퀀스
- `SequenceAspect` - AOP 처리
- `Reset` - 리셋 정책 (NONE, DAILY, MONTHLY, YEARLY)

## 📚 공통코드 구조

### DB 테이블 예시
```sql
CREATE TABLE common_codes (
    code_group VARCHAR(50),
    code VARCHAR(50),
    name VARCHAR(100),
    description VARCHAR(255),
    sort_order INT,
    use_yn CHAR(1),
    PRIMARY KEY (code_group, code)
);

INSERT INTO common_codes VALUES
('USER_STATUS', 'ACTIVE', '활성', '정상 사용 중인 사용자', 1, 'Y'),
('USER_STATUS', 'INACTIVE', '비활성', '휴면 상태 사용자', 2, 'Y'),
('USER_STATUS', 'SUSPENDED', '정지', '이용 정지된 사용자', 3, 'Y');
```

### JPA 구현 예시
```java
@Repository
public class JpaCodeRepository implements CodeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<CodeItem> findByGroup(String codeGroup) {
        return em.createQuery(
            "SELECT new CodeItem(c.code, c.name, c.sortOrder) " +
            "FROM CommonCode c " +
            "WHERE c.codeGroup = :group AND c.useYn = 'Y' " +
            "ORDER BY c.sortOrder",
            CodeItem.class
        )
        .setParameter("group", codeGroup)
        .getResultList();
    }
}
```

## ⚠️ 주의사항

- 공통코드는 애플리케이션 시작 시 캐싱 권장
- 시퀀스 생성은 동시성 제어 필요 (Redis/DB Lock)
- 대용량 시퀀스는 성능 고려 필요
