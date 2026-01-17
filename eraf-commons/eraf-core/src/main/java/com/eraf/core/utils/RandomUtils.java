package com.eraf.core.utils;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 랜덤 생성 유틸리티
 */
public final class RandomUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABETIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";

    private RandomUtils() {
    }

    // ===== 정수 =====

    /**
     * 랜덤 정수 (0 이상 bound 미만)
     */
    public static int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * 랜덤 정수 (min 이상 max 이하)
     */
    public static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min은 max보다 작거나 같아야 합니다");
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * 랜덤 정수 (0 이상 Integer.MAX_VALUE 미만)
     */
    public static int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    /**
     * 보안 랜덤 정수 (0 이상 bound 미만)
     */
    public static int nextSecureInt(int bound) {
        return SECURE_RANDOM.nextInt(bound);
    }

    /**
     * 보안 랜덤 정수 (min 이상 max 이하)
     */
    public static int nextSecureInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min은 max보다 작거나 같아야 합니다");
        }
        if (min == max) {
            return min;
        }
        return SECURE_RANDOM.nextInt(max - min + 1) + min;
    }

    // ===== Long =====

    /**
     * 랜덤 long (0 이상 bound 미만)
     */
    public static long nextLong(long bound) {
        return ThreadLocalRandom.current().nextLong(bound);
    }

    /**
     * 랜덤 long (min 이상 max 이하)
     */
    public static long nextLong(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min은 max보다 작거나 같아야 합니다");
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /**
     * 랜덤 long
     */
    public static long nextLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    // ===== Double =====

    /**
     * 랜덤 double (0.0 이상 1.0 미만)
     */
    public static double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * 랜덤 double (0.0 이상 bound 미만)
     */
    public static double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * 랜덤 double (min 이상 max 미만)
     */
    public static double nextDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min은 max보다 작거나 같아야 합니다");
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    // ===== Float =====

    /**
     * 랜덤 float (0.0 이상 1.0 미만)
     */
    public static float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    /**
     * 랜덤 float (0.0 이상 bound 미만)
     */
    public static float nextFloat(float bound) {
        return ThreadLocalRandom.current().nextFloat() * bound;
    }

    /**
     * 랜덤 float (min 이상 max 미만)
     */
    public static float nextFloat(float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("min은 max보다 작거나 같아야 합니다");
        }
        if (min == max) {
            return min;
        }
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    // ===== Boolean =====

    /**
     * 랜덤 boolean
     */
    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * 특정 확률로 true 반환 (0.0 ~ 1.0)
     */
    public static boolean nextBoolean(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("확률은 0.0에서 1.0 사이여야 합니다");
        }
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    // ===== 문자열 =====

    /**
     * 랜덤 알파벳+숫자 문자열
     */
    public static String nextAlphanumeric(int length) {
        return nextString(length, ALPHANUMERIC);
    }

    /**
     * 랜덤 알파벳 문자열
     */
    public static String nextAlphabetic(int length) {
        return nextString(length, ALPHABETIC);
    }

    /**
     * 랜덤 숫자 문자열
     */
    public static String nextNumeric(int length) {
        return nextString(length, NUMERIC);
    }

    /**
     * 커스텀 문자셋으로 랜덤 문자열 생성
     */
    public static String nextString(int length, String charset) {
        if (length <= 0) {
            return "";
        }
        if (charset == null || charset.isEmpty()) {
            throw new IllegalArgumentException("charset은 비어있을 수 없습니다");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = nextInt(charset.length());
            sb.append(charset.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 보안 랜덤 알파벳+숫자 문자열 (토큰, 비밀번호 등에 사용)
     */
    public static String nextSecureAlphanumeric(int length) {
        return nextSecureString(length, ALPHANUMERIC);
    }

    /**
     * 보안 랜덤 문자열
     */
    public static String nextSecureString(int length, String charset) {
        if (length <= 0) {
            return "";
        }
        if (charset == null || charset.isEmpty()) {
            throw new IllegalArgumentException("charset은 비어있을 수 없습니다");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(charset.length());
            sb.append(charset.charAt(index));
        }
        return sb.toString();
    }

    // ===== 바이트 배열 =====

    /**
     * 랜덤 바이트 배열
     */
    public static byte[] nextBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    /**
     * 보안 랜덤 바이트 배열
     */
    public static byte[] nextSecureBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    // ===== 배열/리스트에서 선택 =====

    /**
     * 배열에서 랜덤 요소 선택
     */
    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[nextInt(array.length)];
    }

    /**
     * 리스트에서 랜덤 요소 선택
     */
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * 배열에서 여러 개 랜덤 선택 (중복 허용)
     */
    public static <T> T[] randomElements(T[] array, int count) {
        if (array == null || array.length == 0 || count <= 0) {
            return array;
        }
        @SuppressWarnings("unchecked")
        T[] result = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), count);
        for (int i = 0; i < count; i++) {
            result[i] = array[nextInt(array.length)];
        }
        return result;
    }

    // ===== Hex =====

    /**
     * 랜덤 Hex 문자열
     */
    public static String nextHex(int length) {
        if (length <= 0) {
            return "";
        }
        byte[] bytes = nextBytes((length + 1) / 2);
        return bytesToHex(bytes).substring(0, length);
    }

    /**
     * 보안 랜덤 Hex 문자열
     */
    public static String nextSecureHex(int length) {
        if (length <= 0) {
            return "";
        }
        byte[] bytes = nextSecureBytes((length + 1) / 2);
        return bytesToHex(bytes).substring(0, length);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ===== 특수 용도 =====

    /**
     * 랜덤 6자리 숫자 (OTP/PIN 등에 사용)
     */
    public static String nextOtp() {
        return nextOtp(6);
    }

    /**
     * 랜덤 n자리 숫자
     */
    public static String nextOtp(int length) {
        return nextNumeric(length);
    }

    /**
     * 보안 랜덤 6자리 숫자
     */
    public static String nextSecureOtp() {
        return nextSecureOtp(6);
    }

    /**
     * 보안 랜덤 n자리 숫자
     */
    public static String nextSecureOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 랜덤 색상 코드 (Hex)
     */
    public static String nextColor() {
        return String.format("#%06X", nextInt(0x1000000));
    }

    /**
     * 랜덤 RGB 색상 (r, g, b)
     */
    public static int[] nextRgb() {
        return new int[]{nextInt(256), nextInt(256), nextInt(256)};
    }

    // ===== 확률/가중치 =====

    /**
     * 가중치 기반 인덱스 선택
     * weights = [10, 20, 30] -> 0번 10%, 1번 20%, 2번 30%
     */
    public static int nextWeightedIndex(int[] weights) {
        if (weights == null || weights.length == 0) {
            return -1;
        }
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }
        if (total == 0) {
            return -1;
        }
        int random = nextInt(total);
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
            if (random < sum) {
                return i;
            }
        }
        return weights.length - 1;
    }

    /**
     * 가중치 기반 요소 선택
     */
    public static <T> T nextWeightedElement(T[] elements, int[] weights) {
        if (elements == null || weights == null || elements.length != weights.length) {
            return null;
        }
        int index = nextWeightedIndex(weights);
        return index >= 0 ? elements[index] : null;
    }

    // ===== Gaussian (정규분포) =====

    /**
     * 가우시안 분포 랜덤 double (평균 0.0, 표준편차 1.0)
     */
    public static double nextGaussian() {
        return ThreadLocalRandom.current().nextGaussian();
    }

    /**
     * 가우시안 분포 랜덤 double (지정된 평균, 표준편차)
     */
    public static double nextGaussian(double mean, double stddev) {
        return mean + stddev * nextGaussian();
    }

    // ===== 유틸리티 =====

    /**
     * SecureRandom 인스턴스 반환 (직접 사용 시)
     */
    public static SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }

    /**
     * ThreadLocalRandom 인스턴스 반환 (직접 사용 시)
     */
    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }
}
