package com.eraf.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 인코딩/디코딩 유틸리티
 * 파일 업로드, 이미지 처리, API 통신 등에 필수
 */
public final class Base64Utils {

    private Base64Utils() {
    }

    // ===== 기본 Base64 인코딩 =====

    /**
     * 바이트 배열을 Base64 문자열로 인코딩
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 문자열을 Base64로 인코딩
     */
    public static String encode(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 문자열을 Base64로 인코딩 (Charset 지정)
     */
    public static String encode(String data, Charset charset) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return encode(data.getBytes(charset));
    }

    // ===== 기본 Base64 디코딩 =====

    /**
     * Base64 문자열을 바이트 배열로 디코딩
     */
    public static byte[] decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Base64 문자열을 일반 문자열로 디코딩
     */
    public static String decodeToString(String base64) {
        byte[] decoded = decode(base64);
        return decoded != null ? new String(decoded, StandardCharsets.UTF_8) : null;
    }

    /**
     * Base64 문자열을 일반 문자열로 디코딩 (Charset 지정)
     */
    public static String decodeToString(String base64, Charset charset) {
        byte[] decoded = decode(base64);
        return decoded != null ? new String(decoded, charset) : null;
    }

    // ===== URL-Safe Base64 인코딩 =====

    /**
     * 바이트 배열을 URL-Safe Base64로 인코딩
     * (URL 파라미터에 안전하게 사용 가능: +,/ 대신 -,_ 사용)
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return Base64.getUrlEncoder().encodeToString(data);
    }

    /**
     * 문자열을 URL-Safe Base64로 인코딩
     */
    public static String encodeUrlSafe(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 바이트 배열을 URL-Safe Base64로 인코딩 (패딩 제거)
     */
    public static String encodeUrlSafeWithoutPadding(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * 문자열을 URL-Safe Base64로 인코딩 (패딩 제거)
     */
    public static String encodeUrlSafeWithoutPadding(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return encodeUrlSafeWithoutPadding(data.getBytes(StandardCharsets.UTF_8));
    }

    // ===== URL-Safe Base64 디코딩 =====

    /**
     * URL-Safe Base64 문자열을 바이트 배열로 디코딩
     */
    public static byte[] decodeUrlSafe(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            return Base64.getUrlDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * URL-Safe Base64 문자열을 일반 문자열로 디코딩
     */
    public static String decodeUrlSafeToString(String base64) {
        byte[] decoded = decodeUrlSafe(base64);
        return decoded != null ? new String(decoded, StandardCharsets.UTF_8) : null;
    }

    // ===== MIME Base64 인코딩 (줄바꿈 포함) =====

    /**
     * 바이트 배열을 MIME Base64로 인코딩 (76자마다 줄바꿈)
     */
    public static String encodeMime(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return Base64.getMimeEncoder().encodeToString(data);
    }

    /**
     * 문자열을 MIME Base64로 인코딩
     */
    public static String encodeMime(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return encodeMime(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * MIME Base64 문자열을 바이트 배열로 디코딩
     */
    public static byte[] decodeMime(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            return Base64.getMimeDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * MIME Base64 문자열을 일반 문자열로 디코딩
     */
    public static String decodeMimeToString(String base64) {
        byte[] decoded = decodeMime(base64);
        return decoded != null ? new String(decoded, StandardCharsets.UTF_8) : null;
    }

    // ===== 검증 =====

    /**
     * 유효한 Base64 문자열인지 확인
     */
    public static boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 유효한 URL-Safe Base64 문자열인지 확인
     */
    public static boolean isUrlSafeBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Base64.getUrlDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // ===== 이미지 처리 =====

    /**
     * 이미지 바이트를 Data URL로 변환 (HTML img src에 바로 사용 가능)
     * 예: data:image/png;base64,iVBORw0KG...
     */
    public static String toDataUrl(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || mimeType == null) {
            return null;
        }
        String base64 = encode(imageBytes);
        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * PNG 이미지를 Data URL로 변환
     */
    public static String toPngDataUrl(byte[] imageBytes) {
        return toDataUrl(imageBytes, "image/png");
    }

    /**
     * JPEG 이미지를 Data URL로 변환
     */
    public static String toJpegDataUrl(byte[] imageBytes) {
        return toDataUrl(imageBytes, "image/jpeg");
    }

    /**
     * Data URL에서 Base64 부분만 추출
     */
    public static String extractBase64FromDataUrl(String dataUrl) {
        if (dataUrl == null || !dataUrl.contains("base64,")) {
            return null;
        }
        int index = dataUrl.indexOf("base64,");
        return dataUrl.substring(index + 7);
    }

    /**
     * Data URL을 바이트 배열로 변환
     */
    public static byte[] fromDataUrl(String dataUrl) {
        String base64 = extractBase64FromDataUrl(dataUrl);
        return base64 != null ? decode(base64) : null;
    }

    // ===== 유틸리티 =====

    /**
     * Base64 인코딩 후 길이 계산 (패딩 포함)
     */
    public static int calculateEncodedLength(int dataLength) {
        return ((dataLength + 2) / 3) * 4;
    }

    /**
     * Base64 디코딩 후 대략적인 길이 계산
     */
    public static int calculateDecodedLength(int base64Length) {
        return (base64Length / 4) * 3;
    }

    /**
     * 청크 단위로 Base64 인코딩 (대용량 데이터용)
     */
    public static String encodeChunked(byte[] data, int chunkSize) {
        if (data == null || data.length == 0) {
            return null;
        }
        if (chunkSize <= 0) {
            chunkSize = 1024;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length; i += chunkSize) {
            int end = Math.min(i + chunkSize, data.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(data, i, chunk, 0, end - i);
            result.append(encode(chunk));
        }
        return result.toString();
    }
}
