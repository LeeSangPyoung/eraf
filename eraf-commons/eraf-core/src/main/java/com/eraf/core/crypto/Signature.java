package com.eraf.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 비대칭 서명 (RSA-2048 고정)
 */
public final class Signature {

    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    private Signature() {
    }

    /**
     * RSA 키 쌍 생성
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("키 쌍 생성 실패", e);
        }
    }

    /**
     * 서명 생성
     *
     * @param data       서명할 데이터
     * @param privateKey Base64 인코딩된 개인 키
     * @return Base64 인코딩된 서명
     */
    public static String sign(String data, String privateKey) {
        if (data == null || privateKey == null) {
            throw new IllegalArgumentException("데이터와 개인 키는 null일 수 없습니다");
        }
        try {
            PrivateKey key = decodePrivateKey(privateKey);
            java.security.Signature signature = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new CryptoException("서명 생성 실패", e);
        }
    }

    /**
     * 서명 검증
     *
     * @param data      원본 데이터
     * @param signature Base64 인코딩된 서명
     * @param publicKey Base64 인코딩된 공개 키
     * @return 검증 결과
     */
    public static boolean verify(String data, String signature, String publicKey) {
        if (data == null || signature == null || publicKey == null) {
            return false;
        }
        try {
            PublicKey key = decodePublicKey(publicKey);
            java.security.Signature sig = java.security.Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(key);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 개인 키를 Base64 문자열로 인코딩
     */
    public static String encodePrivateKey(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 공개 키를 Base64 문자열로 인코딩
     */
    public static String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Base64 문자열을 개인 키로 디코딩
     */
    public static PrivateKey decodePrivateKey(String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new CryptoException("개인 키 디코딩 실패", e);
        }
    }

    /**
     * Base64 문자열을 공개 키로 디코딩
     */
    public static PublicKey decodePublicKey(String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new CryptoException("공개 키 디코딩 실패", e);
        }
    }

    /**
     * RSA 암호화 (공개 키)
     */
    public static String encrypt(String data, String publicKey) {
        if (data == null || publicKey == null) {
            throw new IllegalArgumentException("데이터와 공개 키는 null일 수 없습니다");
        }
        try {
            PublicKey key = decodePublicKey(publicKey);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CryptoException("RSA 암호화 실패", e);
        }
    }

    /**
     * RSA 복호화 (개인 키)
     */
    public static String decrypt(String encryptedData, String privateKey) {
        if (encryptedData == null || privateKey == null) {
            throw new IllegalArgumentException("암호문과 개인 키는 null일 수 없습니다");
        }
        try {
            PrivateKey key = decodePrivateKey(privateKey);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("RSA 복호화 실패", e);
        }
    }
}
