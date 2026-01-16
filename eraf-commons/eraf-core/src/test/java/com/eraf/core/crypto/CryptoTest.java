package com.eraf.core.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Crypto - AES-256-GCM 암호화 테스트")
class CryptoTest {

    private static final String TEST_KEY = "12345678901234567890123456789012"; // 32 bytes
    private static final String TEST_DATA = "Hello, ERAF!";

    @Nested
    @DisplayName("문자열 암호화/복호화")
    class StringEncryption {

        @Test
        @DisplayName("정상 암호화 및 복호화")
        void encryptAndDecrypt() {
            // given
            String plainText = TEST_DATA;

            // when
            String encrypted = Crypto.encrypt(plainText, TEST_KEY);
            String decrypted = Crypto.decrypt(encrypted, TEST_KEY);

            // then
            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("동일 데이터 암호화 시 매번 다른 결과 (IV 랜덤)")
        void differentCiphertextEachTime() {
            // given
            String plainText = TEST_DATA;

            // when
            String encrypted1 = Crypto.encrypt(plainText, TEST_KEY);
            String encrypted2 = Crypto.encrypt(plainText, TEST_KEY);

            // then
            assertNotEquals(encrypted1, encrypted2);

            // 복호화 결과는 동일
            assertEquals(Crypto.decrypt(encrypted1, TEST_KEY), Crypto.decrypt(encrypted2, TEST_KEY));
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void nullInput() {
            assertNull(Crypto.encrypt(null, TEST_KEY));
            assertNull(Crypto.decrypt(null, TEST_KEY));
        }

        @Test
        @DisplayName("빈 문자열 암호화")
        void emptyString() {
            String encrypted = Crypto.encrypt("", TEST_KEY);
            String decrypted = Crypto.decrypt(encrypted, TEST_KEY);

            assertNotNull(encrypted);
            assertEquals("", decrypted);
        }

        @Test
        @DisplayName("한글 암호화")
        void koreanText() {
            String plainText = "안녕하세요, ERAF 공통모듈입니다!";

            String encrypted = Crypto.encrypt(plainText, TEST_KEY);
            String decrypted = Crypto.decrypt(encrypted, TEST_KEY);

            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("긴 텍스트 암호화")
        void longText() {
            String plainText = "A".repeat(10000);

            String encrypted = Crypto.encrypt(plainText, TEST_KEY);
            String decrypted = Crypto.decrypt(encrypted, TEST_KEY);

            assertEquals(plainText, decrypted);
        }

        @Test
        @DisplayName("잘못된 키로 복호화 시 예외 발생")
        void wrongKeyDecryption() {
            String encrypted = Crypto.encrypt(TEST_DATA, TEST_KEY);
            String wrongKey = "wrong-key-12345678901234567890ab";

            assertThrows(CryptoException.class, () -> Crypto.decrypt(encrypted, wrongKey));
        }

        @Test
        @DisplayName("잘못된 암호문 복호화 시 예외 발생")
        void invalidCiphertext() {
            assertThrows(CryptoException.class, () -> Crypto.decrypt("invalid-data", TEST_KEY));
        }
    }

    @Nested
    @DisplayName("바이트 배열 암호화/복호화")
    class ByteEncryption {

        @Test
        @DisplayName("바이트 배열 암호화 및 복호화")
        void encryptAndDecryptBytes() {
            byte[] data = TEST_DATA.getBytes();

            byte[] encrypted = Crypto.encryptBytes(data, TEST_KEY);
            byte[] decrypted = Crypto.decryptBytes(encrypted, TEST_KEY);

            assertArrayEquals(data, decrypted);
        }

        @Test
        @DisplayName("null 바이트 배열 입력 시 null 반환")
        void nullBytes() {
            assertNull(Crypto.encryptBytes(null, TEST_KEY));
            assertNull(Crypto.decryptBytes(null, TEST_KEY));
        }
    }

    @Nested
    @DisplayName("키 생성")
    class KeyGeneration {

        @Test
        @DisplayName("랜덤 키 생성")
        void generateKey() {
            String key1 = Crypto.generateKey();
            String key2 = Crypto.generateKey();

            assertNotNull(key1);
            assertNotNull(key2);
            assertNotEquals(key1, key2);

            // 생성된 키로 암호화/복호화 테스트
            String encrypted = Crypto.encrypt(TEST_DATA, key1);
            String decrypted = Crypto.decrypt(encrypted, key1);
            assertEquals(TEST_DATA, decrypted);
        }
    }

    @Nested
    @DisplayName("키 정규화")
    class KeyNormalization {

        @Test
        @DisplayName("32바이트 미만 키 사용")
        void shortKey() {
            String shortKey = "short-key";

            String encrypted = Crypto.encrypt(TEST_DATA, shortKey);
            String decrypted = Crypto.decrypt(encrypted, shortKey);

            assertEquals(TEST_DATA, decrypted);
        }

        @Test
        @DisplayName("32바이트 초과 키 사용")
        void longKey() {
            String longKey = "this-is-a-very-long-key-that-exceeds-32-bytes";

            String encrypted = Crypto.encrypt(TEST_DATA, longKey);
            String decrypted = Crypto.decrypt(encrypted, longKey);

            assertEquals(TEST_DATA, decrypted);
        }

        @Test
        @DisplayName("Base64 인코딩된 키 사용")
        void base64Key() {
            String base64Key = Crypto.generateKey(); // Base64 인코딩된 키

            String encrypted = Crypto.encrypt(TEST_DATA, base64Key);
            String decrypted = Crypto.decrypt(encrypted, base64Key);

            assertEquals(TEST_DATA, decrypted);
        }
    }
}
