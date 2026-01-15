package com.eraf.core.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * HMAC 서명 (HMAC-SHA256 고정)
 */
public final class Hmac {

    private static final String ALGORITHM = "HmacSHA256";

    private Hmac() {
    }

    /**
     * HMAC 서명 생성 (Hex 출력)
     *
     * @param data 데이터
     * @param key  비밀 키
     * @return HMAC 서명 (Hex)
     */
    public static String sign(String data, String key) {
        if (data == null || key == null) {
            throw new IllegalArgumentException("데이터와 키는 null일 수 없습니다");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptoException("HMAC 서명 생성 실패", e);
        }
    }

    /**
     * HMAC 서명 생성 (Base64 출력)
     */
    public static String signBase64(String data, String key) {
        if (data == null || key == null) {
            throw new IllegalArgumentException("데이터와 키는 null일 수 없습니다");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptoException("HMAC 서명 생성 실패", e);
        }
    }

    /**
     * HMAC 서명 검증
     *
     * @param data      원본 데이터
     * @param signature 서명 (Hex)
     * @param key       비밀 키
     * @return 검증 결과
     */
    public static boolean verify(String data, String signature, String key) {
        if (data == null || signature == null || key == null) {
            return false;
        }
        String expectedSignature = sign(data, key);
        return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * HMAC 서명 검증 (Base64)
     */
    public static boolean verifyBase64(String data, String signature, String key) {
        if (data == null || signature == null || key == null) {
            return false;
        }
        String expectedSignature = signBase64(data, key);
        return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 바이트 배열을 Hex 문자열로 변환
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
