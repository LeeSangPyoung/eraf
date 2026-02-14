package com.eraf.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("ExcelWriter 생성")
    void testCreateExcelWriter() throws Exception {
        try (ExcelWriter writer = new ExcelWriter()) {
            assertNotNull(writer);
        }
    }

    @Test
    @DisplayName("시트 추가")
    void testAddSheet() throws Exception {
        try (ExcelWriter writer = new ExcelWriter()) {
            writer.createSheet("TestSheet");
            assertNotNull(writer);
        }
    }

    @Test
    @DisplayName("데이터 쓰기")
    void testWriteData() throws Exception {
        try (ExcelWriter writer = new ExcelWriter()) {
            List<List<Object>> data = Arrays.asList(
                    Arrays.asList("Name", "Age", "Email"),
                    Arrays.asList("John", 30, "john@example.com"),
                    Arrays.asList("Jane", 25, "jane@example.com")
            );

            writer.write("Data", data, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.write(out);
            assertTrue(out.size() > 0);
        }
    }

    @Test
    @DisplayName("파일로 저장")
    void testSaveToFile() throws Exception {
        try (ExcelWriter writer = new ExcelWriter()) {
            List<List<Object>> data = Arrays.asList(
                    Arrays.asList("Col1", "Col2"),
                    Arrays.asList("val1", "val2")
            );

            writer.write("Test", data);

            Path file = tempDir.resolve("test.xlsx");
            writer.save(file);

            assertTrue(file.toFile().exists());
            assertTrue(file.toFile().length() > 0);
        }
    }
}
