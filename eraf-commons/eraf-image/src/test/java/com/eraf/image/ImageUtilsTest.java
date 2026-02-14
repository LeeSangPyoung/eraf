package com.eraf.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilsTest {

    @TempDir
    Path tempDir;

    private Path sourceImage;

    @BeforeEach
    void setUp() throws Exception {
        // Create a test source image (800x600 PNG)
        sourceImage = tempDir.resolve("source.png");
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "png", sourceImage.toFile());
    }

    @Test
    @DisplayName("이미지 리사이즈 (비율 유지)")
    void testResize() throws Exception {
        // Given
        Path target = tempDir.resolve("resized.png");

        // When
        ImageUtils.resize(sourceImage, target, 400, 300);

        // Then
        assertTrue(target.toFile().exists());
        assertTrue(target.toFile().length() > 0);
    }

    @Test
    @DisplayName("이미지 리사이즈 (비율 강제)")
    void testResizeForce() throws Exception {
        // Given
        Path target = tempDir.resolve("force-resized.png");

        // When
        ImageUtils.resizeForce(sourceImage, target, 400, 200);

        // Then
        assertTrue(target.toFile().exists());
        assertTrue(target.toFile().length() > 0);
    }

    @Test
    @DisplayName("이미지 크롭 (좌표 지정)")
    void testCrop() throws Exception {
        // Given
        Path target = tempDir.resolve("cropped.png");

        // When
        ImageUtils.crop(sourceImage, target, 100, 100, 200, 200);

        // Then
        assertTrue(target.toFile().exists());
        ImageUtils.ImageInfo info = ImageUtils.getInfo(target);
        assertEquals(200, info.width());
        assertEquals(200, info.height());
    }

    @Test
    @DisplayName("이미지 회전")
    void testRotate() throws Exception {
        // Given
        Path target = tempDir.resolve("rotated.png");

        // When
        ImageUtils.rotate(sourceImage, target, 90);

        // Then
        assertTrue(target.toFile().exists());
        assertTrue(target.toFile().length() > 0);
    }

    @Test
    @DisplayName("이미지 정보 조회")
    void testGetInfo() throws Exception {
        // When
        ImageUtils.ImageInfo info = ImageUtils.getInfo(sourceImage);

        // Then
        assertEquals(800, info.width());
        assertEquals(600, info.height());
        assertEquals("png", info.format());
    }

    @Test
    @DisplayName("BufferedImage ↔ byte[] 변환")
    void testBytesConversion() throws Exception {
        // Given
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // When
        byte[] bytes = ImageUtils.toBytes(img, "png");
        BufferedImage restored = ImageUtils.toBufferedImage(bytes);

        // Then
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertNotNull(restored);
        assertEquals(100, restored.getWidth());
        assertEquals(100, restored.getHeight());
    }

    @Test
    @DisplayName("썸네일 생성")
    void testThumbnail() throws Exception {
        // Given
        Path target = tempDir.resolve("thumb.png");

        // When
        ImageUtils.thumbnail(sourceImage, target, 100);

        // Then
        assertTrue(target.toFile().exists());
        assertTrue(target.toFile().length() > 0);
    }
}
