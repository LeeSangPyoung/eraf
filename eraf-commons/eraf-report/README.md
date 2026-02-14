# ERAF Report

리포트 생성 및 템플릿 엔진을 지원하는 모듈입니다.

## 기능

- **템플릿 기반 리포트**: Thymeleaf, FreeMarker 지원
- **다양한 출력 형식**: PDF, Excel, CSV, HTML
- **데이터 시각화**: 차트, 그래프 포함
- **동적 데이터**: DB, API 연동
- **스케줄링**: 주기적 리포트 생성
- **이메일 발송**: 생성된 리포트 자동 발송

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-report</artifactId>
</dependency>
```

## 사용법

### 1. HTML 리포트 생성

```java
import com.eraf.report.ReportGenerator;
import org.thymeleaf.context.Context;

@Service
public class ReportService {

    private final ReportGenerator reportGenerator;

    public String generateSalesReport(LocalDate startDate, LocalDate endDate) {
        // 데이터 조회
        List<Sale> sales = salesRepository.findByDateBetween(startDate, endDate);

        // 템플릿 컨텍스트 생성
        Context context = new Context();
        context.setVariable("startDate", startDate);
        context.setVariable("endDate", endDate);
        context.setVariable("sales", sales);
        context.setVariable("totalAmount", calculateTotal(sales));

        // HTML 리포트 생성
        return reportGenerator.generateHtml("sales-report", context);
    }
}
```

**템플릿 파일** (`templates/reports/sales-report.html`):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Sales Report</title>
    <style>
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        .summary { margin: 20px 0; font-size: 1.2em; }
    </style>
</head>
<body>
    <h1>Sales Report</h1>
    <div class="summary">
        <p>Period: <span th:text="${#temporals.format(startDate, 'yyyy-MM-dd')}"></span>
           ~ <span th:text="${#temporals.format(endDate, 'yyyy-MM-dd')}"></span></p>
        <p>Total Sales: <strong th:text="${totalAmount}"></strong> KRW</p>
    </div>

    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Product</th>
                <th>Quantity</th>
                <th>Amount</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="sale : ${sales}">
                <td th:text="${#temporals.format(sale.date, 'yyyy-MM-dd')}"></td>
                <td th:text="${sale.productName}"></td>
                <td th:text="${sale.quantity}"></td>
                <td th:text="${sale.amount}"></td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

### 2. PDF 리포트 생성

```java
import com.eraf.pdf.PdfGenerator;

public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate) throws IOException {
    // 1. HTML 리포트 생성
    String html = generateSalesReport(startDate, endDate);

    // 2. PDF로 변환
    return PdfGenerator.fromHtmlToBytes(html, "/path/to/font.ttf");
}

@GetMapping("/reports/sales/pdf")
public ResponseEntity<byte[]> downloadPdfReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
        throws IOException {

    byte[] pdf = generatePdfReport(startDate, endDate);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", "attachment; filename=sales-report.pdf")
        .body(pdf);
}
```

### 3. Excel 리포트 생성

```java
import com.eraf.excel.ExcelWriter;

public void generateExcelReport(LocalDate startDate, LocalDate endDate, Path outputPath)
        throws IOException {

    List<Sale> sales = salesRepository.findByDateBetween(startDate, endDate);

    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Sales Report");

        // 헤더
        writer.writeHeader(List.of("Date", "Product", "Quantity", "Amount"));

        // 데이터
        List<Map<String, Object>> data = sales.stream()
            .map(sale -> Map.of(
                "Date", sale.getDate(),
                "Product", sale.getProductName(),
                "Quantity", sale.getQuantity(),
                "Amount", sale.getAmount()
            ))
            .toList();

        writer.writeData(data);

        // 합계 행
        writer.writeRow(Map.of(
            "Product", "Total",
            "Amount", sales.stream().mapToLong(Sale::getAmount).sum()
        ));

        writer.write(outputPath);
    }
}
```

### 4. CSV 리포트 생성

```java
public String generateCsvReport(LocalDate startDate, LocalDate endDate) {
    List<Sale> sales = salesRepository.findByDateBetween(startDate, endDate);

    StringBuilder csv = new StringBuilder();
    csv.append("Date,Product,Quantity,Amount\n");

    for (Sale sale : sales) {
        csv.append(String.format("%s,%s,%d,%d\n",
            sale.getDate(),
            sale.getProductName(),
            sale.getQuantity(),
            sale.getAmount()
        ));
    }

    return csv.toString();
}
```

## 고급 기능

### 1. 차트 포함 리포트

```java
@Service
public class ChartReportService {

    public String generateChartReport(LocalDate startDate, LocalDate endDate) {
        List<MonthlySales> monthlySales = calculateMonthlySales(startDate, endDate);

        // Chart.js 데이터 생성
        List<String> labels = monthlySales.stream()
            .map(m -> m.getMonth().toString())
            .toList();

        List<Long> data = monthlySales.stream()
            .map(MonthlySales::getAmount)
            .toList();

        Context context = new Context();
        context.setVariable("labels", labels);
        context.setVariable("data", data);
        context.setVariable("monthlySales", monthlySales);

        return reportGenerator.generateHtml("chart-report", context);
    }
}
```

**템플릿 파일** (`templates/reports/chart-report.html`):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <h1>Monthly Sales Chart</h1>

    <canvas id="salesChart" width="400" height="200"></canvas>

    <script th:inline="javascript">
    const labels = /*[[${labels}]]*/ [];
    const data = /*[[${data}]]*/ [];

    new Chart(document.getElementById('salesChart'), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Monthly Sales',
                data: data,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
    </script>

    <table>
        <thead>
            <tr><th>Month</th><th>Amount</th></tr>
        </thead>
        <tbody>
            <tr th:each="item : ${monthlySales}">
                <td th:text="${item.month}"></td>
                <td th:text="${item.amount}"></td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

### 2. 스케줄링된 리포트

```java
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class ScheduledReportService {

    private final ReportService reportService;
    private final NotificationService notificationService;

    // 매일 오전 9시에 전일 리포트 생성 및 발송
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReport() throws IOException {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        byte[] pdf = reportService.generatePdfReport(yesterday, yesterday);

        EmailMessage email = EmailMessage.builder()
            .to("manager@example.com")
            .subject("Daily Sales Report - " + yesterday)
            .body("Attached is the daily sales report.")
            .attachment(new ByteArrayResource(pdf), "daily-report.pdf", "application/pdf")
            .build();

        notificationService.sendEmail(email);
    }

    // 매주 월요일 오전 9시에 주간 리포트
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReport() throws IOException {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);

        byte[] pdf = reportService.generatePdfReport(startDate, endDate);

        // 이메일 발송...
    }
}
```

### 3. 다국어 리포트

```java
@Service
public class I18nReportService {

    private final MessageSource messageSource;

    public String generateLocalizedReport(Locale locale) {
        Context context = new Context(locale);
        context.setVariable("data", getData());

        return reportGenerator.generateHtml("localized-report", context);
    }
}
```

**메시지 파일** (`messages_ko.properties`):

```properties
report.title=판매 리포트
report.date=날짜
report.product=제품명
report.quantity=수량
report.amount=금액
```

**템플릿**:

```html
<h1 th:text="#{report.title}"></h1>
<table>
    <thead>
        <tr>
            <th th:text="#{report.date}"></th>
            <th th:text="#{report.product}"></th>
            <th th:text="#{report.quantity}"></th>
            <th th:text="#{report.amount}"></th>
        </tr>
    </thead>
    <!-- ... -->
</table>
```

### 4. 동적 쿼리 리포트

```java
@Service
public class DynamicReportService {

    public String generateCustomReport(ReportRequest request) {
        // 동적 쿼리 생성
        String sql = buildDynamicQuery(request);
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

        Context context = new Context();
        context.setVariable("columns", request.getColumns());
        context.setVariable("data", data);
        context.setVariable("filters", request.getFilters());

        return reportGenerator.generateHtml("dynamic-report", context);
    }

    private String buildDynamicQuery(ReportRequest request) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", request.getColumns()));
        sql.append(" FROM ").append(request.getTable());

        if (!request.getFilters().isEmpty()) {
            sql.append(" WHERE ");
            sql.append(request.getFilters().stream()
                .map(f -> f.getField() + " " + f.getOperator() + " ?")
                .collect(Collectors.joining(" AND ")));
        }

        return sql.toString();
    }
}
```

### 5. 대용량 리포트 (스트리밍)

```java
@GetMapping("/reports/large/csv")
public void streamLargeReport(HttpServletResponse response) throws IOException {
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=large-report.csv");

    try (PrintWriter writer = response.getWriter()) {
        // 헤더
        writer.println("ID,Date,Product,Amount");

        // 대용량 데이터를 chunk 단위로 처리
        int pageSize = 1000;
        int page = 0;
        Page<Sale> salesPage;

        do {
            salesPage = salesRepository.findAll(PageRequest.of(page++, pageSize));

            for (Sale sale : salesPage.getContent()) {
                writer.printf("%d,%s,%s,%d%n",
                    sale.getId(),
                    sale.getDate(),
                    sale.getProductName(),
                    sale.getAmount()
                );
            }

            writer.flush();

        } while (salesPage.hasNext());
    }
}
```

## 실전 예제

### 재무 리포트

```java
@Service
public class FinancialReportService {

    public byte[] generateFinancialReport(int year, int quarter) throws IOException {
        // 데이터 수집
        QuarterlyFinance finance = calculateQuarterlyFinance(year, quarter);

        Context context = new Context();
        context.setVariable("year", year);
        context.setVariable("quarter", quarter);
        context.setVariable("revenue", finance.getRevenue());
        context.setVariable("expenses", finance.getExpenses());
        context.setVariable("profit", finance.getProfit());
        context.setVariable("profitMargin", finance.getProfitMargin());

        String html = reportGenerator.generateHtml("financial-report", context);
        return PdfGenerator.fromHtmlToBytes(html, "/path/to/font.ttf");
    }
}
```

### 재고 리포트

```java
@Service
public class InventoryReportService {

    public void generateInventoryReport(Path outputPath) throws IOException {
        List<InventoryItem> items = inventoryRepository.findAll();

        try (ExcelWriter writer = new ExcelWriter()) {
            // 전체 재고
            writer.createSheet("All Items");
            writer.writeHeader(List.of("SKU", "Name", "Quantity", "Location", "Last Updated"));
            writer.writeData(mapToData(items));

            // 재고 부족 품목
            writer.createSheet("Low Stock");
            List<InventoryItem> lowStock = items.stream()
                .filter(item -> item.getQuantity() < item.getMinimumStock())
                .toList();
            writer.writeHeader(List.of("SKU", "Name", "Current", "Minimum"));
            writer.writeData(mapToLowStockData(lowStock));

            // 과잉 재고 품목
            writer.createSheet("Overstock");
            List<InventoryItem> overstock = items.stream()
                .filter(item -> item.getQuantity() > item.getMaximumStock())
                .toList();
            writer.writeData(mapToOverstockData(overstock));

            writer.write(outputPath);
        }
    }
}
```

## 모범 사례

1. **템플릿 재사용**: 공통 레이아웃을 fragments로 분리
2. **데이터 최적화**: 필요한 데이터만 조회 (DTO 사용)
3. **스트리밍**: 대용량 리포트는 스트리밍 방식
4. **캐싱**: 동일 조건 리포트는 캐싱
5. **비동기 처리**: 무거운 리포트는 비동기로 생성
6. **스케줄링**: 주기적 리포트는 스케줄러 활용
7. **에러 처리**: 리포트 생성 실패 시 알림

## 참고

- [Thymeleaf](https://www.thymeleaf.org/)
- [FreeMarker](https://freemarker.apache.org/)
- [JasperReports](https://community.jaspersoft.com/)
- [Apache POI](https://poi.apache.org/)
