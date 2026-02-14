package com.eraf.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Signature - RSA 비대칭 서명/암호화")
class SignatureTest {

    private static String publicKey;
    private static String privateKey;

    @BeforeAll
    static void setUp() {
        KeyPair keyPair = Signature.generateKeyPair();
        publicKey = Signature.encodePublicKey(keyPair.getPublic());
        privateKey = Signature.encodePrivateKey(keyPair.getPrivate());
    }

    @Test
    @DisplayName("generateKeyPair - 키 쌍 생성")
    void generateKeyPair() {
        KeyPair keyPair = Signature.generateKeyPair();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }

    @Test
    @DisplayName("sign + verify - 서명 생성 및 검증")
    void signAndVerify() {
        String data = "hello world";
        String signature = Signature.sign(data, privateKey);
        assertNotNull(signature);

        assertTrue(Signature.verify(data, signature, publicKey));
    }

    @Test
    @DisplayName("verify - 잘못된 데이터로 검증 실패")
    void verifyWrongData() {
        String signature = Signature.sign("original", privateKey);
        assertFalse(Signature.verify("tampered", signature, publicKey));
    }

    @Test
    @DisplayName("verify - 잘못된 서명으로 검증 실패")
    void verifyWrongSignature() {
        assertFalse(Signature.verify("data", "invalid-sig", publicKey));
    }

    @Test
    @DisplayName("verify - null 입력 시 false")
    void verifyNull() {
        assertFalse(Signature.verify(null, "sig", publicKey));
        assertFalse(Signature.verify("data", null, publicKey));
        assertFalse(Signature.verify("data", "sig", null));
    }

    @Test
    @DisplayName("sign - null 데이터 시 예외")
    void signNullData() {
        assertThrows(IllegalArgumentException.class, () -> Signature.sign(null, privateKey));
    }

    @Test
    @DisplayName("sign - null 키 시 예외")
    void signNullKey() {
        assertThrows(IllegalArgumentException.class, () -> Signature.sign("data", null));
    }

    @Test
    @DisplayName("encrypt + decrypt - RSA 암호화/복호화")
    void encryptAndDecrypt() {
        String data = "secret message";
        String encrypted = Signature.encrypt(data, publicKey);
        assertNotNull(encrypted);
        assertNotEquals(data, encrypted);

        String decrypted = Signature.decrypt(encrypted, privateKey);
        assertEquals(data, decrypted);
    }

    @Test
    @DisplayName("encrypt - null 데이터 시 예외")
    void encryptNullData() {
        assertThrows(IllegalArgumentException.class, () -> Signature.encrypt(null, publicKey));
    }

    @Test
    @DisplayName("decrypt - null 암호문 시 예외")
    void decryptNullData() {
        assertThrows(IllegalArgumentException.class, () -> Signature.decrypt(null, privateKey));
    }

    @Test
    @DisplayName("encodePublicKey / decodePublicKey 라운드 트립")
    void publicKeyRoundTrip() {
        KeyPair keyPair = Signature.generateKeyPair();
        String encoded = Signature.encodePublicKey(keyPair.getPublic());
        assertNotNull(Signature.decodePublicKey(encoded));
    }

    @Test
    @DisplayName("encodePrivateKey / decodePrivateKey 라운드 트립")
    void privateKeyRoundTrip() {
        KeyPair keyPair = Signature.generateKeyPair();
        String encoded = Signature.encodePrivateKey(keyPair.getPrivate());
        assertNotNull(Signature.decodePrivateKey(encoded));
    }

    @Test
    @DisplayName("decodePrivateKey - 잘못된 입력 시 CryptoException")
    void decodeInvalidPrivateKey() {
        assertThrows(CryptoException.class, () -> Signature.decodePrivateKey("invalid"));
    }

    @Test
    @DisplayName("decodePublicKey - 잘못된 입력 시 CryptoException")
    void decodeInvalidPublicKey() {
        assertThrows(CryptoException.class, () -> Signature.decodePublicKey("invalid"));
    }
}
