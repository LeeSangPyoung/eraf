package com.eraf.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReportData 테스트")
class ReportDataTest {

    @Test
    @DisplayName("Builder로 데이터 생성")
    void builder_ShouldCreateData() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name", "Amount"))
                .addRow(Map.of("Name", "Item A", "Amount", 1000))
                .addRow(Map.of("Name", "Item B", "Amount", 2000))
                .totalCount(100)
                .page(1)
                .pageSize(10)
                .metadata("generatedBy", "system")
                .build();

        assertThat(data.getHeaders()).containsExactly("Name", "Amount");
        assertThat(data.getRows()).hasSize(2);
        assertThat(data.getTotalCount()).isEqualTo(100);
        assertThat(data.getPage()).isEqualTo(1);
        assertThat(data.getPageSize()).isEqualTo(10);
        assertThat(data.getMetadata()).containsEntry("generatedBy", "system");
    }

    @Test
    @DisplayName("빈 데이터 isEmpty 확인")
    void isEmpty_ShouldReturnTrueForNoRows() {
        ReportData empty = ReportData.builder()
                .headers(List.of("Name"))
                .build();
        assertThat(empty.isEmpty()).isTrue();

        ReportData notEmpty = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .build();
        assertThat(notEmpty.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("rows() 메서드로 일괄 설정")
    void rows_ShouldReplaceAllRows() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Before"))
                .rows(List.of(
                        Map.of("Name", "After1"),
                        Map.of("Name", "After2")
                ))
                .build();

        assertThat(data.getRows()).hasSize(2);
        assertThat(data.getRows().get(0)).containsEntry("Name", "After1");
    }

    @Test
    @DisplayName("headers는 읽기 전용")
    void headers_ShouldBeUnmodifiable() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .build();
        assertThatThrownBy(() -> data.getHeaders().add("Extra"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("rows는 읽기 전용")
    void rows_ShouldBeUnmodifiable() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .addRow(Map.of("Name", "Test"))
                .build();
        assertThatThrownBy(() -> data.getRows().add(Map.of("Name", "Extra")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("metadata는 읽기 전용")
    void metadata_ShouldBeUnmodifiable() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .metadata("key", "value")
                .build();
        assertThatThrownBy(() -> data.getMetadata().put("extra", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("기본값 확인")
    void defaults_ShouldBeCorrect() {
        ReportData data = ReportData.builder()
                .headers(List.of("Name"))
                .build();

        assertThat(data.getTotalCount()).isEqualTo(0);
        assertThat(data.getPage()).isEqualTo(1);
        assertThat(data.getPageSize()).isEqualTo(0);
        assertThat(data.getMetadata()).isEmpty();
    }
}
