package com.eraf.core.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 바코드 생성기
 */
public final class BarcodeGenerator {

    private BarcodeGenerator() {
    }

    /**
     * Code128 바코드 생성
     */
    public static void generateCode128(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.CODE_128, outputPath, width, height);
    }

    /**
     * Code39 바코드 생성
     */
    public static void generateCode39(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.CODE_39, outputPath, width, height);
    }

    /**
     * EAN-13 바코드 생성 (13자리 숫자)
     */
    public static void generateEan13(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.EAN_13, outputPath, width, height);
    }

    /**
     * EAN-8 바코드 생성 (8자리 숫자)
     */
    public static void generateEan8(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.EAN_8, outputPath, width, height);
    }

    /**
     * UPC-A 바코드 생성 (12자리 숫자)
     */
    public static void generateUpcA(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.UPC_A, outputPath, width, height);
    }

    /**
     * ITF 바코드 생성 (짝수 자릿수 숫자)
     */
    public static void generateItf(String content, Path outputPath, int width, int height) throws IOException {
        generate(content, BarcodeFormat.ITF, outputPath, width, height);
    }

    /**
     * 바코드를 바이트 배열로 생성
     */
    public static byte[] generateCode128ToBytes(String content, int width, int height) throws IOException {
        return generateToBytes(content, BarcodeFormat.CODE_128, width, height, "PNG");
    }

    /**
     * 범용 바코드 생성
     */
    public static void generate(String content, BarcodeFormat format, Path outputPath, int width, int height) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
        } catch (WriterException e) {
            throw new IOException("바코드 생성 실패", e);
        }
    }

    /**
     * 범용 바코드 바이트 배열 생성
     */
    public static byte[] generateToBytes(String content, BarcodeFormat format, int width, int height, String imageFormat) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, baos);
                return baos.toByteArray();
            }
        } catch (WriterException e) {
            throw new IOException("바코드 생성 실패", e);
        }
    }

    /**
     * OutputStream으로 바코드 출력
     */
    public static void generate(String content, BarcodeFormat format, OutputStream outputStream, int width, int height, String imageFormat) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, outputStream);
        } catch (WriterException e) {
            throw new IOException("바코드 생성 실패", e);
        }
    }
}
