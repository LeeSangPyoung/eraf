package com.eraf.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("PDF 생성기 인스턴스 생성")
    void testCreateInstance() {
        // Given & When
        PdfGenerator generator = new PdfGenerator();

        // Then
        assertNotNull(generator);
    }

    @Test
    @DisplayName("HTML을 PDF로 변환")
    void testGenerateFromHtml() {
        // Given
        PdfGenerator generator = new PdfGenerator();
        String html = "<html><body><h1>Test PDF</h1><p>This is a test.</p></body></html>";

        // When
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        generator.generateFromHtml(html, output);

        // Then
        assertTrue(output.size() > 0);
    }

    @Test
    @DisplayName("텍스트 추가")
    void testAddText() {
        // Given
        PdfGenerator generator = new PdfGenerator();

        // When
        generator.addText("Hello World");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        generator.generate(output);

        // Then
        assertTrue(output.size() > 0);
    }
}
