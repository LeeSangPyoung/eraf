package com.eraf.core.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * QR코드 생성기
 */
public final class QRCodeGenerator {

    private QRCodeGenerator() {
    }

    /**
     * QR코드 생성 (기본 설정)
     */
    public static void generate(String content, Path outputPath, int size) throws IOException {
        generate(content, outputPath, size, ErrorCorrectionLevel.M);
    }

    /**
     * QR코드 생성 (에러 보정 레벨 지정)
     *
     * @param content               내용
     * @param outputPath            출력 경로
     * @param size                  크기 (가로=세로)
     * @param errorCorrectionLevel  L(7%), M(15%), Q(25%), H(30%)
     */
    public static void generate(String content, Path outputPath, int size, ErrorCorrectionLevel errorCorrectionLevel) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = createHints(errorCorrectionLevel);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
        } catch (WriterException e) {
            throw new IOException("QR코드 생성 실패", e);
        }
    }

    /**
     * QR코드를 바이트 배열로 생성
     */
    public static byte[] generateToBytes(String content, int size) throws IOException {
        return generateToBytes(content, size, ErrorCorrectionLevel.M, "PNG");
    }

    /**
     * QR코드를 바이트 배열로 생성 (상세 옵션)
     */
    public static byte[] generateToBytes(String content, int size, ErrorCorrectionLevel errorCorrectionLevel, String imageFormat) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = createHints(errorCorrectionLevel);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, baos);
                return baos.toByteArray();
            }
        } catch (WriterException e) {
            throw new IOException("QR코드 생성 실패", e);
        }
    }

    /**
     * OutputStream으로 QR코드 출력
     */
    public static void generate(String content, OutputStream outputStream, int size, String imageFormat) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = createHints(ErrorCorrectionLevel.M);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, outputStream);
        } catch (WriterException e) {
            throw new IOException("QR코드 생성 실패", e);
        }
    }

    /**
     * URL QR코드 생성
     */
    public static void generateUrl(String url, Path outputPath, int size) throws IOException {
        generate(url, outputPath, size, ErrorCorrectionLevel.M);
    }

    /**
     * vCard QR코드 생성 (연락처)
     */
    public static void generateVCard(String name, String phone, String email, Path outputPath, int size) throws IOException {
        String vcard = String.format("""
                BEGIN:VCARD
                VERSION:3.0
                N:%s
                TEL:%s
                EMAIL:%s
                END:VCARD
                """, name, phone, email);
        generate(vcard, outputPath, size, ErrorCorrectionLevel.M);
    }

    /**
     * WiFi 설정 QR코드 생성
     */
    public static void generateWifi(String ssid, String password, String encryption, Path outputPath, int size) throws IOException {
        // encryption: WPA, WEP, nopass
        String wifi = String.format("WIFI:T:%s;S:%s;P:%s;;", encryption, ssid, password);
        generate(wifi, outputPath, size, ErrorCorrectionLevel.M);
    }

    private static Map<EncodeHintType, Object> createHints(ErrorCorrectionLevel level) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, level);
        hints.put(EncodeHintType.MARGIN, 1);
        return hints;
    }
}
