package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base64Utils - Base64 인코딩/디코딩")
class Base64UtilsTest {

    @Nested
    @DisplayName("기본 Base64 인코딩")
    class BasicEncode {

        @Test
        @DisplayName("바이트 배열 인코딩")
        void encodeBytes() {
            String encoded = Base64Utils.encode("Hello".getBytes(StandardCharsets.UTF_8));
            assertEquals("SGVsbG8=", encoded);
        }

        @Test
        @DisplayName("문자열 인코딩")
        void encodeString() {
            assertEquals("SGVsbG8=", Base64Utils.encode("Hello"));
        }

        @Test
        @DisplayName("문자열 인코딩 (Charset)")
        void encodeStringWithCharset() {
            String encoded = Base64Utils.encode("Hello", StandardCharsets.UTF_8);
            assertEquals("SGVsbG8=", encoded);
        }

        @Test
        @DisplayName("null/empty 처리")
        void encodeNullEmpty() {
            assertNull(Base64Utils.encode((byte[]) null));
            assertNull(Base64Utils.encode(new byte[0]));
            assertNull(Base64Utils.encode((String) null));
            assertNull(Base64Utils.encode(""));
        }
    }

    @Nested
    @DisplayName("기본 Base64 디코딩")
    class BasicDecode {

        @Test
        @DisplayName("바이트 배열 디코딩")
        void decodeToBytes() {
            byte[] decoded = Base64Utils.decode("SGVsbG8=");
            assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), decoded);
        }

        @Test
        @DisplayName("문자열 디코딩")
        void decodeToString() {
            assertEquals("Hello", Base64Utils.decodeToString("SGVsbG8="));
        }

        @Test
        @DisplayName("문자열 디코딩 (Charset)")
        void decodeToStringWithCharset() {
            assertEquals("Hello", Base64Utils.decodeToString("SGVsbG8=", StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("null/empty 처리")
        void decodeNullEmpty() {
            assertNull(Base64Utils.decode(null));
            assertNull(Base64Utils.decode(""));
            assertNull(Base64Utils.decodeToString(null));
        }

        @Test
        @DisplayName("잘못된 Base64 문자열")
        void decodeInvalid() {
            assertNull(Base64Utils.decode("!!!invalid!!!"));
        }
    }

    @Nested
    @DisplayName("URL-Safe Base64")
    class UrlSafe {

        @Test
        @DisplayName("URL-Safe 인코딩")
        void encodeUrlSafe() {
            String data = "Hello+World/Test";
            String encoded = Base64Utils.encodeUrlSafe(data);
            assertNotNull(encoded);
            assertFalse(encoded.contains("+"));
            assertFalse(encoded.contains("/"));
        }

        @Test
        @DisplayName("URL-Safe 인코딩 (바이트)")
        void encodeUrlSafeBytes() {
            String encoded = Base64Utils.encodeUrlSafe("Hello".getBytes(StandardCharsets.UTF_8));
            assertNotNull(encoded);
        }

        @Test
        @DisplayName("URL-Safe 패딩 없이 인코딩")
        void encodeUrlSafeWithoutPadding() {
            String encoded = Base64Utils.encodeUrlSafeWithoutPadding("Hello");
            assertNotNull(encoded);
            assertFalse(encoded.contains("="));
        }

        @Test
        @DisplayName("URL-Safe 패딩 없이 (바이트)")
        void encodeUrlSafeWithoutPaddingBytes() {
            String encoded = Base64Utils.encodeUrlSafeWithoutPadding("Hi".getBytes(StandardCharsets.UTF_8));
            assertNotNull(encoded);
            assertFalse(encoded.contains("="));
        }

        @Test
        @DisplayName("URL-Safe 디코딩")
        void decodeUrlSafe() {
            String encoded = Base64Utils.encodeUrlSafe("Hello World");
            String decoded = Base64Utils.decodeUrlSafeToString(encoded);
            assertEquals("Hello World", decoded);
        }

        @Test
        @DisplayName("URL-Safe 디코딩 바이트")
        void decodeUrlSafeBytes() {
            byte[] decoded = Base64Utils.decodeUrlSafe(Base64Utils.encodeUrlSafe("test".getBytes()));
            assertNotNull(decoded);
        }

        @Test
        @DisplayName("URL-Safe null/empty")
        void urlSafeNullEmpty() {
            assertNull(Base64Utils.encodeUrlSafe((byte[]) null));
            assertNull(Base64Utils.encodeUrlSafe(new byte[0]));
            assertNull(Base64Utils.encodeUrlSafe((String) null));
            assertNull(Base64Utils.encodeUrlSafe(""));
            assertNull(Base64Utils.encodeUrlSafeWithoutPadding((byte[]) null));
            assertNull(Base64Utils.encodeUrlSafeWithoutPadding((String) null));
            assertNull(Base64Utils.decodeUrlSafe(null));
            assertNull(Base64Utils.decodeUrlSafe(""));
        }

        @Test
        @DisplayName("URL-Safe 잘못된 입력 디코딩")
        void decodeUrlSafeInvalid() {
            assertNull(Base64Utils.decodeUrlSafe("!!!invalid!!!"));
        }
    }

    @Nested
    @DisplayName("MIME Base64")
    class Mime {

        @Test
        @DisplayName("MIME 인코딩 (바이트)")
        void encodeMimeBytes() {
            byte[] data = new byte[200];
            String encoded = Base64Utils.encodeMime(data);
            assertNotNull(encoded);
        }

        @Test
        @DisplayName("MIME 인코딩 (문자열)")
        void encodeMimeString() {
            String encoded = Base64Utils.encodeMime("Hello World");
            assertNotNull(encoded);
        }

        @Test
        @DisplayName("MIME 디코딩")
        void decodeMime() {
            String encoded = Base64Utils.encodeMime("Hello World");
            String decoded = Base64Utils.decodeMimeToString(encoded);
            assertEquals("Hello World", decoded);
        }

        @Test
        @DisplayName("MIME 디코딩 (바이트)")
        void decodeMimeBytes() {
            byte[] decoded = Base64Utils.decodeMime(Base64Utils.encodeMime("test"));
            assertNotNull(decoded);
        }

        @Test
        @DisplayName("MIME null/empty")
        void mimeNullEmpty() {
            assertNull(Base64Utils.encodeMime((byte[]) null));
            assertNull(Base64Utils.encodeMime(new byte[0]));
            assertNull(Base64Utils.encodeMime((String) null));
            assertNull(Base64Utils.encodeMime(""));
            assertNull(Base64Utils.decodeMime(null));
            assertNull(Base64Utils.decodeMime(""));
        }

        @Test
        @DisplayName("MIME 디코더는 잘못된 문자를 무시하고 디코딩")
        void decodeMimeLenient() {
            // MIME decoder is lenient - ignores invalid characters
            byte[] result = Base64Utils.decodeMime("!!!invalid!!!");
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("검증")
    class Validation {

        @Test
        @DisplayName("isBase64")
        void isBase64() {
            assertTrue(Base64Utils.isBase64("SGVsbG8="));
            assertTrue(Base64Utils.isBase64("dGVzdA=="));
            assertFalse(Base64Utils.isBase64(null));
            assertFalse(Base64Utils.isBase64(""));
            assertFalse(Base64Utils.isBase64("!!!not-base64!!!"));
        }

        @Test
        @DisplayName("isUrlSafeBase64")
        void isUrlSafeBase64() {
            String encoded = Base64Utils.encodeUrlSafe("test");
            assertTrue(Base64Utils.isUrlSafeBase64(encoded));
            assertFalse(Base64Utils.isUrlSafeBase64(null));
            assertFalse(Base64Utils.isUrlSafeBase64(""));
        }
    }

    @Nested
    @DisplayName("이미지 처리")
    class ImageProcessing {

        @Test
        @DisplayName("toDataUrl")
        void toDataUrl() {
            byte[] data = {1, 2, 3};
            String dataUrl = Base64Utils.toDataUrl(data, "image/png");
            assertNotNull(dataUrl);
            assertTrue(dataUrl.startsWith("data:image/png;base64,"));
        }

        @Test
        @DisplayName("toPngDataUrl")
        void toPngDataUrl() {
            byte[] data = {1, 2, 3};
            String dataUrl = Base64Utils.toPngDataUrl(data);
            assertTrue(dataUrl.startsWith("data:image/png;base64,"));
        }

        @Test
        @DisplayName("toJpegDataUrl")
        void toJpegDataUrl() {
            byte[] data = {1, 2, 3};
            String dataUrl = Base64Utils.toJpegDataUrl(data);
            assertTrue(dataUrl.startsWith("data:image/jpeg;base64,"));
        }

        @Test
        @DisplayName("toDataUrl - null 처리")
        void toDataUrlNull() {
            assertNull(Base64Utils.toDataUrl(null, "image/png"));
            assertNull(Base64Utils.toDataUrl(new byte[]{1}, null));
        }

        @Test
        @DisplayName("extractBase64FromDataUrl")
        void extractBase64FromDataUrl() {
            String dataUrl = "data:image/png;base64,AQID";
            assertEquals("AQID", Base64Utils.extractBase64FromDataUrl(dataUrl));
        }

        @Test
        @DisplayName("extractBase64FromDataUrl - null/invalid")
        void extractBase64Invalid() {
            assertNull(Base64Utils.extractBase64FromDataUrl(null));
            assertNull(Base64Utils.extractBase64FromDataUrl("no-base64-here"));
        }

        @Test
        @DisplayName("fromDataUrl")
        void fromDataUrl() {
            byte[] original = {1, 2, 3};
            String dataUrl = Base64Utils.toPngDataUrl(original);
            byte[] result = Base64Utils.fromDataUrl(dataUrl);
            assertArrayEquals(original, result);
        }

        @Test
        @DisplayName("fromDataUrl - null")
        void fromDataUrlNull() {
            assertNull(Base64Utils.fromDataUrl(null));
            assertNull(Base64Utils.fromDataUrl("invalid"));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("calculateEncodedLength")
        void calculateEncodedLength() {
            assertEquals(4, Base64Utils.calculateEncodedLength(1));
            assertEquals(4, Base64Utils.calculateEncodedLength(3));
            assertEquals(8, Base64Utils.calculateEncodedLength(4));
        }

        @Test
        @DisplayName("calculateDecodedLength")
        void calculateDecodedLength() {
            assertEquals(3, Base64Utils.calculateDecodedLength(4));
            assertEquals(6, Base64Utils.calculateDecodedLength(8));
        }

        @Test
        @DisplayName("encodeChunked")
        void encodeChunked() {
            byte[] data = "Hello World Test Data".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64Utils.encodeChunked(data, 5);
            assertNotNull(encoded);
        }

        @Test
        @DisplayName("encodeChunked - null/empty")
        void encodeChunkedNull() {
            assertNull(Base64Utils.encodeChunked(null, 10));
            assertNull(Base64Utils.encodeChunked(new byte[0], 10));
        }

        @Test
        @DisplayName("encodeChunked - 기본 chunkSize")
        void encodeChunkedDefaultSize() {
            byte[] data = "test".getBytes();
            String encoded = Base64Utils.encodeChunked(data, 0);
            assertNotNull(encoded);
        }
    }

    @Test
    @DisplayName("라운드트립 - 인코딩 후 디코딩")
    void roundTrip() {
        String original = "한글 테스트 Hello 123 !@#";
        String encoded = Base64Utils.encode(original);
        String decoded = Base64Utils.decodeToString(encoded);
        assertEquals(original, decoded);
    }
}
