package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdGenerator - ID 생성 유틸리티")
class IdGeneratorTest {

    @Nested
    @DisplayName("UUID")
    class UuidTests {

        @Test
        @DisplayName("UUID 형식 (하이픈 포함)")
        void uuid() {
            String id = IdGenerator.uuid();
            assertNotNull(id);
            assertEquals(36, id.length());
            assertTrue(id.contains("-"));
        }

        @Test
        @DisplayName("UUID Compact (하이픈 제외)")
        void uuidCompact() {
            String id = IdGenerator.uuidCompact();
            assertNotNull(id);
            assertEquals(32, id.length());
            assertFalse(id.contains("-"));
        }

        @Test
        @DisplayName("UUID 고유성")
        void uuidUniqueness() {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                ids.add(IdGenerator.uuid());
            }
            assertEquals(100, ids.size());
        }
    }

    @Nested
    @DisplayName("ULID")
    class UlidTests {

        @Test
        @DisplayName("ULID 형식 (26자)")
        void ulid() {
            String id = IdGenerator.ulid();
            assertNotNull(id);
            assertEquals(26, id.length());
        }

        @Test
        @DisplayName("ULID 고유성")
        void ulidUniqueness() {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                ids.add(IdGenerator.ulid());
            }
            assertEquals(100, ids.size());
        }

        @Test
        @DisplayName("ULID 알파벳 문자만 포함")
        void ulidCharset() {
            String id = IdGenerator.ulid();
            // ULID uses 0-9, A-Z, a-z characters
            assertTrue(id.matches("[0-9A-Za-z]+"));
        }
    }

    @Nested
    @DisplayName("NanoID")
    class NanoIdTests {

        @Test
        @DisplayName("NanoID 기본 (21자)")
        void nanoid() {
            String id = IdGenerator.nanoid();
            assertNotNull(id);
            assertEquals(21, id.length());
        }

        @Test
        @DisplayName("NanoID 길이 지정")
        void nanoidWithSize() {
            assertEquals(10, IdGenerator.nanoid(10).length());
            assertEquals(50, IdGenerator.nanoid(50).length());
        }

        @Test
        @DisplayName("NanoID 커스텀 알파벳")
        void nanoidCustomAlphabet() {
            String id = IdGenerator.nanoid(10, "ABC123");
            assertEquals(10, id.length());
            for (char c : id.toCharArray()) {
                assertTrue("ABC123".indexOf(c) >= 0);
            }
        }

        @Test
        @DisplayName("NanoID 고유성")
        void nanoidUniqueness() {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                ids.add(IdGenerator.nanoid());
            }
            assertEquals(100, ids.size());
        }
    }

    @Nested
    @DisplayName("숫자 ID")
    class NumericIdTests {

        @Test
        @DisplayName("숫자 ID 길이")
        void numericId() {
            String id = IdGenerator.numericId(8);
            assertEquals(8, id.length());
            assertTrue(id.matches("\\d+"));
        }

        @Test
        @DisplayName("타임스탬프 ID")
        void timestampId() {
            String id = IdGenerator.timestampId();
            assertNotNull(id);
            assertTrue(id.length() >= 17); // 13자 밀리초 + 4자 랜덤
            assertTrue(id.matches("\\d+"));
        }
    }

    @Nested
    @DisplayName("접두사 ID")
    class PrefixedIdTests {

        @Test
        @DisplayName("접두사 + 타임스탬프 ID")
        void prefixedId() {
            String id = IdGenerator.prefixedId("ORD");
            assertNotNull(id);
            assertTrue(id.startsWith("ORD-"));
        }

        @Test
        @DisplayName("접두사 + UUID")
        void prefixedUuid() {
            String id = IdGenerator.prefixedUuid("USR");
            assertNotNull(id);
            assertTrue(id.startsWith("USR-"));
            assertEquals(36, id.length()); // "USR-" (4) + uuidCompact (32) = 36
        }
    }
}
