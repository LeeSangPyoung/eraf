package com.eraf.test.data;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestDataGenerator 테스트
 */
class TestDataGeneratorTest {

    @Test
    void fullName_ShouldGenerateNonEmptyName() {
        // when
        String name = TestDataGenerator.fullName();

        // then
        assertThat(name).isNotNull();
        assertThat(name).isNotEmpty();
    }

    @Test
    void email_ShouldGenerateValidEmail() {
        // when
        String email = TestDataGenerator.email();

        // then
        assertThat(email).isNotNull();
        assertThat(email).contains("@");
        assertThat(email).contains(".");
    }

    @Test
    void randomInt_WithRange_ShouldBeInRange() {
        // given
        int min = 10;
        int max = 20;

        // when
        int value = TestDataGenerator.randomInt(min, max);

        // then
        assertThat(value).isBetween(min, max);
    }

    @Test
    void randomLong_WithRange_ShouldBeInRange() {
        // given
        long min = 100L;
        long max = 200L;

        // when
        long value = TestDataGenerator.randomLong(min, max);

        // then
        assertThat(value).isBetween(min, max);
    }

    @Test
    void randomDouble_WithRange_ShouldBeInRange() {
        // given
        double min = 1.0;
        double max = 10.0;

        // when
        double value = TestDataGenerator.randomDouble(min, max);

        // then
        assertThat(value).isBetween(min, max);
    }

    @Test
    void randomBoolean_ShouldReturnBoolean() {
        // when
        boolean value = TestDataGenerator.randomBoolean();

        // then - 단순히 예외 없이 실행되는지 확인
        assertThat(value).isIn(true, false);
    }

    @Test
    void pastDate_ShouldBeBeforeNow() {
        // when
        LocalDate pastDate = TestDataGenerator.pastDate();

        // then
        assertThat(pastDate).isBefore(LocalDate.now().plusDays(1));
    }

    @Test
    void futureDate_ShouldBeAfterNow() {
        // when
        LocalDate futureDate = TestDataGenerator.futureDate();

        // then
        assertThat(futureDate).isAfter(LocalDate.now().minusDays(1));
    }

    @Test
    void pastDateTime_ShouldBeBeforeNow() {
        // when
        LocalDateTime pastDateTime = TestDataGenerator.pastDateTime();

        // then
        assertThat(pastDateTime).isBefore(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void futureInstant_ShouldBeAfterNow() {
        // when
        Instant futureInstant = TestDataGenerator.futureInstant();

        // then
        assertThat(futureInstant).isAfter(Instant.now().minusSeconds(1));
    }

    @Test
    void list_ShouldGenerateCorrectNumberOfItems() {
        // given
        int count = 10;

        // when
        List<String> names = TestDataGenerator.list(count, TestDataGenerator::fullName);

        // then
        assertThat(names).hasSize(count);
        assertThat(names).allMatch(name -> !name.isEmpty());
    }

    @Test
    void oneOf_WithArray_ShouldReturnOneOfOptions() {
        // given
        String[] options = {"A", "B", "C"};

        // when
        String selected = TestDataGenerator.oneOf(options);

        // then
        assertThat(selected).isIn((Object[]) options);
    }

    @Test
    void oneOf_WithList_ShouldReturnOneOfOptions() {
        // given
        List<String> options = List.of("X", "Y", "Z");

        // when
        String selected = TestDataGenerator.oneOf(options);

        // then
        assertThat(selected).isIn(options);
    }

    @Test
    void korean_ShouldGenerateKoreanData() {
        // given
        TestDataGenerator korean = TestDataGenerator.korean();

        // when
        String name = korean.instanceFullName();

        // then
        assertThat(name).isNotNull();
        assertThat(name).isNotEmpty();
    }

    @Test
    void uuid_ShouldGenerateValidUUID() {
        // when
        String uuid = TestDataGenerator.uuid();

        // then
        assertThat(uuid).isNotNull();
        assertThat(uuid).hasSize(36);  // UUID 표준 길이
        assertThat(uuid).contains("-");
    }

    @Test
    void url_ShouldGenerateValidUrl() {
        // when
        String url = TestDataGenerator.url();

        // then
        assertThat(url).isNotNull();
        assertThat(url).matches("^https?://.*");
    }

    @Test
    void ipAddress_ShouldGenerateValidIp() {
        // when
        String ip = TestDataGenerator.ipAddress();

        // then
        assertThat(ip).isNotNull();
        assertThat(ip).matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    }

    @Test
    void text_ShouldRespectMaxLength() {
        // given
        int maxLength = 50;

        // when
        String text = TestDataGenerator.text(maxLength);

        // then
        assertThat(text).isNotNull();
        assertThat(text.length()).isLessThanOrEqualTo(maxLength);
    }

    @Test
    void multipleCalls_ShouldGenerateDifferentValues() {
        // when
        String name1 = TestDataGenerator.fullName();
        String name2 = TestDataGenerator.fullName();
        String email1 = TestDataGenerator.email();
        String email2 = TestDataGenerator.email();

        // then - 대부분의 경우 다른 값이 생성되어야 함 (랜덤)
        // 단, 100% 보장은 불가능하므로 여러 번 시도
        boolean allSame = name1.equals(name2) && email1.equals(email2);
        assertThat(allSame).isFalse();
    }
}
