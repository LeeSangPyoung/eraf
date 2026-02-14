package com.eraf.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportServiceTest {

    private ReportService reportService;
    private ReportProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ReportProperties();
        reportService = new ReportService(properties);
        reportService.registerGenerator(new CsvReportGenerator());
        reportService.registerGenerator(new HtmlReportGenerator());
    }

    @Test
    @DisplayName("CSV 리포트 생성")
    void testGenerateCsv() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("sales", ReportFormat.CSV)
                .title("Sales Report")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .addRow(Map.of("Name", "Product A", "Amount", 1000))
                .addRow(Map.of("Name", "Product B", "Amount", 2500))
                .build();

        // When
        byte[] result = reportService.generate(definition, data);

        // Then
        assertThat(result).isNotEmpty();
        String csv = new String(result, StandardCharsets.UTF_8);
        assertThat(csv).contains("Name,Amount");
        assertThat(csv).contains("Product A,1000");
        assertThat(csv).contains("Product B,2500");
    }

    @Test
    @DisplayName("HTML 리포트 생성")
    void testGenerateHtml() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("users", ReportFormat.HTML)
                .title("User List")
                .description("Active users report")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("ID", "Name", "Email"))
                .addRow(Map.of("ID", 1, "Name", "John", "Email", "john@test.com"))
                .addRow(Map.of("ID", 2, "Name", "Jane", "Email", "jane@test.com"))
                .totalCount(2)
                .build();

        // When
        byte[] result = reportService.generate(definition, data);

        // Then
        assertThat(result).isNotEmpty();
        String html = new String(result, StandardCharsets.UTF_8);
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<h1>User List</h1>");
        assertThat(html).contains("Active users report");
        assertThat(html).contains("<td>John</td>");
        assertThat(html).contains("<td>jane@test.com</td>");
    }

    @Test
    @DisplayName("지원하지 않는 포맷 시 예외 발생")
    void testUnsupportedFormat() {
        // Given - PDF generator not registered
        ReportDefinition definition = ReportDefinition.builder("report", ReportFormat.PDF)
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .build();

        // When & Then
        assertThatThrownBy(() -> reportService.generate(definition, data))
                .isInstanceOf(ReportGenerationException.class)
                .hasMessageContaining("No generator registered for format: PDF");
    }

    @Test
    @DisplayName("커스텀 제너레이터 등록")
    void testRegisterCustomGenerator() {
        // Given - custom PDF-like generator
        ReportGenerator customGenerator = new ReportGenerator() {
            @Override
            public ReportFormat getFormat() {
                return ReportFormat.PDF;
            }

            @Override
            public byte[] generate(ReportDefinition definition, ReportData data) {
                return ("PDF:" + definition.getName()).getBytes(StandardCharsets.UTF_8);
            }
        };

        reportService.registerGenerator(customGenerator);

        ReportDefinition definition = ReportDefinition.builder("invoice", ReportFormat.PDF)
                .build();
        ReportData data = ReportData.builder()
                .headers(List.of("Item"))
                .build();

        // When
        byte[] result = reportService.generate(definition, data);

        // Then
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("PDF:invoice");
    }

    @Test
    @DisplayName("지원 포맷 목록 조회")
    void testGetSupportedFormats() {
        // When
        List<ReportFormat> formats = reportService.getSupportedFormats();

        // Then
        assertThat(formats).contains(ReportFormat.CSV, ReportFormat.HTML);
        assertThat(formats).doesNotContain(ReportFormat.PDF, ReportFormat.EXCEL);
    }

    @Test
    @DisplayName("포맷 지원 여부 확인")
    void testIsFormatSupported() {
        assertThat(reportService.isFormatSupported(ReportFormat.CSV)).isTrue();
        assertThat(reportService.isFormatSupported(ReportFormat.HTML)).isTrue();
        assertThat(reportService.isFormatSupported(ReportFormat.PDF)).isFalse();
        assertThat(reportService.isFormatSupported(ReportFormat.EXCEL)).isFalse();
    }

    @Test
    @DisplayName("파일로 리포트 생성")
    void testGenerateToFile(@TempDir Path tempDir) throws IOException {
        // Given
        ReportDefinition definition = ReportDefinition.builder("output", ReportFormat.CSV)
                .title("File Output Test")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test Item"))
                .build();

        Path outputPath = tempDir.resolve("output.csv");

        // When
        reportService.generateToFile(definition, data, outputPath);

        // Then
        assertThat(Files.exists(outputPath)).isTrue();
        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        assertThat(content).contains("Name");
        assertThat(content).contains("Test Item");
    }

    @Test
    @DisplayName("하위 디렉토리에 파일 생성")
    void testGenerateToFileWithSubdirectory(@TempDir Path tempDir) {
        // Given
        ReportDefinition definition = ReportDefinition.builder("nested", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Col"))
                .addRow(Map.of("Col", "Val"))
                .build();

        Path outputPath = tempDir.resolve("sub/dir/report.csv");

        // When
        reportService.generateToFile(definition, data, outputPath);

        // Then
        assertThat(Files.exists(outputPath)).isTrue();
    }

    @Test
    @DisplayName("maxRows 초과 시 예외 발생")
    void testMaxRowsExceeded() {
        // Given
        properties.setMaxRows(2);

        ReportDefinition definition = ReportDefinition.builder("big", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "A"))
                .addRow(Map.of("Name", "B"))
                .addRow(Map.of("Name", "C"))
                .build();

        // When & Then
        assertThatThrownBy(() -> reportService.generate(definition, data))
                .isInstanceOf(ReportGenerationException.class)
                .hasMessageContaining("exceeds maximum allowed: 2");
    }

    @Test
    @DisplayName("maxRows 0이면 제한 없음")
    void testMaxRowsUnlimited() {
        // Given
        properties.setMaxRows(0);

        ReportDefinition definition = ReportDefinition.builder("unlimited", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "A"))
                .addRow(Map.of("Name", "B"))
                .addRow(Map.of("Name", "C"))
                .build();

        // When
        byte[] result = reportService.generate(definition, data);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("기존 제너레이터 교체")
    void testReplaceExistingGenerator() {
        // Given - replace CSV generator with custom one
        ReportGenerator customCsv = new ReportGenerator() {
            @Override
            public ReportFormat getFormat() {
                return ReportFormat.CSV;
            }

            @Override
            public byte[] generate(ReportDefinition definition, ReportData data) {
                return "CUSTOM_CSV".getBytes(StandardCharsets.UTF_8);
            }
        };

        reportService.registerGenerator(customCsv);

        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder().headers(List.of("X")).build();

        // When
        byte[] result = reportService.generate(definition, data);

        // Then
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("CUSTOM_CSV");
    }
}
