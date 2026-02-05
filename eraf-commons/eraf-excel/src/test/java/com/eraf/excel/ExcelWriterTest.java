package com.eraf.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ExcelWriter 생성")
    void testCreateExcelWriter() {
        // Given & When
        ExcelWriter writer = new ExcelWriter();

        // Then
        assertNotNull(writer);
    }

    @Test
    @DisplayName("시트 추가")
    void testAddSheet() {
        // Given
        ExcelWriter writer = new ExcelWriter();

        // When
        writer.createSheet("TestSheet");

        // Then
        assertNotNull(writer);
    }

    @Test
    @DisplayName("데이터 쓰기")
    void testWriteData() {
        // Given
        ExcelWriter writer = new ExcelWriter();
        writer.createSheet("Data");

        List<String> headers = Arrays.asList("Name", "Age", "Email");
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("John", 30, "john@example.com"),
                Arrays.asList("Jane", 25, "jane@example.com")
        );

        // When
        writer.writeHeaders(headers);
        writer.writeRows(data);

        // Then
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(out);
        assertTrue(out.size() > 0);
    }

    @Test
    @DisplayName("파일로 저장")
    void testWriteToFile() {
        // Given
        ExcelWriter writer = new ExcelWriter();
        writer.createSheet("Test");
        writer.writeHeaders(Arrays.asList("Col1", "Col2"));

        File file = tempDir.resolve("test.xlsx").toFile();

        // When
        writer.writeToFile(file.getAbsolutePath());

        // Then
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
