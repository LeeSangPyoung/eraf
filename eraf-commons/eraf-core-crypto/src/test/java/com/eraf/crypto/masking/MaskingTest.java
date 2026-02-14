package com.eraf.crypto.masking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Masking - 마스킹 유틸리티")
class MaskingTest {

    @Nested
    @DisplayName("이름 마스킹")
    class NameTests {
        @Test void twoChars() { assertEquals("홍*", Masking.name("홍길")); }
        @Test void threeChars() { assertEquals("홍*동", Masking.name("홍길동")); }
        @Test void fourChars() { assertEquals("홍**동", Masking.name("홍길수동")); }
        @Test void oneChar() { assertEquals("홍", Masking.name("홍")); }
        @Test void nullInput() { assertNull(Masking.name(null)); }
    }

    @Nested
    @DisplayName("전화번호 마스킹")
    class PhoneTests {
        @Test void mobile11() { assertEquals("010-****-5678", Masking.phone("01012345678")); }
        @Test void landline10_02() { assertEquals("02-****-5678", Masking.phone("0212345678")); }
        @Test void landline10_031() { assertEquals("031-***-5678", Masking.phone("0311235678")); }
        @Test void withDashes() { assertEquals("010-****-5678", Masking.phone("010-1234-5678")); }
        @Test void nullInput() { assertNull(Masking.phone(null)); }
        @Test void shortNumber() { assertNotNull(Masking.phone("12345")); }

        @Test void patternShowFirst() {
            assertEquals("010-****-****", Masking.phone("01012345678", MaskPattern.SHOW_FIRST));
        }
        @Test void patternShowLast() {
            assertEquals("***-****-5678", Masking.phone("01012345678", MaskPattern.SHOW_LAST));
        }
        @Test void patternDefault() {
            assertEquals("010-****-5678", Masking.phone("01012345678", MaskPattern.DEFAULT));
        }
        @Test void patternShowFirstShort() {
            assertEquals("**", Masking.phone("12", MaskPattern.SHOW_FIRST));
        }
        @Test void patternShowLastShort() {
            assertEquals("**", Masking.phone("12", MaskPattern.SHOW_LAST));
        }
        @Test void patternNull() { assertNull(Masking.phone(null, MaskPattern.SHOW_FIRST)); }
    }

    @Nested
    @DisplayName("이메일 마스킹")
    class EmailTests {
        @Test void normal() { assertEquals("te**@gmail.com", Masking.email("test@gmail.com")); }
        @Test void shortLocal() { assertEquals("**@gmail.com", Masking.email("ab@gmail.com")); }
        @Test void singleCharLocal() { assertEquals("*@gmail.com", Masking.email("a@gmail.com")); }
        @Test void noAt() { assertEquals("test", Masking.email("test")); }
        @Test void nullInput() { assertNull(Masking.email(null)); }
    }

    @Nested
    @DisplayName("카드번호 마스킹")
    class CardTests {
        @Test void standard16() { assertEquals("1234-****-****-3456", Masking.card("1234567890123456")); }
        @Test void withDashes() { assertEquals("1234-****-****-3456", Masking.card("1234-5678-9012-3456")); }
        @Test void shortNumber() { assertNotNull(Masking.card("123456")); }
        @Test void nullInput() { assertNull(Masking.card(null)); }
    }

    @Nested
    @DisplayName("계좌번호 마스킹")
    class AccountTests {
        @Test void standard() { assertEquals("123-****-890", Masking.account("1234567890")); }
        @Test void shortNumber() { assertNotNull(Masking.account("12345")); }
        @Test void nullInput() { assertNull(Masking.account(null)); }
    }

    @Nested
    @DisplayName("주소 마스킹")
    class AddressTests {
        @Test void normal() { assertEquals("서울시 강남구 ***", Masking.address("서울시 강남구 역삼동 123-45")); }
        @Test void twoWords() { assertNotNull(Masking.address("서울시 강남구")); }
        @Test void nullInput() { assertNull(Masking.address(null)); }
    }

    @Nested
    @DisplayName("IP 마스킹")
    class IpTests {
        @Test void ipv4() { assertEquals("192.168.1.***", Masking.ip("192.168.1.100")); }
        @Test void noDot() { assertNotNull(Masking.ip("localhost")); }
        @Test void nullInput() { assertNull(Masking.ip(null)); }
    }

    @Nested
    @DisplayName("차량번호 마스킹")
    class CarNumberTests {
        @Test void standard() { assertEquals("12가*****", Masking.carNumber("12가1234호")); }
        @Test void shortNumber() { assertEquals("abc", Masking.carNumber("abc")); }
        @Test void nullInput() { assertNull(Masking.carNumber(null)); }
    }

    @Nested
    @DisplayName("주민등록번호 마스킹")
    class ResidentNoTests {
        @Test void standard() { assertEquals("900101-*******", Masking.residentNo("900101-1234567")); }
        @Test void noDash() { assertEquals("900101-*******", Masking.residentNo("9001011234567")); }
        @Test void shortNumber() { assertNotNull(Masking.residentNo("123456")); }
        @Test void nullInput() { assertNull(Masking.residentNo(null)); }
    }

    @Nested
    @DisplayName("범용 마스킹")
    class GeneralTests {
        @Test void maskMiddle() { assertEquals("h***o", Masking.maskMiddle("hello")); }
        @Test void maskMiddleShort() { assertEquals("ab", Masking.maskMiddle("ab")); }
        @Test void maskMiddleNull() { assertNull(Masking.maskMiddle(null)); }

        @Test void maskLastHalf() { assertEquals("he***", Masking.maskLastHalf("hello")); }
        @Test void maskLastHalfEmpty() { assertEquals("", Masking.maskLastHalf("")); }
        @Test void maskLastHalfNull() { assertNull(Masking.maskLastHalf(null)); }

        @Test void maskAll() { assertEquals("*****", Masking.maskAll("hello")); }
        @Test void maskAllNull() { assertNull(Masking.maskAll(null)); }

        @Test void maskCustom() { assertEquals("he***", Masking.mask("hello", 2, 3)); }
        @Test void maskCustomOverflow() { assertEquals("hel**", Masking.mask("hello", 3, 10)); }
        @Test void maskCustomNull() { assertNull(Masking.mask(null, 0, 3)); }
        @Test void maskCustomNegativeStart() { assertEquals("hello", Masking.mask("hello", -1, 3)); }
        @Test void maskCustomZeroLength() { assertEquals("hello", Masking.mask("hello", 0, 0)); }
        @Test void maskCustomStartBeyond() { assertEquals("hello", Masking.mask("hello", 10, 3)); }
    }

    @Test
    @DisplayName("MaskPattern 열거형")
    void maskPatternEnum() {
        assertEquals(5, MaskPattern.values().length);
        assertNotNull(MaskPattern.valueOf("DEFAULT"));
        assertNotNull(MaskPattern.valueOf("SHOW_FIRST"));
        assertNotNull(MaskPattern.valueOf("SHOW_LAST"));
        assertNotNull(MaskPattern.valueOf("MASK_MIDDLE"));
        assertNotNull(MaskPattern.valueOf("MASK_ALL"));
    }
}
