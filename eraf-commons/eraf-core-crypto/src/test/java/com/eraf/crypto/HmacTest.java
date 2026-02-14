package com.eraf.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hmac - HMAC-SHA256 서명")
class HmacTest {

    private static final String KEY = "my-secret-key";
    private static final String DATA = "hello world";

    @Test
    @DisplayName("sign - Hex 서명 생성")
    void sign() {
        String signature = Hmac.sign(DATA, KEY);
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertEquals(64, signature.length()); // SHA-256 = 32 bytes = 64 hex chars
    }

    @Test
    @DisplayName("sign - 동일 입력에 동일 결과")
    void signDeterministic() {
        String sig1 = Hmac.sign(DATA, KEY);
        String sig2 = Hmac.sign(DATA, KEY);
        assertEquals(sig1, sig2);
    }

    @Test
    @DisplayName("sign - 다른 데이터 → 다른 서명")
    void signDifferentData() {
        String sig1 = Hmac.sign("data1", KEY);
        String sig2 = Hmac.sign("data2", KEY);
        assertNotEquals(sig1, sig2);
    }

    @Test
    @DisplayName("sign - 다른 키 → 다른 서명")
    void signDifferentKey() {
        String sig1 = Hmac.sign(DATA, "key1");
        String sig2 = Hmac.sign(DATA, "key2");
        assertNotEquals(sig1, sig2);
    }

    @Test
    @DisplayName("sign - null 데이터 시 예외")
    void signNullData() {
        assertThrows(IllegalArgumentException.class, () -> Hmac.sign(null, KEY));
    }

    @Test
    @DisplayName("sign - null 키 시 예외")
    void signNullKey() {
        assertThrows(IllegalArgumentException.class, () -> Hmac.sign(DATA, null));
    }

    @Test
    @DisplayName("signBase64 - Base64 서명 생성")
    void signBase64() {
        String signature = Hmac.signBase64(DATA, KEY);
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    @DisplayName("signBase64 - null 데이터 시 예외")
    void signBase64NullData() {
        assertThrows(IllegalArgumentException.class, () -> Hmac.signBase64(null, KEY));
    }

    @Test
    @DisplayName("verify - 유효한 서명")
    void verifyValid() {
        String signature = Hmac.sign(DATA, KEY);
        assertTrue(Hmac.verify(DATA, signature, KEY));
    }

    @Test
    @DisplayName("verify - 잘못된 서명")
    void verifyInvalid() {
        assertFalse(Hmac.verify(DATA, "invalid-signature", KEY));
    }

    @Test
    @DisplayName("verify - null 입력 시 false")
    void verifyNull() {
        assertFalse(Hmac.verify(null, "sig", KEY));
        assertFalse(Hmac.verify(DATA, null, KEY));
        assertFalse(Hmac.verify(DATA, "sig", null));
    }

    @Test
    @DisplayName("verifyBase64 - 유효한 서명")
    void verifyBase64Valid() {
        String signature = Hmac.signBase64(DATA, KEY);
        assertTrue(Hmac.verifyBase64(DATA, signature, KEY));
    }

    @Test
    @DisplayName("verifyBase64 - 잘못된 서명")
    void verifyBase64Invalid() {
        assertFalse(Hmac.verifyBase64(DATA, "invalid", KEY));
    }

    @Test
    @DisplayName("verifyBase64 - null 입력 시 false")
    void verifyBase64Null() {
        assertFalse(Hmac.verifyBase64(null, "sig", KEY));
        assertFalse(Hmac.verifyBase64(DATA, null, KEY));
        assertFalse(Hmac.verifyBase64(DATA, "sig", null));
    }
}
