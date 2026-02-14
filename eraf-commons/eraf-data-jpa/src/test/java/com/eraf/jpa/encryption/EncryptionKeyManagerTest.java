package com.eraf.jpa.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EncryptionKeyManager 테스트
 */
class EncryptionKeyManagerTest {

    private EncryptionKeyManager keyManager;

    @BeforeEach
    void setUp() {
        keyManager = EncryptionKeyManager.getInstance();

        // 테스트용 키 설정 (v1: 현재, v0: 이전)
        keyManager.setKeyForTesting(1, "TEST_KEY_V1_32_BYTES_LONG_KEY_");
        keyManager.setKeyForTesting(0, "TEST_KEY_V0_32_BYTES_LONG_KEY_");
    }

    @Test
    @DisplayName("암호화 - 최신 버전(v1) 사용")
    void testEncrypt() {
        // When
        String encrypted = keyManager.encrypt("sensitive-data");

        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).startsWith("v1:");
        assertThat(encrypted).contains(":");
    }

    @Test
    @DisplayName("암호화/복호화 - 왕복 테스트")
    void testEncryptDecryptRoundTrip() {
        // Given
        String plainText = "my-secret-password";

        // When
        String encrypted = keyManager.encrypt(plainText);
        String decrypted = keyManager.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 - v1 버전")
    void testDecrypt_V1() {
        // Given
        String plainText = "test-data";
        String encrypted = keyManager.encrypt(plainText); // v1:...

        // When
        String decrypted = keyManager.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("복호화 - v0 버전 (이전 키)")
    void testDecrypt_V0() {
        // Given - v0 키로 직접 암호화
        keyManager.setKeyForTesting(1, "OLD_KEY_V0"); // 임시로 v1을 v0로 변경
        String encryptedWithV0 = keyManager.encrypt("old-data");
        String v0Data = "v0:" + encryptedWithV0.substring(3); // v1: → v0:

        // 다시 v1 키 복원
        keyManager.setKeyForTesting(1, "TEST_KEY_V1_32_BYTES_LONG_KEY_");
        keyManager.setKeyForTesting(0, "OLD_KEY_V0");

        // When
        String decrypted = keyManager.decrypt(v0Data);

        // Then
        assertThat(decrypted).isEqualTo("old-data");
    }

    @Test
    @DisplayName("복호화 - 레거시 데이터 (버전 없음)")
    void testDecrypt_Legacy() {
        // Given - 버전 prefix 없는 레거시 데이터
        String plainText = "legacy-data";

        // v0 키로 암호화 (버전 prefix 제거)
        keyManager.setKeyForTesting(1, "TEST_KEY_V0_32_BYTES_LONG_KEY_");
        String encrypted = keyManager.encrypt(plainText);
        String legacyData = encrypted.substring(3); // "v1:" 제거

        // 원래 키 복원
        keyManager.setKeyForTesting(1, "TEST_KEY_V1_32_BYTES_LONG_KEY_");
        keyManager.setKeyForTesting(0, "TEST_KEY_V0_32_BYTES_LONG_KEY_");

        // When
        String decrypted = keyManager.decrypt(legacyData);

        // Then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("isEncryptedWithCurrentKey - 최신 키 확인")
    void testIsEncryptedWithCurrentKey() {
        // Given
        String currentKeyData = keyManager.encrypt("test");
        String oldKeyData = "v0:some_old_encrypted_data";

        // Then
        assertThat(keyManager.isEncryptedWithCurrentKey(currentKeyData)).isTrue();
        assertThat(keyManager.isEncryptedWithCurrentKey(oldKeyData)).isFalse();
        assertThat(keyManager.isEncryptedWithCurrentKey("legacy_no_version")).isFalse();
    }

    @Test
    @DisplayName("reEncrypt - 이전 키 → 최신 키")
    void testReEncrypt() {
        // Given - v0 데이터 생성
        keyManager.setKeyForTesting(1, "OLD_KEY");
        String oldEncrypted = keyManager.encrypt("data");
        String v0Data = "v0:" + oldEncrypted.substring(3);

        keyManager.setKeyForTesting(1, "TEST_KEY_V1_32_BYTES_LONG_KEY_");
        keyManager.setKeyForTesting(0, "OLD_KEY");

        // When
        String reEncrypted = keyManager.reEncrypt(v0Data);

        // Then
        assertThat(reEncrypted).startsWith("v1:");
        assertThat(keyManager.isEncryptedWithCurrentKey(reEncrypted)).isTrue();

        // 재암호화된 데이터 복호화 확인
        String decrypted = keyManager.decrypt(reEncrypted);
        assertThat(decrypted).isEqualTo("data");
    }

    @Test
    @DisplayName("reEncrypt - 이미 최신 버전인 경우 변경 없음")
    void testReEncrypt_AlreadyCurrent() {
        // Given
        String currentData = keyManager.encrypt("test");

        // When
        String reEncrypted = keyManager.reEncrypt(currentData);

        // Then
        assertThat(reEncrypted).isEqualTo(currentData); // 동일한 데이터 반환
    }

    @Test
    @DisplayName("extractVersion - 버전 추출")
    void testExtractVersion() {
        // Given
        String v1Data = "v1:encrypted_data";
        String v0Data = "v0:encrypted_data";
        String legacyData = "encrypted_data_no_version";

        // Then
        assertThat(keyManager.extractVersion(v1Data)).isEqualTo(1);
        assertThat(keyManager.extractVersion(v0Data)).isEqualTo(0);
        assertThat(keyManager.extractVersion(legacyData)).isEqualTo(0); // 레거시는 v0
    }

    @Test
    @DisplayName("getCurrentVersion - 현재 버전 조회")
    void testGetCurrentVersion() {
        // Then
        assertThat(keyManager.getCurrentVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAvailableVersions - 사용 가능한 버전 목록")
    void testGetAvailableVersions() {
        // Then
        assertThat(keyManager.getAvailableVersions()).contains(1, 0);
        assertThat(keyManager.getAvailableVersions().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("암호화 - NULL 처리")
    void testEncrypt_Null() {
        // Then
        assertThat(keyManager.encrypt(null)).isNull();
    }

    @Test
    @DisplayName("복호화 - NULL 처리")
    void testDecrypt_Null() {
        // Then
        assertThat(keyManager.decrypt(null)).isNull();
    }

    @Test
    @DisplayName("복호화 실패 - 잘못된 키")
    void testDecrypt_WrongKey() {
        // Given - 다른 키로 암호화된 데이터
        String plainText = "secret";
        keyManager.setKeyForTesting(1, "CORRECT_KEY");
        String encrypted = keyManager.encrypt(plainText);

        // 키 변경 (복호화 실패 시뮬레이션)
        keyManager.setKeyForTesting(1, "WRONG_KEY");
        keyManager.setKeyForTesting(0, "ANOTHER_WRONG_KEY");

        // When/Then - 복호화 실패
        assertThatThrownBy(() -> keyManager.decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to decrypt");
    }

    @Test
    @DisplayName("다양한 데이터 암호화 테스트")
    void testEncrypt_VariousData() {
        String[] testData = {
                "",
                "a",
                "123456",
                "한글 데이터",
                "Special chars: !@#$%^&*()",
                "Very long string ".repeat(100)
        };

        for (String data : testData) {
            String encrypted = keyManager.encrypt(data);
            String decrypted = keyManager.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(data);
        }
    }
}
