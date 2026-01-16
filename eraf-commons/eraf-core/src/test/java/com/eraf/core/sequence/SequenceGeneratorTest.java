package com.eraf.core.sequence;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SequenceGenerator - 채번 생성기 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SequenceGeneratorTest {

    @BeforeEach
    void setUp() {
        SequenceGenerator.resetAll();
    }

    @AfterEach
    void tearDown() {
        SequenceGenerator.resetAll();
    }

    @Nested
    @DisplayName("기본 채번 생성")
    class BasicSequence {

        @Test
        @DisplayName("순차적 번호 생성")
        void sequentialGeneration() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String seq1 = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);
            String seq2 = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);
            String seq3 = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);

            assertEquals("ORD-" + today + "-00001", seq1);
            assertEquals("ORD-" + today + "-00002", seq2);
            assertEquals("ORD-" + today + "-00003", seq3);
        }

        @Test
        @DisplayName("서로 다른 시퀀스 독립 운영")
        void independentSequences() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String order1 = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);
            String payment1 = SequenceGenerator.next("PAYMENT", "PAY", Reset.DAILY, 5);
            String order2 = SequenceGenerator.next("ORDER", "ORD", Reset.DAILY, 5);

            assertEquals("ORD-" + today + "-00001", order1);
            assertEquals("PAY-" + today + "-00001", payment1);
            assertEquals("ORD-" + today + "-00002", order2);
        }
    }

    @Nested
    @DisplayName("리셋 정책")
    class ResetPolicy {

        @Test
        @DisplayName("DAILY - 날짜 포함")
        void dailyReset() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String seq = SequenceGenerator.next("TEST", "TST", Reset.DAILY, 5);

            assertTrue(seq.contains(today));
            assertEquals("TST-" + today + "-00001", seq);
        }

        @Test
        @DisplayName("MONTHLY - 년월 포함")
        void monthlyReset() {
            String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

            String seq = SequenceGenerator.next("TEST", "TST", Reset.MONTHLY, 5);

            assertTrue(seq.contains(yearMonth));
            assertEquals("TST-" + yearMonth + "-00001", seq);
        }

        @Test
        @DisplayName("YEARLY - 년도 포함")
        void yearlyReset() {
            String year = String.valueOf(LocalDate.now().getYear());

            String seq = SequenceGenerator.next("TEST", "TST", Reset.YEARLY, 5);

            assertTrue(seq.contains(year));
            assertEquals("TST-" + year + "-00001", seq);
        }

        @Test
        @DisplayName("NEVER - 날짜 미포함")
        void neverReset() {
            String seq = SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);

            assertEquals("TST00001", seq);
        }
    }

    @Nested
    @DisplayName("자릿수 설정")
    class DigitSettings {

        @Test
        @DisplayName("5자리 패딩")
        void fiveDigits() {
            String seq = SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);

            assertTrue(seq.endsWith("00001"));
        }

        @Test
        @DisplayName("3자리 패딩")
        void threeDigits() {
            String seq = SequenceGenerator.next("TEST", "TST", Reset.NEVER, 3);

            assertTrue(seq.endsWith("001"));
        }

        @Test
        @DisplayName("8자리 패딩")
        void eightDigits() {
            String seq = SequenceGenerator.next("TEST", "TST", Reset.NEVER, 8);

            assertTrue(seq.endsWith("00000001"));
        }
    }

    @Nested
    @DisplayName("커스텀 포맷")
    class CustomFormat {

        @Test
        @DisplayName("구분자 변경")
        void customSeparator() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String seq = SequenceGenerator.next("TEST", "ORD", Reset.DAILY, 5, "/", "yyyyMMdd");

            assertEquals("ORD/" + today + "/00001", seq);
        }

        @Test
        @DisplayName("날짜 포맷 변경")
        void customDateFormat() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

            String seq = SequenceGenerator.next("TEST", "ORD", Reset.DAILY, 5, "-", "yyMMdd");

            assertEquals("ORD-" + today + "-00001", seq);
        }
    }

    @Nested
    @DisplayName("현재 시퀀스 조회")
    class CurrentSequence {

        @Test
        @DisplayName("현재 값 조회")
        void getCurrent() {
            SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);
            SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);
            SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);

            assertEquals(3, SequenceGenerator.current("TEST"));
        }

        @Test
        @DisplayName("존재하지 않는 시퀀스 조회 시 0 반환")
        void getCurrentNotExists() {
            assertEquals(0, SequenceGenerator.current("NOT_EXISTS"));
        }
    }

    @Nested
    @DisplayName("시퀀스 리셋")
    class SequenceReset {

        @Test
        @DisplayName("특정 시퀀스 리셋")
        void resetSingle() {
            SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);
            SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);

            assertEquals(2, SequenceGenerator.current("TEST"));

            SequenceGenerator.reset("TEST");

            assertEquals(0, SequenceGenerator.current("TEST"));

            String newSeq = SequenceGenerator.next("TEST", "TST", Reset.NEVER, 5);
            assertEquals("TST00001", newSeq);
        }

        @Test
        @DisplayName("전체 시퀀스 리셋")
        void resetAll() {
            SequenceGenerator.next("TEST1", "T1", Reset.NEVER, 5);
            SequenceGenerator.next("TEST2", "T2", Reset.NEVER, 5);

            SequenceGenerator.resetAll();

            assertEquals(0, SequenceGenerator.current("TEST1"));
            assertEquals(0, SequenceGenerator.current("TEST2"));
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("멀티스레드 환경에서 중복 없이 생성")
        void concurrentGeneration() throws InterruptedException {
            int threadCount = 10;
            int iterationsPerThread = 100;
            java.util.Set<String> results = java.util.concurrent.ConcurrentHashMap.newKeySet();
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        for (int j = 0; j < iterationsPerThread; j++) {
                            String seq = SequenceGenerator.next("CONCURRENT", "C", Reset.NEVER, 5);
                            results.add(seq);
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();

            // 모든 생성된 시퀀스가 고유해야 함
            assertEquals(threadCount * iterationsPerThread, results.size());
            assertEquals(threadCount * iterationsPerThread, SequenceGenerator.current("CONCURRENT"));
        }
    }

    @Nested
    @DisplayName("접두사 처리")
    class PrefixHandling {

        @Test
        @DisplayName("빈 접두사")
        void emptyPrefix() {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String seq = SequenceGenerator.next("TEST", "", Reset.DAILY, 5);

            assertEquals(today + "-00001", seq);
        }

        @Test
        @DisplayName("null 접두사")
        void nullPrefix() {
            String seq = SequenceGenerator.next("TEST", null, Reset.NEVER, 5);

            assertEquals("00001", seq);
        }
    }
}
