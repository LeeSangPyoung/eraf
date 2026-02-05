package com.eraf.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("이미지 리사이즈")
    void testResize() {
        // Given
        BufferedImage original = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        // When
        BufferedImage resized = ImageUtils.resize(original, 400, 300);

        // Then
        assertNotNull(resized);
        assertEquals(400, resized.getWidth());
        assertEquals(300, resized.getHeight());
    }

    @Test
    @DisplayName("비율 유지 리사이즈")
    void testResizeKeepAspectRatio() {
        // Given
        BufferedImage original = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        // When
        BufferedImage resized = ImageUtils.resizeKeepAspectRatio(original, 400);

        // Then
        assertNotNull(resized);
        assertEquals(400, resized.getWidth());
        assertEquals(300, resized.getHeight());
    }

    @Test
    @DisplayName("이미지 크롭")
    void testCrop() {
        // Given
        BufferedImage original = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        // When
        BufferedImage cropped = ImageUtils.crop(original, 100, 100, 200, 200);

        // Then
        assertNotNull(cropped);
        assertEquals(200, cropped.getWidth());
        assertEquals(200, cropped.getHeight());
    }

    @Test
    @DisplayName("이미지 회전")
    void testRotate() {
        // Given
        BufferedImage original = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        // When
        BufferedImage rotated = ImageUtils.rotate(original, 90);

        // Then
        assertNotNull(rotated);
    }
}
