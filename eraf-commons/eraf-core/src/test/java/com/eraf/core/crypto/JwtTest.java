package com.eraf.core.crypto;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jwt - JWT 토큰 테스트")
class JwtTest {

    // 최소 32바이트 이상의 비밀 키 필요
    private static final String SECRET_KEY = "eraf-jwt-secret-key-for-testing-12345678";

    @Nested
    @DisplayName("JWT 생성")
    class TokenCreation {

        @Test
        @DisplayName("기본 JWT 생성")
        void createToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");
            claims.put("role", "ADMIN");

            String token = Jwt.create(claims, SECRET_KEY);

            assertNotNull(token);
            assertTrue(token.split("\\.").length == 3); // header.payload.signature
        }

        @Test
        @DisplayName("만료 시간 지정 JWT 생성")
        void createTokenWithExpiration() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");

            String token = Jwt.create(claims, SECRET_KEY, 30); // 30분

            assertNotNull(token);
            assertFalse(Jwt.isExpired(token, SECRET_KEY));
        }

        @Test
        @DisplayName("클레임 없이 JWT 생성")
        void createTokenWithoutClaims() {
            String token = Jwt.create(null, SECRET_KEY);

            assertNotNull(token);
        }

        @Test
        @DisplayName("빈 클레임으로 JWT 생성")
        void createTokenWithEmptyClaims() {
            String token = Jwt.create(new HashMap<>(), SECRET_KEY);

            assertNotNull(token);
        }
    }

    @Nested
    @DisplayName("JWT 검증")
    class TokenVerification {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void verifyValidToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");
            claims.put("email", "user@example.com");

            String token = Jwt.create(claims, SECRET_KEY);
            Claims result = Jwt.verify(token, SECRET_KEY);

            assertNotNull(result);
            assertEquals("user123", result.get("userId"));
            assertEquals("user@example.com", result.get("email"));
        }

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        void verifyExpiredToken() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");

            // 0분 만료 (즉시 만료)
            String token = Jwt.create(claims, SECRET_KEY, 0);

            // 잠시 대기 후 검증
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThrows(Jwt.JwtValidationException.class, () -> Jwt.verify(token, SECRET_KEY));
        }

        @Test
        @DisplayName("잘못된 키로 검증 실패")
        void verifyWithWrongKey() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");

            String token = Jwt.create(claims, SECRET_KEY);
            String wrongKey = "wrong-secret-key-for-testing-12345678";

            assertThrows(Jwt.JwtValidationException.class, () -> Jwt.verify(token, wrongKey));
        }

        @Test
        @DisplayName("잘못된 형식 토큰 검증 실패")
        void verifyMalformedToken() {
            assertThrows(Jwt.JwtValidationException.class, () -> Jwt.verify("invalid.token", SECRET_KEY));
        }

        @Test
        @DisplayName("verifyQuietly - 유효한 토큰")
        void verifyQuietlyValid() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");

            String token = Jwt.create(claims, SECRET_KEY);
            Optional<Claims> result = Jwt.verifyQuietly(token, SECRET_KEY);

            assertTrue(result.isPresent());
            assertEquals("user123", result.get().get("userId"));
        }

        @Test
        @DisplayName("verifyQuietly - 잘못된 토큰")
        void verifyQuietlyInvalid() {
            Optional<Claims> result = Jwt.verifyQuietly("invalid-token", SECRET_KEY);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("JWT 만료 확인")
    class ExpirationCheck {

        @Test
        @DisplayName("만료되지 않은 토큰")
        void notExpired() {
            Map<String, Object> claims = new HashMap<>();
            String token = Jwt.create(claims, SECRET_KEY, 60);

            assertFalse(Jwt.isExpired(token, SECRET_KEY));
        }

        @Test
        @DisplayName("만료된 토큰")
        void expired() {
            Map<String, Object> claims = new HashMap<>();
            String token = Jwt.create(claims, SECRET_KEY, 0);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertTrue(Jwt.isExpired(token, SECRET_KEY));
        }
    }

    @Nested
    @DisplayName("클레임 추출 (검증 없이)")
    class ClaimExtraction {

        @Test
        @DisplayName("특정 클레임 추출")
        void getClaim() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");
            claims.put("role", "ADMIN");

            String token = Jwt.create(claims, SECRET_KEY);
            Optional<Object> userId = Jwt.getClaim(token, "userId");

            assertTrue(userId.isPresent());
            assertEquals("user123", userId.get());
        }

        @Test
        @DisplayName("존재하지 않는 클레임 추출")
        void getClaimNotExists() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", "user123");

            String token = Jwt.create(claims, SECRET_KEY);
            Optional<Object> notExists = Jwt.getClaim(token, "notExists");

            assertFalse(notExists.isPresent());
        }

        @Test
        @DisplayName("만료 시간 추출")
        void getExpiration() {
            Map<String, Object> claims = new HashMap<>();
            String token = Jwt.create(claims, SECRET_KEY, 60);

            Optional<Instant> expiration = Jwt.getExpiration(token);

            assertTrue(expiration.isPresent());
            assertTrue(expiration.get().isAfter(Instant.now()));
        }

        @Test
        @DisplayName("잘못된 토큰에서 클레임 추출 시 빈 Optional")
        void getClaimFromInvalidToken() {
            Optional<Object> result = Jwt.getClaim("invalid", "userId");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("비밀 키 생성")
    class KeyGeneration {

        @Test
        @DisplayName("랜덤 비밀 키 생성")
        void generateSecretKey() {
            String key1 = Jwt.generateSecretKey();
            String key2 = Jwt.generateSecretKey();

            assertNotNull(key1);
            assertNotNull(key2);
            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("생성된 키로 토큰 생성 및 검증")
        void useGeneratedKey() {
            String key = Jwt.generateSecretKey();
            Map<String, Object> claims = new HashMap<>();
            claims.put("test", "value");

            String token = Jwt.create(claims, key);
            Claims result = Jwt.verify(token, key);

            assertEquals("value", result.get("test"));
        }
    }
}
