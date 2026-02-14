package com.eraf.sequence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class SequenceTest {

    // ========== Reset Enum ==========
    @Nested
    @DisplayName("Reset (리셋 정책)")
    class ResetTest {

        @Test
        @DisplayName("enum 값 확인")
        void enumValues() {
            Reset[] values = Reset.values();
            assertEquals(4, values.length);
            assertEquals(Reset.DAILY, Reset.valueOf("DAILY"));
            assertEquals(Reset.MONTHLY, Reset.valueOf("MONTHLY"));
            assertEquals(Reset.YEARLY, Reset.valueOf("YEARLY"));
            assertEquals(Reset.NEVER, Reset.valueOf("NEVER"));
        }
    }

    // ========== SequenceGenerator ==========
    @Nested
    @DisplayName("SequenceGenerator (채번 생성기)")
    class SequenceGeneratorTest {

        @BeforeEach
        void setUp() {
            SequenceGenerator.resetAll();
        }

        @AfterEach
        void tearDown() {
            SequenceGenerator.resetAll();
        }

        @Test
        @DisplayName("기본 채번 (NEVER 리셋)")
        void basicSequenceNeverReset() {
            String seq1 = SequenceGenerator.next("test", "ORD", Reset.NEVER, 5);
            String seq2 = SequenceGenerator.next("test", "ORD", Reset.NEVER, 5);

            assertEquals("ORD00001", seq1);
            assertEquals("ORD00002", seq2);
        }

        @Test
        @DisplayName("접두사 없는 채번")
        void noPrefix() {
            String seq = SequenceGenerator.next("test", "", Reset.NEVER, 5);
            assertEquals("00001", seq);
        }

        @Test
        @DisplayName("DAILY 리셋 - 날짜 포함")
        void dailyReset() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String seq = SequenceGenerator.next("daily-test", "INV", Reset.DAILY, 4);

            assertTrue(seq.startsWith("INV-" + today + "-"));
            assertTrue(seq.endsWith("0001"));
        }

        @Test
        @DisplayName("MONTHLY 리셋 - 월 포함")
        void monthlyReset() {
            String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String seq = SequenceGenerator.next("monthly-test", "RPT", Reset.MONTHLY, 3);

            assertTrue(seq.startsWith("RPT-" + month + "-"));
            assertTrue(seq.endsWith("001"));
        }

        @Test
        @DisplayName("YEARLY 리셋 - 연도 포함")
        void yearlyReset() {
            String year = String.valueOf(LocalDate.now().getYear());
            String seq = SequenceGenerator.next("yearly-test", "DOC", Reset.YEARLY, 6);

            assertTrue(seq.startsWith("DOC-" + year + "-"));
            assertTrue(seq.endsWith("000001"));
        }

        @Test
        @DisplayName("순차 증가 확인")
        void sequentialIncrement() {
            for (int i = 1; i <= 5; i++) {
                String seq = SequenceGenerator.next("inc-test", "", Reset.NEVER, 3);
                assertEquals(String.format("%03d", i), seq);
            }
        }

        @Test
        @DisplayName("current() - 현재 값 조회")
        void current() {
            assertEquals(0, SequenceGenerator.current("new-seq"));

            SequenceGenerator.next("new-seq", "", Reset.NEVER, 3);
            assertEquals(1, SequenceGenerator.current("new-seq"));

            SequenceGenerator.next("new-seq", "", Reset.NEVER, 3);
            assertEquals(2, SequenceGenerator.current("new-seq"));
        }

        @Test
        @DisplayName("reset() - 시퀀스 리셋")
        void reset() {
            SequenceGenerator.next("reset-test", "", Reset.NEVER, 3);
            SequenceGenerator.next("reset-test", "", Reset.NEVER, 3);
            assertEquals(2, SequenceGenerator.current("reset-test"));

            SequenceGenerator.reset("reset-test");
            assertEquals(0, SequenceGenerator.current("reset-test"));
        }

        @Test
        @DisplayName("resetAll() - 전체 시퀀스 리셋")
        void resetAll() {
            SequenceGenerator.next("seq1", "", Reset.NEVER, 3);
            SequenceGenerator.next("seq2", "", Reset.NEVER, 3);

            SequenceGenerator.resetAll();

            assertEquals(0, SequenceGenerator.current("seq1"));
            assertEquals(0, SequenceGenerator.current("seq2"));
        }

        @Test
        @DisplayName("커스텀 날짜 구분자와 포맷")
        void customDateSeparatorAndFormat() {
            String seq = SequenceGenerator.next("custom", "PO", Reset.DAILY, 3, "/", "yyyy-MM-dd");

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            assertTrue(seq.startsWith("PO/" + today + "/"));
            assertTrue(seq.endsWith("001"));
        }

        @Test
        @DisplayName("서로 다른 이름의 시퀀스는 독립적")
        void independentSequences() {
            SequenceGenerator.next("A", "", Reset.NEVER, 3);
            SequenceGenerator.next("A", "", Reset.NEVER, 3);
            SequenceGenerator.next("B", "", Reset.NEVER, 3);

            assertEquals(2, SequenceGenerator.current("A"));
            assertEquals(1, SequenceGenerator.current("B"));
        }
    }

    // ========== SequenceAspect ==========
    @Nested
    @DisplayName("SequenceAspect (채번 AOP)")
    class SequenceAspectTest {

        private SequenceAspect aspect;

        @BeforeEach
        void setUp() {
            SequenceGenerator.resetAll();
            aspect = new SequenceAspect();
        }

        @AfterEach
        void tearDown() {
            SequenceGenerator.resetAll();
        }

        @Test
        @DisplayName("@Sequence 필드에 자동 채번")
        void populateSequenceField() {
            TestEntity entity = new TestEntity();
            aspect.populateSequenceFields(entity);

            assertNotNull(entity.orderNo);
            assertTrue(entity.orderNo.startsWith("ORD"));
        }

        @Test
        @DisplayName("이미 값이 있는 필드는 건너뜀")
        void skipExistingValue() {
            TestEntity entity = new TestEntity();
            entity.orderNo = "EXISTING";

            aspect.populateSequenceFields(entity);

            assertEquals("EXISTING", entity.orderNo);
        }

        @Test
        @DisplayName("@Sequence 없는 필드는 무시")
        void ignoreNonSequenceField() {
            TestEntity entity = new TestEntity();
            entity.name = null;

            aspect.populateSequenceFields(entity);

            assertNull(entity.name);
        }

        @Test
        @DisplayName("null 객체 처리")
        void nullTarget() {
            assertDoesNotThrow(() -> aspect.populateSequenceFields(null));
        }

        @Test
        @DisplayName("커스텀 SequenceProvider")
        void customSequenceProvider() {
            SequenceAspect.SequenceProvider customProvider =
                    (name, prefix, reset, digits, sep, format) -> "CUSTOM-001";

            SequenceAspect customAspect = new SequenceAspect(customProvider);
            TestEntity entity = new TestEntity();
            customAspect.populateSequenceFields(entity);

            assertEquals("CUSTOM-001", entity.orderNo);
        }

        // 테스트용 엔티티
        static class TestEntity {
            @Sequence(name = "order", prefix = "ORD", reset = Reset.NEVER, digits = 5)
            String orderNo;

            String name;
        }
    }
}
