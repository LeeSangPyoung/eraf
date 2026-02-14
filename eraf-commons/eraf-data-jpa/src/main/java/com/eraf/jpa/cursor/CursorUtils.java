package com.eraf.jpa.cursor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 커서 인코딩/디코딩 유틸리티
 */
public class CursorUtils {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /**
     * Long 타입 커서 인코딩
     */
    public static String encodeLong(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return ENCODER.encodeToString(cursor.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Long 타입 커서 디코딩
     */
    public static Long decodeLong(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(DECODER.decode(encodedCursor), StandardCharsets.UTF_8);
            return Long.parseLong(decoded);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format", e);
        }
    }

    /**
     * String 타입 커서 인코딩
     */
    public static String encodeString(String cursor) {
        if (cursor == null) {
            return null;
        }
        return ENCODER.encodeToString(cursor.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * String 타입 커서 디코딩
     */
    public static String decodeString(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return null;
        }
        try {
            return new String(DECODER.decode(encodedCursor), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format", e);
        }
    }

    /**
     * Instant 타입 커서 인코딩 (타임스탬프 밀리초 사용)
     */
    public static String encodeInstant(Instant cursor) {
        if (cursor == null) {
            return null;
        }
        long millis = cursor.toEpochMilli();
        return ENCODER.encodeToString(String.valueOf(millis).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Instant 타입 커서 디코딩
     */
    public static Instant decodeInstant(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(DECODER.decode(encodedCursor), StandardCharsets.UTF_8);
            long millis = Long.parseLong(decoded);
            return Instant.ofEpochMilli(millis);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format", e);
        }
    }

    /**
     * 복합 커서 인코딩 (예: "id:timestamp" 형식)
     */
    public static String encodeComposite(Object... parts) {
        if (parts == null || parts.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(":");
            }
            sb.append(parts[i]);
        }
        return ENCODER.encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 복합 커서 디코딩
     */
    public static String[] decodeComposite(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(DECODER.decode(encodedCursor), StandardCharsets.UTF_8);
            return decoded.split(":");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format", e);
        }
    }
}
