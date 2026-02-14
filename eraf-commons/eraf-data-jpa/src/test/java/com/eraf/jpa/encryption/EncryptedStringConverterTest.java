package com.eraf.jpa.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EncryptedStringConverter 테스트
 */
class EncryptedStringConverterTest {

    private EncryptedStringConverter converter;

    @BeforeEach
    void setUp() {
        // 테스트용 암호화 키 설정
        System.setProperty("eraf.encryption.key", "TEST_ENCRYPTION_KEY_FOR_UNIT_TEST");
        converter = new EncryptedStringConverter();
    }

    @Test
    void convertToDatabaseColumn_ShouldEncryptPlainText() {
        // given
        String plainText = "sensitive-data-123";

        // when
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted.length()).isGreaterThan(plainText.length());
    }

    @Test
    void convertToEntityAttribute_ShouldDecryptCipherText() {
        // given
        String plainText = "sensitive-data-123";
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // when
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encryptDecrypt_ShouldBeReversible() {
        // given
        String[] testValues = {
                "주민번호 123456-1234567",
                "카드번호 1234-5678-9012-3456",
                "email@example.com",
                "한글 암호화 테스트",
                "!@#$%^&*()_+-=[]{}|;:',.<>?/`~",
                "very long text ".repeat(100)
        };

        for (String original : testValues) {
            // when
            String encrypted = converter.convertToDatabaseColumn(original);
            String decrypted = converter.convertToEntityAttribute(encrypted);

            // then
            assertThat(decrypted).isEqualTo(original);
            assertThat(encrypted).isNotEqualTo(original);
        }
    }

    @Test
    void convertToDatabaseColumn_WithNull_ShouldReturnNull() {
        // when
        String result = converter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_WithNull_ShouldReturnNull() {
        // when
        String result = converter.convertToEntityAttribute(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    void encryptTwice_ShouldProduceDifferentResults() {
        // given
        String plainText = "same-input";

        // when (GCM 모드는 랜덤 IV 사용하므로 매번 다른 암호문 생성)
        String encrypted1 = converter.convertToDatabaseColumn(plainText);
        String encrypted2 = converter.convertToDatabaseColumn(plainText);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);

        // but both decrypt to same value
        String decrypted1 = converter.convertToEntityAttribute(encrypted1);
        String decrypted2 = converter.convertToEntityAttribute(encrypted2);
        assertThat(decrypted1).isEqualTo(plainText);
        assertThat(decrypted2).isEqualTo(plainText);
    }

    @Test
    void convertToEntityAttribute_WithWrongKey_ShouldThrowException() {
        // given - EncryptionKeyManager는 싱글톤이므로 키 교체를 위해 직접 변경
        EncryptionKeyManager keyManager = EncryptionKeyManager.getInstance();

        // 현재 키로 암호화
        String plainText = "sensitive-data";
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // 기존 키를 완전히 다른 키로 교체 (v1만 변경하고, fallback도 실패하도록)
        String originalKey = System.getProperty("eraf.encryption.key", "TEST_ENCRYPTION_KEY_FOR_UNIT_TEST");
        keyManager.setKeyForTesting(1, "COMPLETELY_DIFFERENT_WRONG_KEY_123456");

        try {
            // when & then - 잘못된 키로는 복호화 실패
            assertThatThrownBy(() -> converter.convertToEntityAttribute(encrypted))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            // 원래 키 복원
            keyManager.setKeyForTesting(1, originalKey);
        }
    }

    @Test
    void convertToEntityAttribute_WithCorruptedData_ShouldThrowException() {
        // given
        String corruptedData = "this-is-not-valid-encrypted-data";

        // when & then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(corruptedData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to decrypt sensitive data");
    }

    @Test
    void convertToDatabaseColumn_WithEmptyString_ShouldEncrypt() {
        // given
        String emptyString = "";

        // when
        String encrypted = converter.convertToDatabaseColumn(emptyString);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEmpty();

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertThat(decrypted).isEqualTo(emptyString);
    }

    @Test
    void encryptDecrypt_WithSpecialCharacters_ShouldWork() {
        // given
        String specialChars = "특수문자: 🔐🔑🛡️ \n\t\r\0";

        // when
        String encrypted = converter.convertToDatabaseColumn(specialChars);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(decrypted).isEqualTo(specialChars);
    }

    @Test
    void encryptDecrypt_WithVeryLongText_ShouldWork() {
        // given
        String longText = "A".repeat(10000);

        // when
        String encrypted = converter.convertToDatabaseColumn(longText);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // then
        assertThat(decrypted).isEqualTo(longText);
        assertThat(encrypted.length()).isGreaterThan(longText.length());
    }
}
