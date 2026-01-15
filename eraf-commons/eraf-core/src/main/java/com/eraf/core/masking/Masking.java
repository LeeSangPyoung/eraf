package com.eraf.core.masking;

/**
 * 마스킹 유틸리티
 */
public final class Masking {

    private static final char MASK_CHAR = '*';

    private Masking() {
    }

    /**
     * 이름 마스킹
     * 예: 홍길동 -> 홍*동, 김철수 -> 김*수
     */
    public static String name(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        // 가운데 글자 마스킹
        int maskLength = name.length() - 2;
        return name.charAt(0) + repeat(MASK_CHAR, maskLength) + name.charAt(name.length() - 1);
    }

    /**
     * 전화번호 마스킹
     * 예: 01012345678 -> 010-****-5678
     */
    public static String phone(String phone) {
        if (phone == null) {
            return null;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() == 11) {
            // 010-****-5678
            return cleaned.substring(0, 3) + "-****-" + cleaned.substring(7);
        } else if (cleaned.length() == 10) {
            // 02-****-5678 or 031-***-5678
            if (cleaned.startsWith("02")) {
                return cleaned.substring(0, 2) + "-****-" + cleaned.substring(6);
            }
            return cleaned.substring(0, 3) + "-***-" + cleaned.substring(6);
        }
        return maskMiddle(phone);
    }

    /**
     * 전화번호 마스킹 (패턴 지정)
     */
    public static String phone(String phone, MaskPattern pattern) {
        if (phone == null) {
            return null;
        }
        String cleaned = phone.replaceAll("[^0-9]", "");

        return switch (pattern) {
            case SHOW_FIRST -> {
                // 앞 3자리만 표시: 010-****-****
                if (cleaned.length() >= 3) {
                    yield cleaned.substring(0, 3) + "-****-****";
                }
                yield maskAll(phone);
            }
            case SHOW_LAST -> {
                // 뒤 4자리만 표시: ***-****-5678
                if (cleaned.length() >= 4) {
                    yield "***-****-" + cleaned.substring(cleaned.length() - 4);
                }
                yield maskAll(phone);
            }
            default -> phone(phone);
        };
    }

    /**
     * 이메일 마스킹
     * 예: test@gmail.com -> te**@gmail.com
     */
    public static String email(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return repeat(MASK_CHAR, localPart.length()) + "@" + domain;
        }
        int showLength = Math.min(2, localPart.length() / 2);
        return localPart.substring(0, showLength) +
                repeat(MASK_CHAR, localPart.length() - showLength) +
                "@" + domain;
    }

    /**
     * 카드번호 마스킹
     * 예: 1234567890123456 -> 1234-****-****-3456
     */
    public static String card(String cardNo) {
        if (cardNo == null) {
            return null;
        }
        String cleaned = cardNo.replaceAll("[^0-9]", "");
        if (cleaned.length() < 16) {
            return maskMiddle(cardNo);
        }
        return cleaned.substring(0, 4) + "-****-****-" + cleaned.substring(12);
    }

    /**
     * 계좌번호 마스킹
     * 예: 1234567890 -> 123-***-*890
     */
    public static String account(String accountNo) {
        if (accountNo == null) {
            return null;
        }
        String cleaned = accountNo.replaceAll("[^0-9]", "");
        if (cleaned.length() < 6) {
            return maskMiddle(accountNo);
        }
        int showFirst = 3;
        int showLast = 3;
        int maskLength = cleaned.length() - showFirst - showLast;
        return cleaned.substring(0, showFirst) + "-" +
                repeat(MASK_CHAR, maskLength) + "-" +
                cleaned.substring(cleaned.length() - showLast);
    }

    /**
     * 주소 마스킹
     * 예: 서울시 강남구 역삼동 123-45 -> 서울시 강남구 ***
     */
    public static String address(String address) {
        if (address == null) {
            return null;
        }
        // 시/군/구 까지만 표시
        String[] parts = address.split(" ");
        if (parts.length <= 2) {
            return address.substring(0, Math.min(address.length(), address.length() / 2)) + " ***";
        }
        return parts[0] + " " + parts[1] + " ***";
    }

    /**
     * IP 주소 마스킹
     * 예: 192.168.1.100 -> 192.168.1.***
     */
    public static String ip(String ip) {
        if (ip == null) {
            return null;
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot < 0) {
            return maskMiddle(ip);
        }
        return ip.substring(0, lastDot + 1) + "***";
    }

    /**
     * 차량번호 마스킹
     * 예: 12가1234 -> 12가****
     */
    public static String carNumber(String carNo) {
        if (carNo == null || carNo.length() < 4) {
            return carNo;
        }
        // 앞 3자리 표시, 나머지 마스킹
        int showLength = 3;
        return carNo.substring(0, showLength) + repeat(MASK_CHAR, carNo.length() - showLength);
    }

    /**
     * 주민등록번호 마스킹
     * 예: 900101-1234567 -> 900101-*******
     */
    public static String residentNo(String residentNo) {
        if (residentNo == null) {
            return null;
        }
        String cleaned = residentNo.replaceAll("[^0-9]", "");
        if (cleaned.length() == 13) {
            return cleaned.substring(0, 6) + "-*******";
        }
        return maskLastHalf(residentNo);
    }

    // ===== 범용 마스킹 =====

    /**
     * 중간 마스킹
     */
    public static String maskMiddle(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        int showLength = Math.max(1, str.length() / 4);
        int maskLength = str.length() - (showLength * 2);
        return str.substring(0, showLength) +
                repeat(MASK_CHAR, maskLength) +
                str.substring(str.length() - showLength);
    }

    /**
     * 뒤 절반 마스킹
     */
    public static String maskLastHalf(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        int showLength = str.length() / 2;
        return str.substring(0, showLength) + repeat(MASK_CHAR, str.length() - showLength);
    }

    /**
     * 전체 마스킹
     */
    public static String maskAll(String str) {
        if (str == null) {
            return null;
        }
        return repeat(MASK_CHAR, str.length());
    }

    /**
     * 커스텀 마스킹
     *
     * @param str        원본 문자열
     * @param startIndex 마스킹 시작 인덱스
     * @param length     마스킹 길이
     * @return 마스킹된 문자열
     */
    public static String mask(String str, int startIndex, int length) {
        if (str == null || startIndex < 0 || length <= 0) {
            return str;
        }
        if (startIndex >= str.length()) {
            return str;
        }
        int endIndex = Math.min(startIndex + length, str.length());
        return str.substring(0, startIndex) +
                repeat(MASK_CHAR, endIndex - startIndex) +
                str.substring(endIndex);
    }

    private static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        return String.valueOf(c).repeat(count);
    }
}
