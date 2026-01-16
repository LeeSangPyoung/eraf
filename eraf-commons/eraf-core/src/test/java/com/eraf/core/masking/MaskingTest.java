package com.eraf.core.masking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Masking - 마스킹 유틸리티 테스트")
class MaskingTest {

    @Nested
    @DisplayName("이름 마스킹")
    class NameMasking {

        @Test
        @DisplayName("3글자 이름 마스킹")
        void threeCharName() {
            assertEquals("홍*동", Masking.name("홍길동"));
            assertEquals("김*수", Masking.name("김철수"));
        }

        @Test
        @DisplayName("2글자 이름 마스킹")
        void twoCharName() {
            assertEquals("김*", Masking.name("김수"));
        }

        @Test
        @DisplayName("4글자 이름 마스킹")
        void fourCharName() {
            assertEquals("제*스", Masking.name("제임스")); // 3글자: 첫글자 + * + 마지막
            assertEquals("남**름", Masking.name("남궁이름")); // 4글자
        }

        @Test
        @DisplayName("1글자 이름은 그대로")
        void singleCharName() {
            assertEquals("김", Masking.name("김"));
        }

        @Test
        @DisplayName("null 입력")
        void nullName() {
            assertNull(Masking.name(null));
        }
    }

    @Nested
    @DisplayName("전화번호 마스킹")
    class PhoneMasking {

        @Test
        @DisplayName("휴대폰 번호 마스킹 (11자리)")
        void mobilePhone() {
            assertEquals("010-****-5678", Masking.phone("01012345678"));
            assertEquals("010-****-5678", Masking.phone("010-1234-5678"));
        }

        @Test
        @DisplayName("서울 지역번호 (10자리)")
        void seoulPhone() {
            assertEquals("02-****-5678", Masking.phone("0212345678"));
        }

        @Test
        @DisplayName("일반 지역번호 (10자리)")
        void areaPhone() {
            assertEquals("031-***-5678", Masking.phone("0311235678"));
        }

        @Test
        @DisplayName("패턴 지정 - 앞 번호만 표시")
        void showFirst() {
            assertEquals("010-****-****", Masking.phone("01012345678", MaskPattern.SHOW_FIRST));
        }

        @Test
        @DisplayName("패턴 지정 - 뒤 번호만 표시")
        void showLast() {
            assertEquals("***-****-5678", Masking.phone("01012345678", MaskPattern.SHOW_LAST));
        }

        @Test
        @DisplayName("null 입력")
        void nullPhone() {
            assertNull(Masking.phone(null));
        }
    }

    @Nested
    @DisplayName("이메일 마스킹")
    class EmailMasking {

        @Test
        @DisplayName("일반 이메일 마스킹")
        void normalEmail() {
            assertEquals("te**@gmail.com", Masking.email("test@gmail.com"));
            assertEquals("us*****@example.com", Masking.email("user123@example.com")); // user123 = 7글자, 앞 2글자 + 5개 마스킹
        }

        @Test
        @DisplayName("짧은 로컬 파트")
        void shortLocalPart() {
            assertEquals("**@test.com", Masking.email("ab@test.com"));
        }

        @Test
        @DisplayName("@ 없는 잘못된 이메일")
        void invalidEmail() {
            assertEquals("invalid-email", Masking.email("invalid-email"));
        }

        @Test
        @DisplayName("null 입력")
        void nullEmail() {
            assertNull(Masking.email(null));
        }
    }

    @Nested
    @DisplayName("카드번호 마스킹")
    class CardMasking {

        @Test
        @DisplayName("16자리 카드번호 마스킹")
        void normalCardNumber() {
            assertEquals("1234-****-****-5678", Masking.card("1234567890125678"));
            assertEquals("1234-****-****-5678", Masking.card("1234-5678-9012-5678"));
        }

        @Test
        @DisplayName("짧은 카드번호")
        void shortCardNumber() {
            // 16자리 미만이면 중간 마스킹
            assertNotNull(Masking.card("123456789012"));
        }

        @Test
        @DisplayName("null 입력")
        void nullCard() {
            assertNull(Masking.card(null));
        }
    }

    @Nested
    @DisplayName("계좌번호 마스킹")
    class AccountMasking {

        @Test
        @DisplayName("일반 계좌번호 마스킹")
        void normalAccount() {
            assertEquals("123-****-890", Masking.account("1234567890"));
        }

        @Test
        @DisplayName("긴 계좌번호")
        void longAccount() {
            // 17자리: 앞3 + 마스킹11 + 뒤3 = 123-***********-456
            assertEquals("123-***********-456", Masking.account("12345678901234456"));
        }

        @Test
        @DisplayName("null 입력")
        void nullAccount() {
            assertNull(Masking.account(null));
        }
    }

    @Nested
    @DisplayName("주소 마스킹")
    class AddressMasking {

        @Test
        @DisplayName("상세 주소 마스킹")
        void detailedAddress() {
            assertEquals("서울시 강남구 ***", Masking.address("서울시 강남구 역삼동 123-45"));
        }

        @Test
        @DisplayName("짧은 주소")
        void shortAddress() {
            assertNotNull(Masking.address("서울 강남"));
        }

        @Test
        @DisplayName("null 입력")
        void nullAddress() {
            assertNull(Masking.address(null));
        }
    }

    @Nested
    @DisplayName("IP 주소 마스킹")
    class IpMasking {

        @Test
        @DisplayName("IPv4 마스킹")
        void ipv4() {
            assertEquals("192.168.1.***", Masking.ip("192.168.1.100"));
            assertEquals("10.0.0.***", Masking.ip("10.0.0.1"));
        }

        @Test
        @DisplayName("null 입력")
        void nullIp() {
            assertNull(Masking.ip(null));
        }
    }

    @Nested
    @DisplayName("차량번호 마스킹")
    class CarNumberMasking {

        @Test
        @DisplayName("일반 차량번호 마스킹")
        void normalCarNumber() {
            assertEquals("12가****", Masking.carNumber("12가1234"));
            assertEquals("123*****", Masking.carNumber("123가4567"));
        }

        @Test
        @DisplayName("짧은 차량번호")
        void shortCarNumber() {
            assertEquals("12가", Masking.carNumber("12가"));
        }

        @Test
        @DisplayName("null 입력")
        void nullCarNumber() {
            assertNull(Masking.carNumber(null));
        }
    }

    @Nested
    @DisplayName("주민등록번호 마스킹")
    class ResidentNoMasking {

        @Test
        @DisplayName("13자리 주민번호 마스킹")
        void normalResidentNo() {
            assertEquals("900101-*******", Masking.residentNo("9001011234567"));
            assertEquals("900101-*******", Masking.residentNo("900101-1234567"));
        }

        @Test
        @DisplayName("null 입력")
        void nullResidentNo() {
            assertNull(Masking.residentNo(null));
        }
    }

    @Nested
    @DisplayName("범용 마스킹")
    class GeneralMasking {

        @Test
        @DisplayName("중간 마스킹")
        void maskMiddle() {
            // abcdefg = 7글자, showLength = 7/4 = 1, 양쪽 1글자씩 표시, 가운데 5개 마스킹
            assertEquals("a*****g", Masking.maskMiddle("abcdefg"));
            assertEquals("12", Masking.maskMiddle("12")); // 2글자 이하는 그대로
        }

        @Test
        @DisplayName("뒤 절반 마스킹")
        void maskLastHalf() {
            assertEquals("abc***", Masking.maskLastHalf("abcdef"));
            assertEquals("", Masking.maskLastHalf(""));
        }

        @Test
        @DisplayName("전체 마스킹")
        void maskAll() {
            assertEquals("*****", Masking.maskAll("hello"));
            assertNull(Masking.maskAll(null));
        }

        @Test
        @DisplayName("커스텀 위치 마스킹")
        void customMask() {
            assertEquals("He***World", Masking.mask("HelloWorld", 2, 3));
            assertEquals("Hello", Masking.mask("Hello", 10, 5)); // 범위 벗어나면 그대로
        }
    }
}
