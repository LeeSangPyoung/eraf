package com.eraf.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("텍스트 PDF 생성")
    void testCreateTextPdf() throws Exception {
        // Given
        Path output = tempDir.resolve("text.pdf");

        // When
        PdfGenerator.createTextPdf("Hello World", output);

        // Then
        assertTrue(output.toFile().exists());
        assertTrue(output.toFile().length() > 0);
    }

    @Test
    @DisplayName("HTML을 PDF로 변환")
    void testFromHtml() throws Exception {
        // Given
        String html = "<html><body><h1>Test</h1><p>Content</p></body></html>";
        Path output = tempDir.resolve("html.pdf");

        // When
        PdfGenerator.fromHtml(html, output);

        // Then
        assertTrue(output.toFile().exists());
        assertTrue(output.toFile().length() > 0);
    }

    @Test
    @DisplayName("HTML을 바이트 배열로 변환")
    void testFromHtmlToBytes() throws Exception {
        // Given
        String html = "<html><body><h1>Test</h1></body></html>";

        // When
        byte[] bytes = PdfGenerator.fromHtmlToBytes(html, null);

        // Then
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    @DisplayName("빈 PDF 생성")
    void testCreateEmpty() throws Exception {
        // Given
        Path output = tempDir.resolve("empty.pdf");

        // When
        PdfGenerator.createEmpty(output, 3);

        // Then
        assertTrue(output.toFile().exists());
        assertTrue(output.toFile().length() > 0);
    }
}
