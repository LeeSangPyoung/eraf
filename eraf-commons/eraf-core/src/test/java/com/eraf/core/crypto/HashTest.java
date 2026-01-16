package com.eraf.core.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hash - SHA-256 해시 테스트")
class HashTest {

    @Nested
    @DisplayName("SHA-256 해시 생성")
    class HashGeneration {

        @Test
        @DisplayName("문자열 해시 생성 (Hex)")
        void hashToHex() {
            String data = "Hello, ERAF!";

            String hash = Hash.hash(data);

            assertNotNull(hash);
            assertEquals(64, hash.length()); // SHA-256 = 32 bytes = 64 hex chars
            assertTrue(hash.matches("[0-9a-f]+"));
        }

        @Test
        @DisplayName("문자열 해시 생성 (Base64)")
        void hashToBase64() {
            String data = "Hello, ERAF!";

            String hash = Hash.hashBase64(data);

            assertNotNull(hash);
            assertEquals(44, hash.length()); // SHA-256 Base64 = 44 chars
        }

        @Test
        @DisplayName("동일 입력에 대해 동일 해시 생성")
        void deterministicHash() {
            String data = "test data";

            String hash1 = Hash.hash(data);
            String hash2 = Hash.hash(data);

            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("다른 입력에 대해 다른 해시 생성")
        void differentHashForDifferentInput() {
            String hash1 = Hash.hash("data1");
            String hash2 = Hash.hash("data2");

            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void nullInput() {
            assertNull(Hash.hash(null));
            assertNull(Hash.hashBase64(null));
        }

        @Test
        @DisplayName("빈 문자열 해시")
        void emptyString() {
            String hash = Hash.hash("");

            assertNotNull(hash);
            assertEquals(64, hash.length());
            // SHA-256 of empty string
            assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);
        }

        @Test
        @DisplayName("한글 문자열 해시")
        void koreanString() {
            String data = "안녕하세요";

            String hash = Hash.hash(data);

            assertNotNull(hash);
            assertEquals(64, hash.length());
        }
    }

    @Nested
    @DisplayName("바이트 배열 해시")
    class ByteArrayHash {

        @Test
        @DisplayName("바이트 배열 해시 생성")
        void hashBytes() {
            byte[] data = "Hello, ERAF!".getBytes();

            byte[] hash = Hash.hashBytes(data);

            assertNotNull(hash);
            assertEquals(32, hash.length); // SHA-256 = 32 bytes
        }

        @Test
        @DisplayName("null 바이트 배열 입력 시 null 반환")
        void nullBytes() {
            assertNull(Hash.hashBytes(null));
        }
    }

    @Nested
    @DisplayName("해시 검증")
    class HashVerification {

        @Test
        @DisplayName("올바른 해시 검증 성공")
        void verifyCorrectHash() {
            String data = "test data";
            String hash = Hash.hash(data);

            assertTrue(Hash.verify(data, hash));
        }

        @Test
        @DisplayName("잘못된 해시 검증 실패")
        void verifyWrongHash() {
            String data = "test data";
            String wrongHash = Hash.hash("wrong data");

            assertFalse(Hash.verify(data, wrongHash));
        }

        @Test
        @DisplayName("null 입력 검증 실패")
        void verifyNullInputs() {
            String hash = Hash.hash("data");

            assertFalse(Hash.verify(null, hash));
            assertFalse(Hash.verify("data", null));
            assertFalse(Hash.verify(null, null));
        }

        @Test
        @DisplayName("대소문자 구분 검증")
        void caseSensitiveVerification() {
            String data = "TEST";
            String hash = Hash.hash(data);

            assertTrue(Hash.verify("TEST", hash));
            assertFalse(Hash.verify("test", hash));
        }
    }

    @Nested
    @DisplayName("알려진 해시 값 검증")
    class KnownHashValues {

        @Test
        @DisplayName("알려진 SHA-256 값과 일치")
        void knownSHA256Values() {
            // "hello" SHA-256
            assertEquals(
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                Hash.hash("hello")
            );

            // "world" SHA-256
            assertEquals(
                "486ea46224d1bb4fb680f34f7c9ad96a8f24ec88be73ea8e5a6c65260e9cb8a7",
                Hash.hash("world")
            );
        }
    }
}
