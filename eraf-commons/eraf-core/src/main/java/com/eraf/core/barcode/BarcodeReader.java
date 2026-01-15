package com.eraf.core.barcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 바코드/QR코드 읽기
 */
public final class BarcodeReader {

    private BarcodeReader() {
    }

    /**
     * 바코드/QR코드 읽기
     */
    public static Optional<String> read(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        return read(image);
    }

    /**
     * InputStream에서 바코드/QR코드 읽기
     */
    public static Optional<String> read(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        return read(image);
    }

    /**
     * BufferedImage에서 바코드/QR코드 읽기
     */
    public static Optional<String> read(BufferedImage image) {
        if (image == null) {
            return Optional.empty();
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return Optional.ofNullable(result.getText());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * QR코드만 읽기
     */
    public static Optional<String> readQRCode(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        return readQRCode(image);
    }

    /**
     * QR코드만 읽기 (BufferedImage)
     */
    public static Optional<String> readQRCode(BufferedImage image) {
        if (image == null) {
            return Optional.empty();
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.List.of(BarcodeFormat.QR_CODE));

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return Optional.ofNullable(result.getText());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * 바코드 읽기 결과 (형식 포함)
     */
    public static Optional<BarcodeResult> readWithFormat(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        return readWithFormat(image);
    }

    /**
     * 바코드 읽기 결과 (형식 포함)
     */
    public static Optional<BarcodeResult> readWithFormat(BufferedImage image) {
        if (image == null) {
            return Optional.empty();
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return Optional.of(new BarcodeResult(result.getText(), result.getBarcodeFormat().name()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * 바코드 읽기 결과
     */
    public record BarcodeResult(String text, String format) {
    }
}
