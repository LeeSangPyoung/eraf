package com.eraf.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReportDefinition 테스트")
class ReportDefinitionTest {

    @Test
    @DisplayName("Builder로 정의 생성")
    void builder_ShouldCreateDefinition() {
        ReportDefinition definition = ReportDefinition.builder("salesReport", ReportFormat.CSV)
                .title("Sales Report")
                .description("Monthly sales summary")
                .build();

        assertThat(definition.getName()).isEqualTo("salesReport");
        assertThat(definition.getFormat()).isEqualTo(ReportFormat.CSV);
        assertThat(definition.getTitle()).isEqualTo("Sales Report");
        assertThat(definition.getDescription()).isEqualTo("Monthly sales summary");
    }

    @Test
    @DisplayName("파라미터 추가")
    void builder_WithParameter_ShouldIncludeParameter() {
        ReportParameter param = ReportParameter.builder("month", ReportParameter.Type.STRING)
                .required(true)
                .description("Target month")
                .build();

        ReportDefinition definition = ReportDefinition.builder("report", ReportFormat.HTML)
                .parameter(param)
                .build();

        assertThat(definition.getParameters()).containsKey("month");
        assertThat(definition.getParameters().get("month").isRequired()).isTrue();
    }

    @Test
    @DisplayName("파일명 생성")
    void getFileName_ShouldReturnNameWithExtension() {
        ReportDefinition csv = ReportDefinition.builder("sales", ReportFormat.CSV).build();
        assertThat(csv.getFileName()).isEqualTo("sales.csv");

        ReportDefinition html = ReportDefinition.builder("users", ReportFormat.HTML).build();
        assertThat(html.getFileName()).isEqualTo("users.html");

        ReportDefinition pdf = ReportDefinition.builder("invoice", ReportFormat.PDF).build();
        assertThat(pdf.getFileName()).isEqualTo("invoice.pdf");

        ReportDefinition excel = ReportDefinition.builder("data", ReportFormat.EXCEL).build();
        assertThat(excel.getFileName()).isEqualTo("data.xlsx");
    }

    @Test
    @DisplayName("이름 없이 빌드 시 예외")
    void builder_WithoutName_ShouldThrow() {
        assertThatThrownBy(() -> ReportDefinition.builder("", ReportFormat.CSV).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("포맷 없이 빌드 시 예외")
    void builder_WithoutFormat_ShouldThrow() {
        assertThatThrownBy(() -> ReportDefinition.builder("report", null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format");
    }

    @Test
    @DisplayName("파라미터 맵은 읽기 전용")
    void parameters_ShouldBeUnmodifiable() {
        ReportDefinition definition = ReportDefinition.builder("report", ReportFormat.CSV).build();
        assertThatThrownBy(() ->
                definition.getParameters().put("test",
                        ReportParameter.builder("test", ReportParameter.Type.STRING).build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
