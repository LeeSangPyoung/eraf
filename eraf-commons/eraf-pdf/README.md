# ERAF PDF

Flying Saucer (iText) 기반 PDF 생성, 병합, 분할을 지원하는 모듈입니다.

## 기능

- **HTML to PDF**: HTML/CSS를 PDF로 변환
- **PDF 생성**: 프로그래밍 방식으로 PDF 생성
- **PDF 병합**: 여러 PDF 파일 병합
- **PDF 분할**: PDF 페이지 분할
- **텍스트 추출**: PDF에서 텍스트 추출
- **한글 폰트**: 한글 폰트 지원

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-pdf</artifactId>
</dependency>
```

## 사용법

### 1. HTML을 PDF로 변환

```java
import com.eraf.pdf.PdfGenerator;
import java.nio.file.Path;

public void generatePdf() throws IOException {
    String html = """
        <html>
        <head>
            <style>
                body { font-family: 'Malgun Gothic', sans-serif; }
                h1 { color: #333; }
                table { border-collapse: collapse; width: 100%; }
                td, th { border: 1px solid #ddd; padding: 8px; }
            </style>
        </head>
        <body>
            <h1>사용자 목록</h1>
            <table>
                <tr><th>ID</th><th>이름</th><th>이메일</th></tr>
                <tr><td>1</td><td>홍길동</td><td>hong@example.com</td></tr>
            </table>
        </body>
        </html>
        """;

    PdfGenerator.fromHtml(html, Path.of("users.pdf"));
}
```

### 2. 한글 폰트 지정

```java
public void generateKoreanPdf() throws IOException {
    String html = "<html><body><h1>한글 제목</h1><p>한글 내용</p></body></html>";
    String fontPath = "/path/to/NanumGothic.ttf";

    PdfGenerator.fromHtml(html, Path.of("korean.pdf"), fontPath);
}
```

### 3. 바이트 배열로 생성 (HTTP Response)

```java
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletResponse;

@GetMapping("/download/invoice")
public void downloadInvoice(HttpServletResponse response) throws IOException {
    String html = generateInvoiceHtml();

    byte[] pdfBytes = PdfGenerator.fromHtmlToBytes(html, "/path/to/font.ttf");

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
    response.getOutputStream().write(pdfBytes);
}
```

### 4. PDF 병합

```java
import com.eraf.pdf.PdfMerger;

public void mergePdfs() throws IOException {
    List<Path> pdfFiles = List.of(
        Path.of("document1.pdf"),
        Path.of("document2.pdf"),
        Path.of("document3.pdf")
    );

    PdfMerger.merge(pdfFiles, Path.of("merged.pdf"));
}
```

### 5. PDF 분할

```java
import com.eraf.pdf.PdfSplitter;

public void splitPdf() throws IOException {
    Path sourcePdf = Path.of("large_document.pdf");
    Path outputDir = Path.of("split_pages");

    // 모든 페이지를 개별 파일로 분할
    PdfSplitter.split(sourcePdf, outputDir);

    // 특정 페이지만 추출
    PdfSplitter.extractPages(sourcePdf, Path.of("pages_1_5.pdf"), 1, 5);
}
```

### 6. 텍스트 추출

```java
import com.eraf.pdf.PdfTextUtils;

public void extractText() throws IOException {
    Path pdfPath = Path.of("document.pdf");

    // 전체 텍스트 추출
    String fullText = PdfTextUtils.extractText(pdfPath);
    System.out.println(fullText);

    // 특정 페이지 텍스트 추출
    String pageText = PdfTextUtils.extractTextFromPage(pdfPath, 1);
    System.out.println(pageText);
}
```

## 고급 기능

### 1. Thymeleaf 템플릿 사용

```java
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class InvoiceService {

    private final TemplateEngine templateEngine;

    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        // 1. Thymeleaf로 HTML 생성
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("items", invoice.getItems());

        String html = templateEngine.process("invoice", context);

        // 2. PDF로 변환
        return PdfGenerator.fromHtmlToBytes(html, "/path/to/font.ttf");
    }
}
```

**템플릿 파일** (`templates/invoice.html`):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <style>
        body { font-family: 'Malgun Gothic', sans-serif; }
        .header { text-align: center; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    </style>
</head>
<body>
    <div class="header">
        <h1>거래명세서</h1>
        <p>발행일: <span th:text="${invoice.date}"></span></p>
    </div>

    <table>
        <thead>
            <tr>
                <th>품목</th>
                <th>수량</th>
                <th>단가</th>
                <th>금액</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="item : ${items}">
                <td th:text="${item.name}"></td>
                <td th:text="${item.quantity}"></td>
                <td th:text="${item.price}"></td>
                <td th:text="${item.total}"></td>
            </tr>
        </tbody>
    </table>

    <div class="total">
        <h3>합계: <span th:text="${invoice.total}"></span>원</h3>
    </div>
</body>
</html>
```

### 2. 워터마크 추가

```java
import com.lowagie.text.pdf.*;

public void addWatermark(Path sourcePdf, Path outputPdf, String watermarkText) throws IOException {
    try (PdfReader reader = new PdfReader(sourcePdf.toString());
         PdfStamper stamper = new PdfStamper(reader, Files.newOutputStream(outputPdf))) {

        int numPages = reader.getNumberOfPages();
        for (int i = 1; i <= numPages; i++) {
            PdfContentByte content = stamper.getOverContent(i);
            content.beginText();
            content.setFontAndSize(BaseFont.createFont(), 60);
            content.setColorFill(BaseColor.LIGHT_GRAY);
            content.showTextAligned(Element.ALIGN_CENTER, watermarkText, 300, 400, 45);
            content.endText();
        }
    }
}
```

### 3. PDF 암호화

```java
import com.lowagie.text.pdf.PdfWriter;

public void encryptPdf(Path sourcePdf, Path encryptedPdf, String userPassword, String ownerPassword)
        throws IOException {
    try (PdfReader reader = new PdfReader(sourcePdf.toString());
         PdfStamper stamper = new PdfStamper(reader, Files.newOutputStream(encryptedPdf))) {

        stamper.setEncryption(
            userPassword.getBytes(),
            ownerPassword.getBytes(),
            PdfWriter.ALLOW_PRINTING,
            PdfWriter.ENCRYPTION_AES_256
        );
    }
}
```

### 4. PDF 메타데이터 설정

```java
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

public void setPdfMetadata() throws IOException {
    Document document = new Document();
    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("metadata.pdf"));

    document.addTitle("Document Title");
    document.addAuthor("Author Name");
    document.addSubject("Subject");
    document.addKeywords("keyword1, keyword2");
    document.addCreator("ERAF PDF Generator");

    document.open();
    // 내용 추가
    document.close();
}
```

### 5. 페이지 번호 추가

```java
public void addPageNumbers(Path sourcePdf, Path outputPdf) throws IOException {
    try (PdfReader reader = new PdfReader(sourcePdf.toString());
         PdfStamper stamper = new PdfStamper(reader, Files.newOutputStream(outputPdf))) {

        int numPages = reader.getNumberOfPages();
        for (int i = 1; i <= numPages; i++) {
            PdfContentByte content = stamper.getOverContent(i);
            content.beginText();
            content.setFontAndSize(BaseFont.createFont(), 10);
            content.showTextAligned(Element.ALIGN_RIGHT,
                "Page " + i + " of " + numPages, 550, 30, 0);
            content.endText();
        }
    }
}
```

### 6. 이미지 추가

```java
import com.lowagie.text.Image;

public void addImage(String html, Path imagePath) throws IOException {
    String htmlWithImage = html +
        "<img src='" + imagePath.toUri() + "' width='400' />";

    PdfGenerator.fromHtml(htmlWithImage, Path.of("with_image.pdf"));
}
```

## CSS 지원

### 페이지 설정

```html
<style>
    @page {
        size: A4;
        margin: 2cm;
    }

    @page :first {
        margin-top: 3cm;
    }

    body {
        font-size: 12pt;
    }

    h1 {
        page-break-before: always;
    }

    .no-break {
        page-break-inside: avoid;
    }
</style>
```

### 페이지 나누기

```html
<div class="section">
    <h1>Section 1</h1>
    <p>Content...</p>
</div>

<div style="page-break-after: always;"></div>

<div class="section">
    <h1>Section 2</h1>
    <p>Content...</p>
</div>
```

## 실전 예제

### 계약서 생성

```java
@Service
public class ContractService {

    public byte[] generateContract(Contract contract) throws IOException {
        String html = """
            <html>
            <head>
                <style>
                    @page { size: A4; margin: 2cm; }
                    body { font-family: 'Malgun Gothic'; font-size: 12pt; }
                    .title { text-align: center; font-size: 18pt; font-weight: bold; }
                    .clause { margin: 20px 0; }
                    .signature { margin-top: 50px; }
                </style>
            </head>
            <body>
                <div class="title">계약서</div>

                <div class="clause">
                    <strong>제1조 (목적)</strong><br/>
                    본 계약은...
                </div>

                <div class="signature">
                    <table width="100%">
                        <tr>
                            <td width="50%">
                                갑: %s<br/>
                                서명: ______________
                            </td>
                            <td width="50%">
                                을: %s<br/>
                                서명: ______________
                            </td>
                        </tr>
                    </table>
                </div>
            </body>
            </html>
            """.formatted(contract.getPartyA(), contract.getPartyB());

        return PdfGenerator.fromHtmlToBytes(html, "/path/to/font.ttf");
    }
}
```

## 에러 처리

```java
public byte[] generatePdfSafely(String html) {
    try {
        return PdfGenerator.fromHtmlToBytes(html, null);
    } catch (IOException e) {
        log.error("PDF generation failed", e);
        throw new BusinessException("PDF 생성에 실패했습니다", e);
    }
}
```

## 모범 사례

1. **폰트 경로**: 한글 사용 시 반드시 폰트 지정
2. **CSS 사용**: 인라인 스타일보다 `<style>` 태그 권장
3. **페이지 크기**: @page로 명확히 지정
4. **이미지**: 절대 경로 또는 Base64 인코딩 사용
5. **메모리**: 대용량 PDF는 스트리밍 방식 고려
6. **검증**: HTML 유효성 검증 후 PDF 생성
7. **캐싱**: 동일 PDF는 캐싱하여 재사용

## 참고

- [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer)
- [OpenPDF](https://github.com/LibrePDF/OpenPDF)
- [CSS for PDF](https://www.w3.org/TR/css-page-3/)
