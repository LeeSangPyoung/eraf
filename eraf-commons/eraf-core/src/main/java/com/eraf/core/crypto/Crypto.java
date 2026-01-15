package com.eraf.core.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 대칭키 암호화 (AES-256-GCM 고정)
 */
public final class Crypto {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 32; // 256 bits

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Crypto() {
    }

    /**
     * 문자열 암호화
     *
     * @param plainText 평문
     * @param key       암호화 키 (32바이트)
     * @return Base64 인코딩된 암호문
     */
    public static String encrypt(String plainText, String key) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // IV 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            // 암호화
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + CipherText 결합
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new CryptoException("암호화 실패", e);
        }
    }

    /**
     * 문자열 복호화
     *
     * @param cipherText Base64 인코딩된 암호문
     * @param key        암호화 키
     * @return 평문
     */
    public static String decrypt(String cipherText, String key) {
        if (cipherText == null) {
            return null;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] decoded = Base64.getDecoder().decode(cipherText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // IV 추출
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            // CipherText 추출
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            // 복호화
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherBytes);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("복호화 실패", e);
        }
    }

    /**
     * 바이트 배열 암호화
     */
    public static byte[] encryptBytes(byte[] data, String key) {
        if (data == null) {
            return null;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(data);

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return byteBuffer.array();
        } catch (Exception e) {
            throw new CryptoException("암호화 실패", e);
        }
    }

    /**
     * 바이트 배열 복호화
     */
    public static byte[] decryptBytes(byte[] data, String key) {
        if (data == null) {
            return null;
        }
        try {
            byte[] keyBytes = normalizeKey(key);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            return cipher.doFinal(cipherBytes);
        } catch (Exception e) {
            throw new CryptoException("복호화 실패", e);
        }
    }

    /**
     * 랜덤 키 생성 (Base64 인코딩)
     */
    public static String generateKey() {
        byte[] key = new byte[KEY_LENGTH];
        SECURE_RANDOM.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 키를 32바이트로 정규화
     */
    private static byte[] normalizeKey(String key) {
        byte[] keyBytes;
        try {
            // Base64로 먼저 시도
            keyBytes = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            // 일반 문자열로 처리
            keyBytes = key.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length == KEY_LENGTH) {
            return keyBytes;
        }

        // 32바이트로 조정
        byte[] normalized = new byte[KEY_LENGTH];
        if (keyBytes.length > KEY_LENGTH) {
            System.arraycopy(keyBytes, 0, normalized, 0, KEY_LENGTH);
        } else {
            System.arraycopy(keyBytes, 0, normalized, 0, keyBytes.length);
            // 나머지는 0으로 패딩
        }
        return normalized;
    }
}
