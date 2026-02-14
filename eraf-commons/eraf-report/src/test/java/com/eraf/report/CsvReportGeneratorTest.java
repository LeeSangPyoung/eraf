package com.eraf.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReportGeneratorTest {

    private CsvReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CsvReportGenerator();
    }

    @Test
    @DisplayName("기본 CSV 생성")
    void testBasicCsv() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV)
                .title("Test Report")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount", "Date"))
                .addRow(Map.of("Name", "Item A", "Amount", 1000, "Date", "2026-01-01"))
                .addRow(Map.of("Name", "Item B", "Amount", 2000, "Date", "2026-01-02"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then - BOM is included by default, skip it for content assertion
        String content = csv.substring(1); // skip BOM character
        assertThat(content).contains("Name,Amount,Date");
        assertThat(content).contains("Item A,1000,2026-01-01");
        assertThat(content).contains("Item B,2000,2026-01-02");
    }

    @Test
    @DisplayName("UTF-8 BOM 포함 확인")
    void testUtf8Bom() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);

        // Then - UTF-8 BOM: EF BB BF
        assertThat(result[0]).isEqualTo((byte) 0xEF);
        assertThat(result[1]).isEqualTo((byte) 0xBB);
        assertThat(result[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    @DisplayName("BOM 비활성화")
    void testWithoutBom() {
        // Given
        generator.setIncludeBom(false);
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then - should start directly with header
        assertThat(csv).startsWith("Name");
    }

    @Test
    @DisplayName("특수 문자 처리 (쉼표, 따옴표, 줄바꿈)")
    void testWithSpecialCharacters() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Description"))
                .addRow(Map.of("Name", "Item, A", "Description", "Has \"quotes\""))
                .addRow(Map.of("Name", "Item B", "Description", "Line1\nLine2"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then - fields with special chars should be quoted
        assertThat(csv).contains("\"Item, A\"");
        assertThat(csv).contains("\"Has \"\"quotes\"\"\"");
        assertThat(csv).contains("\"Line1\nLine2\"");
    }

    @Test
    @DisplayName("빈 데이터 처리")
    void testEmptyData() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then - should have header only
        String content = csv.substring(1); // skip BOM
        String[] lines = content.trim().split("\r\n");
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEqualTo("Name,Amount");
    }

    @Test
    @DisplayName("커스텀 구분자 설정")
    void testCustomDelimiter() {
        // Given
        generator.setDelimiter(';');
        generator.setIncludeBom(false);

        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .addRow(Map.of("Name", "Item A", "Amount", 1000))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csv).contains("Name;Amount");
        assertThat(csv).contains("Item A;1000");
    }

    @Test
    @DisplayName("CSV 포맷 반환")
    void testGetFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.CSV);
    }

    @Test
    @DisplayName("null 값 처리")
    void testNullValues() {
        // Given
        generator.setIncludeBom(false);
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.CSV).build();

        // Row with missing key "Amount" - should produce empty cell
        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .addRow(Map.of("Name", "Item A"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(csv).contains("Item A,");
    }
}
