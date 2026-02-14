package com.eraf.jpa.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EncryptedStringConverter 키 로테이션 테스트
 * 기존 테스트(EncryptedStringConverterTest)는 backward compatibility 확인용
 * 이 테스트는 새로운 키 로테이션 기능 검증용
 */
class EncryptedStringConverterKeyRotationTest {

    private EncryptedStringConverter converter;
    private EncryptionKeyManager keyManager;

    @BeforeEach
    void setUp() {
        keyManager = EncryptionKeyManager.getInstance();
        converter = new EncryptedStringConverter();

        // 테스트용 키 설정
        keyManager.setKeyForTesting(1, "CURRENT_KEY_V1_32_BYTES_LONG__");
        keyManager.setKeyForTesting(0, "OLD_KEY_V0_32_BYTES_LONG______");
    }

    @Test
    @DisplayName("암호화 - v1 버전 prefix 포함")
    void testEncrypt_IncludesV1Prefix() {
        // Given
        String plainText = "sensitive-data";

        // When
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // Then
        assertThat(encrypted).startsWith("v1:");
    }

    @Test
    @DisplayName("복호화 - v1 데이터")
    void testDecrypt_V1Data() {
        // Given
        String plainText = "test-data";
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // When
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 - v0 레거시 데이터 (이전 키)")
    void testDecrypt_V0LegacyData() {
        // Given - v0 키로 암호화된 데이터 생성
        keyManager.setKeyForTesting(1, "OLD_KEY_V0_32_BYTES_LONG______");
        String plainText = "legacy-data";
        String v0Encrypted = keyManager.encrypt(plainText);
        String v0Data = "v0:" + v0Encrypted.substring(3); // v1: → v0:

        // 현재 키 복원
        keyManager.setKeyForTesting(1, "CURRENT_KEY_V1_32_BYTES_LONG__");

        // When - 새 converter로 v0 데이터 복호화
        String decrypted = converter.convertToEntityAttribute(v0Data);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 - 버전 없는 레거시 데이터")
    void testDecrypt_NoVersionLegacyData() {
        // Given - 버전 prefix 없는 데이터 (v0 키로 암호화)
        keyManager.setKeyForTesting(1, "OLD_KEY_V0_32_BYTES_LONG______");
        String plainText = "old-legacy-data";
        String encrypted = keyManager.encrypt(plainText);
        String legacyData = encrypted.substring(3); // "v1:" 제거

        // 현재 키 복원
        keyManager.setKeyForTesting(1, "CURRENT_KEY_V1_32_BYTES_LONG__");
        keyManager.setKeyForTesting(0, "OLD_KEY_V0_32_BYTES_LONG______");

        // When
        String decrypted = converter.convertToEntityAttribute(legacyData);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("키 로테이션 시나리오 - 이전 키 → 현재 키")
    void testKeyRotationScenario() {
        // Step 1: 초기 키(v0)로 데이터 암호화
        keyManager.setKeyForTesting(1, "OLD_KEY_V0_32_BYTES_LONG______");
        EncryptedStringConverter oldConverter = new EncryptedStringConverter();
        String plainText = "user-password";
        String oldEncrypted = oldConverter.convertToDatabaseColumn(plainText); // v1:... (실제로는 OLD_KEY)

        // Step 2: 키 로테이션 (새 키 배포)
        keyManager.setKeyForTesting(1, "NEW_KEY_V1_32_BYTES_LONG______");
        keyManager.setKeyForTesting(0, "OLD_KEY_V0_32_BYTES_LONG______"); // 이전 키 보관
        EncryptedStringConverter newConverter = new EncryptedStringConverter();

        // Step 3: 이전 키로 암호화된 데이터를 v0로 표시
        String v0Data = "v0:" + oldEncrypted.substring(3);

        // Step 4: 새 converter로 v0 데이터 복호화 가능
        String decrypted = newConverter.convertToEntityAttribute(v0Data);
        assertThat(decrypted).isEqualTo(plainText);

        // Step 5: 새로 암호화하면 v1으로 저장
        String newEncrypted = newConverter.convertToDatabaseColumn(plainText);
        assertThat(newEncrypted).startsWith("v1:");

        // Step 6: v1 데이터 복호화 가능
        String newDecrypted = newConverter.convertToEntityAttribute(newEncrypted);
        assertThat(newDecrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("다중 버전 복호화 - v1, v0, 레거시 모두 지원")
    void testMultiVersionDecryption() {
        // Given - 3가지 버전의 데이터
        String plainText = "test-data";

        // v1 데이터 (현재 키)
        keyManager.setKeyForTesting(1, "KEY_V1_32_BYTES_LONG__________");
        String v1Data = keyManager.encrypt(plainText);
        assertThat(v1Data).startsWith("v1:");

        // v0 데이터 (이전 키)
        keyManager.setKeyForTesting(1, "KEY_V0_32_BYTES_LONG__________");
        String v0Encrypted = keyManager.encrypt(plainText);
        String v0Data = "v0:" + v0Encrypted.substring(3);

        // 레거시 데이터 (버전 없음, v0 키 사용)
        String legacyData = v0Encrypted.substring(3);

        // 현재 키 + 이전 키 설정
        keyManager.setKeyForTesting(1, "KEY_V1_32_BYTES_LONG__________");
        keyManager.setKeyForTesting(0, "KEY_V0_32_BYTES_LONG__________");

        // When - 모든 버전 복호화
        String decrypted1 = converter.convertToEntityAttribute(v1Data);
        String decrypted2 = converter.convertToEntityAttribute(v0Data);
        String decrypted3 = converter.convertToEntityAttribute(legacyData);

        // Then
        assertThat(decrypted1).isEqualTo(plainText);
        assertThat(decrypted2).isEqualTo(plainText);
        assertThat(decrypted3).isEqualTo(plainText);
    }

    @Test
    @DisplayName("NULL 값 처리")
    void testNullHandling() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("빈 문자열 암호화")
    void testEmptyString() {
        String encrypted = converter.convertToDatabaseColumn("");
        assertThat(encrypted).startsWith("v1:");

        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertThat(decrypted).isEmpty();
    }

    @Test
    @DisplayName("다양한 문자셋 지원")
    void testVariousCharsets() {
        String[] testData = {
                "한글 암호화",
                "日本語暗号化",
                "中文加密",
                "Emoji 🔐🔑",
                "Special: !@#$%^&*()",
                "Whitespace: \n\t\r"
        };

        for (String plainText : testData) {
            String encrypted = converter.convertToDatabaseColumn(plainText);
            String decrypted = converter.convertToEntityAttribute(encrypted);
            assertThat(decrypted).isEqualTo(plainText);
        }
    }

    @Test
    @DisplayName("매번 다른 암호문 생성 (랜덤 IV)")
    void testRandomIV() {
        String plainText = "same-input";

        String encrypted1 = converter.convertToDatabaseColumn(plainText);
        String encrypted2 = converter.convertToDatabaseColumn(plainText);

        // 같은 평문이라도 다른 암호문 (랜덤 IV)
        assertThat(encrypted1).isNotEqualTo(encrypted2);

        // 하지만 둘 다 정상 복호화
        assertThat(converter.convertToEntityAttribute(encrypted1)).isEqualTo(plainText);
        assertThat(converter.convertToEntityAttribute(encrypted2)).isEqualTo(plainText);
    }
}
