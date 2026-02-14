package com.eraf.barcode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("QR 코드 파일 생성")
    void testGenerateQRCode() throws Exception {
        // Given
        String content = "https://example.com";
        Path output = tempDir.resolve("qr.png");

        // When
        QRCodeGenerator.generate(content, output, 200);

        // Then
        assertTrue(output.toFile().exists());
        assertTrue(output.toFile().length() > 0);
    }

    @Test
    @DisplayName("QR 코드 바이트 배열 생성")
    void testGenerateQRCodeToBytes() throws Exception {
        // Given
        String content = "TEST-DATA-123";

        // When
        byte[] bytes = QRCodeGenerator.generateToBytes(content, 150);

        // Then
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    @DisplayName("URL QR 코드 생성")
    void testGenerateUrlQRCode() throws Exception {
        // Given
        String url = "https://eraf.com";
        Path output = tempDir.resolve("url-qr.png");

        // When
        QRCodeGenerator.generateUrl(url, output, 200);

        // Then
        assertTrue(output.toFile().exists());
        assertTrue(output.toFile().length() > 0);
    }
}
