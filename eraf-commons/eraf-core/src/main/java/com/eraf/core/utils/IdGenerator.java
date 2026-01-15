package com.eraf.core.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 생성 유틸리티
 */
public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] NANOID_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz-".toCharArray();

    private static final AtomicInteger ULID_COUNTER = new AtomicInteger(RANDOM.nextInt());

    private IdGenerator() {
    }

    // ===== UUID =====

    /**
     * UUID v4 생성 (하이픈 포함)
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * UUID v4 생성 (하이픈 제외)
     */
    public static String uuidCompact() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ===== ULID =====

    /**
     * ULID 생성 (Universally Unique Lexicographically Sortable Identifier)
     * 26자의 대소문자 구분 문자열, 시간순 정렬 가능
     */
    public static String ulid() {
        long timestamp = Instant.now().toEpochMilli();
        return encodeUlid(timestamp);
    }

    private static String encodeUlid(long timestamp) {
        char[] chars = new char[26];

        // 타임스탬프 (10자)
        for (int i = 9; i >= 0; i--) {
            chars[i] = ALPHABET[(int) (timestamp % 32)];
            timestamp /= 32;
        }

        // 랜덤 (16자)
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        int counter = ULID_COUNTER.incrementAndGet();

        for (int i = 10; i < 26; i++) {
            int idx = (i < 14) ?
                    ((random[i - 10] & 0xFF) + counter) % 32 :
                    (random[i - 10] & 0xFF) % 32;
            chars[i] = ALPHABET[idx];
        }

        return new String(chars);
    }

    // ===== NanoID =====

    /**
     * NanoID 생성 (기본 21자)
     */
    public static String nanoid() {
        return nanoid(21);
    }

    /**
     * NanoID 생성 (길이 지정)
     */
    public static String nanoid(int size) {
        char[] id = new char[size];
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);

        for (int i = 0; i < size; i++) {
            id[i] = NANOID_ALPHABET[bytes[i] & 63];
        }

        return new String(id);
    }

    /**
     * 커스텀 알파벳으로 NanoID 생성
     */
    public static String nanoid(int size, String alphabet) {
        char[] alphabetChars = alphabet.toCharArray();
        int mask = (2 << (int) Math.floor(Math.log(alphabetChars.length - 1) / Math.log(2))) - 1;
        int step = (int) Math.ceil(1.6 * mask * size / alphabetChars.length);

        StringBuilder id = new StringBuilder();
        while (id.length() < size) {
            byte[] bytes = new byte[step];
            RANDOM.nextBytes(bytes);
            for (int i = 0; i < step && id.length() < size; i++) {
                int idx = bytes[i] & mask;
                if (idx < alphabetChars.length) {
                    id.append(alphabetChars[idx]);
                }
            }
        }

        return id.toString();
    }

    // ===== 숫자 ID =====

    /**
     * 숫자만으로 구성된 랜덤 ID 생성
     */
    public static String numericId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 타임스탬프 기반 ID 생성 (밀리초 + 랜덤 4자리)
     */
    public static String timestampId() {
        return String.valueOf(System.currentTimeMillis()) + String.format("%04d", RANDOM.nextInt(10000));
    }

    // ===== 순차 ID (접두사 포함) =====

    /**
     * 접두사 + 타임스탬프 + 랜덤 ID 생성
     */
    public static String prefixedId(String prefix) {
        return prefix + "-" + timestampId();
    }

    /**
     * 접두사 + UUID
     */
    public static String prefixedUuid(String prefix) {
        return prefix + "-" + uuidCompact();
    }
}
