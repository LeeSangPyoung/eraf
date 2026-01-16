package com.eraf.core.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password - bcrypt 비밀번호 해싱 테스트")
class PasswordTest {

    @Nested
    @DisplayName("비밀번호 해싱")
    class Hashing {

        @Test
        @DisplayName("비밀번호 해시 생성")
        void hashPassword() {
            String password = "mySecurePassword123!";

            String hash = Password.hash(password);

            assertNotNull(hash);
            assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
            assertNotEquals(password, hash);
        }

        @Test
        @DisplayName("동일 비밀번호 해싱 시 매번 다른 해시 생성 (Salt)")
        void differentHashEachTime() {
            String password = "myPassword123";

            String hash1 = Password.hash(password);
            String hash2 = Password.hash(password);

            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("null 비밀번호 시 예외 발생")
        void nullPassword() {
            assertThrows(IllegalArgumentException.class, () -> Password.hash(null));
        }

        @Test
        @DisplayName("빈 비밀번호 해싱")
        void emptyPassword() {
            String hash = Password.hash("");

            assertNotNull(hash);
            assertTrue(Password.verify("", hash));
        }

        @Test
        @DisplayName("한글 비밀번호 해싱")
        void koreanPassword() {
            String password = "비밀번호123!";

            String hash = Password.hash(password);

            assertTrue(Password.verify(password, hash));
        }
    }

    @Nested
    @DisplayName("비밀번호 검증")
    class Verification {

        @Test
        @DisplayName("올바른 비밀번호 검증 성공")
        void verifyCorrectPassword() {
            String password = "correctPassword123!";
            String hash = Password.hash(password);

            assertTrue(Password.verify(password, hash));
        }

        @Test
        @DisplayName("잘못된 비밀번호 검증 실패")
        void verifyWrongPassword() {
            String password = "correctPassword123!";
            String hash = Password.hash(password);

            assertFalse(Password.verify("wrongPassword", hash));
        }

        @Test
        @DisplayName("null 비밀번호 검증 시 false 반환")
        void verifyNullPassword() {
            String hash = Password.hash("password");

            assertFalse(Password.verify(null, hash));
        }

        @Test
        @DisplayName("null 해시 검증 시 false 반환")
        void verifyNullHash() {
            assertFalse(Password.verify("password", null));
        }

        @Test
        @DisplayName("둘 다 null인 경우 false 반환")
        void verifyBothNull() {
            assertFalse(Password.verify(null, null));
        }
    }

    @Nested
    @DisplayName("Cost Factor 설정")
    class CostFactor {

        @Test
        @DisplayName("커스텀 cost factor로 해싱")
        void hashWithCustomCost() {
            String password = "password123";

            String hash = Password.hash(password, 10);

            assertNotNull(hash);
            assertTrue(hash.contains("$10$"));
            assertTrue(Password.verify(password, hash));
        }

        @Test
        @DisplayName("최소 cost factor (4)")
        void minCostFactor() {
            String password = "password123";

            String hash = Password.hash(password, 4);

            assertTrue(hash.contains("$04$"));
            assertTrue(Password.verify(password, hash));
        }

        @Test
        @DisplayName("cost factor 범위 벗어난 값 예외")
        void invalidCostFactor() {
            assertThrows(IllegalArgumentException.class, () -> Password.hash("password", 3));
            assertThrows(IllegalArgumentException.class, () -> Password.hash("password", 32));
        }
    }

    @Nested
    @DisplayName("재해싱 필요 여부 확인")
    class RehashCheck {

        @Test
        @DisplayName("낮은 cost factor 해시는 재해싱 필요")
        void needsRehashForLowCost() {
            String hash = Password.hash("password", 10);

            assertTrue(Password.needsRehash(hash));
        }

        @Test
        @DisplayName("현재 cost factor 이상이면 재해싱 불필요")
        void noRehashNeeded() {
            String hash = Password.hash("password", 12);

            assertFalse(Password.needsRehash(hash));
        }

        @Test
        @DisplayName("null 해시는 재해싱 필요")
        void nullHashNeedsRehash() {
            assertTrue(Password.needsRehash(null));
        }

        @Test
        @DisplayName("잘못된 형식 해시는 재해싱 필요")
        void invalidHashNeedsRehash() {
            assertTrue(Password.needsRehash("invalid-hash"));
        }
    }
}
