package com.eraf.barcode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeGeneratorTest {

    @Test
    @DisplayName("QR 코드 생성")
    void testGenerateQRCode() {
        // Given
        QRCodeGenerator generator = new QRCodeGenerator();
        String content = "https://example.com";

        // When
        BufferedImage image = generator.generate(content, 200, 200);

        // Then
        assertNotNull(image);
        assertEquals(200, image.getWidth());
        assertEquals(200, image.getHeight());
    }

    @Test
    @DisplayName("QR 코드 Base64 생성")
    void testGenerateQRCodeBase64() {
        // Given
        QRCodeGenerator generator = new QRCodeGenerator();
        String content = "TEST-DATA-123";

        // When
        String base64 = generator.generateBase64(content, 150, 150);

        // Then
        assertNotNull(base64);
        assertTrue(base64.length() > 0);
    }

    @Test
    @DisplayName("빈 내용으로 생성 시 예외")
    void testGenerateWithEmptyContent() {
        // Given
        QRCodeGenerator generator = new QRCodeGenerator();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate("", 100, 100);
        });
    }
}
