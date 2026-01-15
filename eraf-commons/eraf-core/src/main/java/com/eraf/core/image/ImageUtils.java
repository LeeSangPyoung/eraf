package com.eraf.core.image;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

/**
 * 이미지 처리 유틸리티
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * 이미지 리사이즈 (비율 유지)
     */
    public static void resize(Path source, Path target, int width, int height) throws IOException {
        Thumbnails.of(source.toFile())
                .size(width, height)
                .keepAspectRatio(true)
                .toFile(target.toFile());
    }

    /**
     * 이미지 리사이즈 (비율 강제)
     */
    public static void resizeForce(Path source, Path target, int width, int height) throws IOException {
        Thumbnails.of(source.toFile())
                .forceSize(width, height)
                .toFile(target.toFile());
    }

    /**
     * 이미지 리사이즈 (스케일 비율)
     */
    public static void scale(Path source, Path target, double scale) throws IOException {
        Thumbnails.of(source.toFile())
                .scale(scale)
                .toFile(target.toFile());
    }

    /**
     * 이미지 크롭 (중앙)
     */
    public static void cropCenter(Path source, Path target, int width, int height) throws IOException {
        Thumbnails.of(source.toFile())
                .sourceRegion(Positions.CENTER, width, height)
                .size(width, height)
                .toFile(target.toFile());
    }

    /**
     * 이미지 크롭 (좌표 지정)
     */
    public static void crop(Path source, Path target, int x, int y, int width, int height) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        BufferedImage cropped = original.getSubimage(x, y, width, height);
        String format = getFormat(target.toString());
        ImageIO.write(cropped, format, target.toFile());
    }

    /**
     * 이미지 회전
     */
    public static void rotate(Path source, Path target, double degrees) throws IOException {
        Thumbnails.of(source.toFile())
                .scale(1.0)
                .rotate(degrees)
                .toFile(target.toFile());
    }

    /**
     * 텍스트 워터마크 추가
     */
    public static void watermark(Path source, Path target, String text, float opacity) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        BufferedImage watermarked = addTextWatermark(original, text, opacity);
        String format = getFormat(target.toString());
        ImageIO.write(watermarked, format, target.toFile());
    }

    /**
     * 이미지 워터마크 추가
     */
    public static void watermark(Path source, Path target, Path watermarkImage, float opacity) throws IOException {
        BufferedImage watermark = ImageIO.read(watermarkImage.toFile());
        Thumbnails.of(source.toFile())
                .scale(1.0)
                .watermark(Positions.BOTTOM_RIGHT, watermark, opacity)
                .toFile(target.toFile());
    }

    /**
     * 썸네일 생성
     */
    public static void thumbnail(Path source, Path target, int size) throws IOException {
        Thumbnails.of(source.toFile())
                .size(size, size)
                .keepAspectRatio(true)
                .toFile(target.toFile());
    }

    /**
     * 이미지 포맷 변환
     */
    public static void convert(Path source, Path target, String format) throws IOException {
        Thumbnails.of(source.toFile())
                .scale(1.0)
                .outputFormat(format)
                .toFile(target.toFile());
    }

    /**
     * 이미지 품질 조정 (JPEG)
     */
    public static void compress(Path source, Path target, float quality) throws IOException {
        Thumbnails.of(source.toFile())
                .scale(1.0)
                .outputQuality(quality)
                .toFile(target.toFile());
    }

    /**
     * 이미지 정보 조회
     */
    public static ImageInfo getInfo(Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        return new ImageInfo(
                image.getWidth(),
                image.getHeight(),
                getFormat(path.toString())
        );
    }

    /**
     * 바이트 배열을 BufferedImage로 변환
     */
    public static BufferedImage toBufferedImage(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(bis);
        }
    }

    /**
     * BufferedImage를 바이트 배열로 변환
     */
    public static byte[] toBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, bos);
            return bos.toByteArray();
        }
    }

    private static BufferedImage addTextWatermark(BufferedImage original, String text, float opacity) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage watermarked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarked.createGraphics();

        g2d.drawImage(original, 0, 0, null);

        // 워터마크 설정
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g2d.setComposite(alphaChannel);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(width / 20, 20)));

        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        // 우하단 위치
        int x = width - textWidth - 20;
        int y = height - textHeight;

        g2d.drawString(text, x, y);
        g2d.dispose();

        return watermarked;
    }

    private static String getFormat(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return "jpg";
        }
        String ext = filename.substring(lastDot + 1).toLowerCase();
        return switch (ext) {
            case "jpeg" -> "jpg";
            case "png", "gif", "bmp" -> ext;
            default -> "jpg";
        };
    }

    /**
     * 이미지 정보
     */
    public record ImageInfo(int width, int height, String format) {
    }
}
