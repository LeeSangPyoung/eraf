package com.eraf.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlReportGeneratorTest {

    private HtmlReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new HtmlReportGenerator();
    }

    @Test
    @DisplayName("기본 HTML 테이블 생성")
    void testBasicHtml() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("test", ReportFormat.HTML)
                .title("Test Report")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .addRow(Map.of("Name", "Item A", "Amount", 1000))
                .addRow(Map.of("Name", "Item B", "Amount", 2000))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<title>Test Report</title>");
        assertThat(html).contains("<th>Name</th>");
        assertThat(html).contains("<th>Amount</th>");
        assertThat(html).contains("<td>Item A</td>");
        assertThat(html).contains("<td>1000</td>");
        assertThat(html).contains("<td>Item B</td>");
        assertThat(html).contains("<td>2000</td>");
        assertThat(html).contains("Total rows: 2");
    }

    @Test
    @DisplayName("제목과 설명 포함")
    void testWithTitle() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("salesReport", ReportFormat.HTML)
                .title("Sales Report")
                .description("Monthly sales summary by region")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Region", "Total"))
                .addRow(Map.of("Region", "East", "Total", 50000))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(html).contains("<h1>Sales Report</h1>");
        assertThat(html).contains("Monthly sales summary by region");
        assertThat(html).contains("<title>Sales Report</title>");
    }

    @Test
    @DisplayName("제목 미설정 시 이름 사용")
    void testWithoutTitle() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("myReport", ReportFormat.HTML)
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Col1"))
                .addRow(Map.of("Col1", "Value1"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then - should fall back to report name
        assertThat(html).contains("<h1>myReport</h1>");
    }

    @Test
    @DisplayName("빈 데이터 처리")
    void testEmptyData() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("empty", ReportFormat.HTML)
                .title("Empty Report")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(html).contains("No data");
        assertThat(html).contains("Total rows: 0");
    }

    @Test
    @DisplayName("메타데이터 포함")
    void testWithMetadata() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("meta", ReportFormat.HTML)
                .title("Report with Metadata")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .totalCount(100)
                .metadata("generatedBy", "system")
                .metadata("department", "sales")
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(html).contains("Total count: 100");
        assertThat(html).contains("generatedBy: system");
        assertThat(html).contains("department: sales");
    }

    @Test
    @DisplayName("HTML 특수문자 이스케이프")
    void testHtmlEscaping() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("escape", ReportFormat.HTML)
                .title("Report <script>alert('xss')</script>")
                .build();

        ReportData data = ReportData.builder()
                .headers(List.of("Content"))
                .addRow(Map.of("Content", "<b>Bold</b> & \"quoted\""))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then - XSS characters should be escaped
        assertThat(html).doesNotContain("<script>");
        assertThat(html).contains("&lt;script&gt;");
        assertThat(html).contains("&lt;b&gt;Bold&lt;/b&gt; &amp; &quot;quoted&quot;");
    }

    @Test
    @DisplayName("HTML 포맷 반환")
    void testGetFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.HTML);
    }

    @Test
    @DisplayName("CSS 스타일 포함")
    void testIncludesStyles() {
        // Given
        ReportDefinition definition = ReportDefinition.builder("styled", ReportFormat.HTML).build();
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .build();

        // When
        byte[] result = generator.generate(definition, data);
        String html = new String(result, StandardCharsets.UTF_8);

        // Then
        assertThat(html).contains("<style>");
        assertThat(html).contains("border-collapse: collapse");
    }
}
