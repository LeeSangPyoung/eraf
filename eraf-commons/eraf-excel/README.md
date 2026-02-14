# ERAF Excel

Apache POI 기반 Excel 파일 읽기/쓰기를 지원하는 모듈입니다.

## 기능

- **Excel 쓰기**: .xlsx 파일 생성
- **Excel 읽기**: .xls/.xlsx 파일 파싱
- **스트리밍**: 대용량 파일 메모리 효율적 처리
- **스타일링**: 헤더, 데이터, 날짜 스타일 자동 적용
- **자동 폭 조정**: 컬럼 너비 자동 계산

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-excel</artifactId>
</dependency>
```

## 사용법

### 1. Excel 파일 생성

```java
import com.eraf.excel.ExcelWriter;
import java.util.List;
import java.util.Map;

public void createExcel() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        // 헤더 생성
        List<String> headers = List.of("ID", "이름", "이메일", "가입일");

        // 데이터 생성
        List<Map<String, Object>> data = List.of(
            Map.of("ID", 1, "이름", "홍길동", "이메일", "hong@example.com", "가입일", LocalDate.now()),
            Map.of("ID", 2, "이름", "김철수", "이메일", "kim@example.com", "가입일", LocalDate.now())
        );

        // 시트 생성 및 데이터 쓰기
        writer.createSheet("Users");
        writer.writeHeader(headers);
        writer.writeData(data);

        // 파일로 저장
        writer.write(Path.of("users.xlsx"));
    }
}
```

### 2. Excel 파일 읽기

```java
import com.eraf.excel.ExcelReader;

public void readExcel() throws IOException {
    try (ExcelReader reader = new ExcelReader(Path.of("users.xlsx"))) {
        // 첫 번째 시트 선택
        reader.selectSheet(0);

        // 헤더 읽기
        List<String> headers = reader.readHeader();
        System.out.println("Headers: " + headers);

        // 데이터 읽기
        List<Map<String, Object>> data = reader.readAll();
        data.forEach(row -> {
            System.out.println("ID: " + row.get("ID"));
            System.out.println("이름: " + row.get("이름"));
        });
    }
}
```

### 3. Entity에서 Excel 생성

```java
public void exportUsers(List<User> users) throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Users");
        writer.writeHeader(List.of("ID", "Username", "Email", "Created At"));

        List<Map<String, Object>> data = users.stream()
            .map(user -> Map.of(
                "ID", user.getId(),
                "Username", user.getUsername(),
                "Email", user.getEmail(),
                "Created At", user.getCreatedAt()
            ))
            .toList();

        writer.writeData(data);
        writer.write(Path.of("users_export.xlsx"));
    }
}
```

### 4. 스트리밍 (대용량 파일)

```java
import com.eraf.excel.StreamingExcelWriter;

public void exportLargeData() throws IOException {
    try (StreamingExcelWriter writer = new StreamingExcelWriter()) {
        writer.createSheet("Large Dataset");
        writer.writeHeader(List.of("ID", "Name", "Value"));

        // 100만 행 데이터도 메모리 효율적으로 처리
        for (int i = 0; i < 1_000_000; i++) {
            Map<String, Object> row = Map.of(
                "ID", i,
                "Name", "Item " + i,
                "Value", Math.random() * 1000
            );
            writer.writeRow(row);
        }

        writer.write(Path.of("large_dataset.xlsx"));
    }
}
```

## 고급 기능

### 1. 다중 시트

```java
public void createMultiSheetExcel() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        // 첫 번째 시트
        writer.createSheet("Users");
        writer.writeHeader(List.of("ID", "Name"));
        writer.writeData(userData);

        // 두 번째 시트
        writer.createSheet("Orders");
        writer.writeHeader(List.of("Order ID", "User ID", "Amount"));
        writer.writeData(orderData);

        writer.write(Path.of("multi_sheet.xlsx"));
    }
}
```

### 2. 커스텀 스타일

```java
import org.apache.poi.ss.usermodel.*;

public void createStyledExcel() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Styled Data");

        // 커스텀 스타일 생성
        CellStyle highlightStyle = writer.getWorkbook().createCellStyle();
        highlightStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font redFont = writer.getWorkbook().createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        redFont.setBold(true);

        CellStyle errorStyle = writer.getWorkbook().createCellStyle();
        errorStyle.setFont(redFont);

        // 스타일 적용하여 쓰기
        writer.writeHeader(List.of("Status", "Message", "Value"));
        writer.writeRowWithStyle(
            Map.of("Status", "ERROR", "Message", "Failed", "Value", 0),
            errorStyle
        );
    }
}
```

### 3. 수식 (Formula)

```java
public void createFormulaExcel() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Sales");
        writer.writeHeader(List.of("Item", "Price", "Quantity", "Total"));

        // 데이터 행 쓰기
        writer.writeRow(Map.of("Item", "Product A", "Price", 10000, "Quantity", 5));
        writer.writeRow(Map.of("Item", "Product B", "Price", 20000, "Quantity", 3));

        // 수식 행 추가
        Sheet sheet = writer.getCurrentSheet();
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        Cell cell = row.createCell(2);
        cell.setCellValue("Total:");

        cell = row.createCell(3);
        cell.setCellFormula("SUM(D2:D3)");  // 자동 합계

        writer.write(Path.of("sales.xlsx"));
    }
}
```

### 4. 조건부 서식

```java
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.util.CellRangeAddress;

public void createConditionalFormatting() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Scores");
        writer.writeHeader(List.of("Student", "Score"));
        writer.writeData(scoreData);

        Sheet sheet = writer.getCurrentSheet();
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // 80점 이상 초록색
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(
            ComparisonOperator.GE, "80"
        );
        PatternFormatting fill1 = rule1.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.GREEN.index);

        CellRangeAddress[] regions = {CellRangeAddress.valueOf("B2:B100")};
        sheetCF.addConditionalFormatting(regions, rule1);

        writer.write(Path.of("scores.xlsx"));
    }
}
```

### 5. 차트 생성

```java
import org.apache.poi.xddf.usermodel.chart.*;

public void createChartExcel() throws IOException {
    try (ExcelWriter writer = new ExcelWriter()) {
        writer.createSheet("Chart Data");
        writer.writeHeader(List.of("Month", "Sales"));
        writer.writeData(monthlyData);

        Sheet sheet = writer.getCurrentSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 4, 1, 14, 15);

        Chart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        // 막대 차트 생성
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        XDDFDataSource<String> months = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(1, 12, 0, 0)
        );
        XDDFNumericalDataSource<Double> sales = XDDFDataSourcesFactory.fromNumericCellRange(
            sheet, new CellRangeAddress(1, 12, 1, 1)
        );

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(months, sales);
        series.setTitle("Monthly Sales", null);

        chart.plot(data);

        writer.write(Path.of("chart.xlsx"));
    }
}
```

## HTTP Response로 다운로드

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ExcelController {

    @GetMapping("/export/users")
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");

        try (ExcelWriter writer = new ExcelWriter()) {
            writer.createSheet("Users");
            writer.writeHeader(List.of("ID", "Name", "Email"));
            writer.writeData(getUserData());
            writer.write(response.getOutputStream());
        }
    }
}
```

## Excel 템플릿 사용

```java
public void useTemplate() throws IOException {
    // 기존 템플릿 파일 로드
    try (FileInputStream fis = new FileInputStream("template.xlsx");
         Workbook workbook = WorkbookFactory.create(fis)) {

        Sheet sheet = workbook.getSheetAt(0);

        // 특정 셀에 데이터 채우기
        Row row = sheet.getRow(2);
        row.getCell(1).setCellValue("홍길동");
        row.getCell(2).setCellValue("hong@example.com");

        // 수정된 파일 저장
        try (FileOutputStream fos = new FileOutputStream("output.xlsx")) {
            workbook.write(fos);
        }
    }
}
```

## 에러 처리

```java
public void readExcelSafely(Path path) {
    try (ExcelReader reader = new ExcelReader(path)) {
        if (!reader.hasSheet("Users")) {
            throw new BusinessException("Users sheet not found");
        }

        reader.selectSheet("Users");
        List<Map<String, Object>> data = reader.readAll();

        if (data.isEmpty()) {
            throw new BusinessException("No data found");
        }

        // 데이터 검증
        for (Map<String, Object> row : data) {
            if (row.get("ID") == null) {
                throw new BusinessException("ID is required");
            }
        }

    } catch (IOException e) {
        throw new BusinessException("Failed to read Excel file", e);
    }
}
```

## 모범 사례

1. **AutoCloseable**: try-with-resources로 자동 리소스 해제
2. **스트리밍**: 100MB 이상 파일은 StreamingExcelWriter 사용
3. **메모리**: 대용량 데이터는 chunk 단위로 쓰기
4. **스타일**: 스타일 객체 재사용으로 파일 크기 절감
5. **검증**: 읽은 데이터는 반드시 검증
6. **에러 처리**: IOException에 대한 명확한 처리
7. **인코딩**: UTF-8 사용 권장

## 참고

- [Apache POI](https://poi.apache.org/)
- [Excel Format](https://docs.microsoft.com/en-us/office/open-xml/spreadsheet)
