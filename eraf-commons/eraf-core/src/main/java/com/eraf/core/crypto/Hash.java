package com.eraf.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 해시 유틸리티 (SHA-256 고정)
 */
public final class Hash {

    private static final String ALGORITHM = "SHA-256";

    private Hash() {
    }

    /**
     * SHA-256 해시 (Hex 출력)
     */
    public static String hash(String data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("해시 알고리즘을 찾을 수 없습니다", e);
        }
    }

    /**
     * SHA-256 해시 (Base64 출력)
     */
    public static String hashBase64(String data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("해시 알고리즘을 찾을 수 없습니다", e);
        }
    }

    /**
     * 바이트 배열 해시
     */
    public static byte[] hashBytes(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("해시 알고리즘을 찾을 수 없습니다", e);
        }
    }

    /**
     * 해시 값 비교
     */
    public static boolean verify(String data, String expectedHash) {
        if (data == null || expectedHash == null) {
            return false;
        }
        String actualHash = hash(data);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
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
